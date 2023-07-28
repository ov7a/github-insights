package ru.ov7a.github.insights.domain.input

data class RepositoryId(val owner: String, val name: String) {
    companion object {
        // named groups aren't supported in JS
        private val pattern = """\s*(?:https?://github.com/)?([^/\s"]+)/([^/\s"]+)/?\s*""".toRegex()

        fun parse(input: String): RepositoryId? {
            val match = pattern.matchEntire(input) ?: return null

            val owner = match.groups[1]?.value ?: return null
            val name = match.groups[2]?.value ?: return null

            return RepositoryId(owner, name)
        }
    }
}