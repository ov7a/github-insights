package ru.ov7a.pull_requests.domain

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ParseRepoIdTests {

    @Test
    fun should_parse_valid_id() {
        val input = "ov7a/ololo"
        RepositoryId.parse(input) shouldBe RepositoryId(owner = "ov7a", name = "ololo")
    }

    @Test
    fun should_parse_valid_url() {
        val input = "https://github.com/ov7a/kotlin"
        RepositoryId.parse(input) shouldBe RepositoryId(owner = "ov7a", name = "kotlin")
    }

    @Test
    fun should_ignore_spaces_and_trailing_slash() {
        val input = "  https://github.com/ov7a/ov7a.github.io/  "
        RepositoryId.parse(input) shouldBe RepositoryId(owner = "ov7a", name = "ov7a.github.io")
    }

    @Test
    fun should_return_null_for_trash() {
        val input = "asdlaskdjlkj"
        RepositoryId.parse(input) shouldBe null
    }

    @Test
    fun should_return_null_for_id_without_slash() {
        val input = "/kotlin"
        RepositoryId.parse(input) shouldBe null
    }

    @Test
    fun should_not_allow_extra_subpaths() {
        val input = "https://github.com/ov7a/kotlin-asserts-comparison/pulls"
        RepositoryId.parse(input) shouldBe null
    }
}