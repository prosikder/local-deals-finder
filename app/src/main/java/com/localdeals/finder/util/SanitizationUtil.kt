package com.localdeals.finder.util

object SanitizationUtil {
    private val HTML_TAG_PATTERN = Regex("<[^>]*>")
    private val SCRIPT_PATTERN = Regex("(?i)<script.*?>.*?</script>")
    private val JAVASCRIPT_URI_PATTERN = Regex("(?i)javascript:")
    private val CONTROL_CHARS_PATTERN = Regex("[\\p{Cntrl}&&[^\r\n\t]]")
    private val MULTI_SPACE_PATTERN = Regex("\\s+")

    fun sanitizeText(input: String, maxLength: Int = 1000): String {
        if (input.isBlank()) return ""
        var cleaned = input
            .replace(SCRIPT_PATTERN, "")
            .replace(HTML_TAG_PATTERN, "")
            .replace(JAVASCRIPT_URI_PATTERN, "")
            .replace(CONTROL_CHARS_PATTERN, "")
            .trim()

        if (cleaned.length > maxLength) {
            cleaned = cleaned.substring(0, maxLength)
        }
        return cleaned
    }

    fun sanitizeQuery(query: String, maxLength: Int = 100): String {
        if (query.isBlank()) return ""
        val cleaned = sanitizeText(query, maxLength)
            .replace(Regex("['\";]"), "")
            .replace(MULTI_SPACE_PATTERN, " ")
            .trim()
        return cleaned.take(maxLength)
    }

    fun sanitizePhone(phone: String): String {
        if (phone.isBlank()) return ""
        val trimmed = phone.trim()
        val hasPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }
        return if (hasPlus) "+$digits" else digits
    }

    fun sanitizeEmail(email: String): String {
        return email.trim().lowercase()
    }
}
