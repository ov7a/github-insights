import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PullRequestStateDTO {
    @SerialName("open")
    OPEN,

    @SerialName("closed")
    CLOSED
}

@Serializable
data class PullRequestDTO(
    @SerialName("html_url")
    val htmlUrl: String,
    val state: PullRequestStateDTO,
    @SerialName("created_at")
    val createdAt: Instant,
    @SerialName("updated_at")
    val updatedAt: Instant? = null,
    @SerialName("closed_at")
    val closedAt: Instant? = null,
    @SerialName("merged_at")
    val mergedAt: Instant? = null
)