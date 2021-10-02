package ru.ov7a.pull_requests.fetcher.rest

import PullRequestDTO
import PullRequestStateDTO
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import ru.ov7a.pull_requests.domain.PullRequest
import ru.ov7a.pull_requests.domain.PullRequestState
import ru.ov7a.pull_requests.domain.PullRequestsBatch
import ru.ov7a.pull_requests.domain.RepositoryId

class RestPullRequestsClient(
    private val client: HttpClient
) {

    private suspend fun fetch(path: String, pageNumber: Int, authorizationHeader: String? = null): PullRequestsPage {
        val response: HttpResponse = client.get(
            scheme = "https",
            host = "api.github.com",
            path = path,
        ) {
            url {
                parameter("state", "all")
                parameter("per_page", 100)
                parameter("page", pageNumber)
            }
            headers {
                append(HttpHeaders.Accept, GITHUB_API_CONTENT_TYPE)
                authorizationHeader?.let { append(HttpHeaders.Authorization, it) }
            }
        }
        val (hasNextPage, approximateTotalCount) = detectNextPage(response.headers)
        return PullRequestsPage(response.receive(), hasNextPage, approximateTotalCount)
    }

    //dirty hack, but better this than extra request
    private fun detectNextPage(headers: Headers): Pair<Boolean, Int> {
        val linkHeader = headers[HttpHeaders.Link]
            ?: return false to PAGE_SIZE

        val hasNextPage = linkHeader.contains(NEXT_PAGE_MARKER)

        val lastPage = LAST_PAGE_PATTERN.find(linkHeader)?.let {
            it.groups[1]?.value?.toIntOrNull()
        } ?: 1

        return hasNextPage to (lastPage * PAGE_SIZE)
    }

    private fun PullRequestDTO.toDomain() = PullRequest(
        url = this.htmlUrl,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        closedAt = this.closedAt,
        mergedAt = this.mergedAt,
        state = when (this.state) {
            PullRequestStateDTO.OPEN -> PullRequestState.OPEN
            PullRequestStateDTO.CLOSED -> if (this.mergedAt != null) PullRequestState.MERGED else PullRequestState.CLOSED
        },
    )

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    suspend fun fetchAll(repositoryId: RepositoryId, authorizationHeader: String? = null): Flow<PullRequestsBatch> {
        val path = "/repos/${repositoryId.owner}/${repositoryId.name}/pulls"
        return generateSequence(1) { it + 1 }
            .asFlow()
            .map { pageNumber -> fetch(path, pageNumber, authorizationHeader) }
            .transformWhile { page ->
                emit(
                    PullRequestsBatch(
                        totalCount = page.approximateTotalCount,
                        pullRequests = page.pullRequests.map { it.toDomain() }
                    )
                )
                page.hasNextPage && page.pullRequests.isNotEmpty()
            }
    }

    private companion object {
        const val GITHUB_API_CONTENT_TYPE = "application/vnd.github.v3+json"
        const val NEXT_PAGE_MARKER = """; rel="next""""
        val LAST_PAGE_PATTERN = """&page=(\d+)>; rel="last"""".toRegex()
        const val PAGE_SIZE = 100

        data class PullRequestsPage(
            val pullRequests: List<PullRequestDTO>,
            val hasNextPage: Boolean,
            val approximateTotalCount: Int,
        )
    }
}
