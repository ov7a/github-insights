package ru.ov7a.pull_requests.ui

class ValidationException(override val message: String) : IllegalArgumentException(message)