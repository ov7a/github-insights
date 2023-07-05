package ru.ov7a.github.insights.ui

import ru.ov7a.github.insights.ui.contexts.AuthorizationContext
import ru.ov7a.github.insights.ui.contexts.InputContext
import ru.ov7a.github.insights.ui.contexts.PresentationContext

data class Context(
    val inputs: InputContext = InputContext(),
    val authorization: AuthorizationContext = AuthorizationContext(),
    val presentation: PresentationContext = PresentationContext(),
)