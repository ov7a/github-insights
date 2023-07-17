package ru.ov7a.github.insights.ui.contexts

import Cookies
import Cookies.CookieAttributes
import kotlin.js.json
import ru.ov7a.github.insights.fetcher.constructBasicAuthValue
import ru.ov7a.github.insights.ui.ValidationException
import ru.ov7a.github.insights.ui.elements.getHtmlElement
import ru.ov7a.github.insights.ui.elements.getInput
import ru.ov7a.github.insights.ui.elements.getSpoiler

class AuthorizationContext {
    private val userIdInput by lazy { getInput(USER_INPUT_ID) }
    private val tokenIdInput by lazy { getInput(TOKEN_INPUT_ID) }

    private val authBlock by lazy { getSpoiler(AUTH_BLOCK_ID) }
    private val authHint by lazy { getHtmlElement(AUTH_HINT_ID) }

    private val authorizedBlock by lazy { getHtmlElement(AUTHORIZED_BLOCK_ID) }
    private val unauthorizedBlock by lazy { getHtmlElement(UNAUTHORIZED_BLOCK_ID) }

    fun init() {
        updateVisibility()
    }

    fun getAuthorization(): String? {
        return Cookies.get(AUTH_DATA_COOKIE)
    }

    fun saveAuthorization() {
        val user = userIdInput.value.trim()
        val token = tokenIdInput.value.trim()
        if (user.isBlank() || token.isBlank()) {
            throw ValidationException("Please, fill both github user name and token")
        }
        val authorizationHeader: String = constructBasicAuthValue(user, token)
        Cookies.set(AUTH_DATA_COOKIE, authorizationHeader, cookieAttrs)
        Cookies.set(USER_COOKIE, user, cookieAttrs)
        updateVisibility()
    }

    fun resetAuthorization() {
        Cookies.remove(AUTH_DATA_COOKIE, cookieAttrs)
        Cookies.remove(USER_COOKIE, cookieAttrs)
        updateVisibility()
    }

    private fun updateVisibility() {
        val authorized = (getAuthorization() != null)

        if (authorized) {
            val user = Cookies.get(USER_COOKIE)
            authHint.textContent = "Authorized as $user"
            authBlock.open = false
        } else {
            authHint.textContent = "Unauthorized"
        }

        authorizedBlock.hidden = !authorized
        unauthorizedBlock.hidden = authorized
    }

    private companion object {
        @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
        val cookieAttrs = json("secure" to true, "sameSite" to "None") as CookieAttributes
        const val AUTH_DATA_COOKIE = "AuthData"
        const val USER_COOKIE = "User"

        const val USER_INPUT_ID = "auth_user"
        const val TOKEN_INPUT_ID = "auth_token"
        const val AUTH_BLOCK_ID = "auth"
        const val AUTH_HINT_ID = "auth_hint"

        const val AUTHORIZED_BLOCK_ID = "authorized"
        const val UNAUTHORIZED_BLOCK_ID = "unauthorized"
    }
}
