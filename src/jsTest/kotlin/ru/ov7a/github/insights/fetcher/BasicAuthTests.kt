package ru.ov7a.github.insights.fetcher

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BasicAuthTests {

    @Test
    fun should_provide_proper_value() {
        val result = constructBasicAuthValue("the_user", "the_password")

        result shouldBe "Basic dGhlX3VzZXI6dGhlX3Bhc3N3b3Jk"
    }
}
