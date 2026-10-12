package com.localdeals.finder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SanitizationUtilTest {

    @Test
    fun sanitizeText_stripsHtmlAndScriptTags() {
        val dangerousInput = "<script>alert('xss')</script>Great discount on <b>Pizza</b>!"
        val cleaned = SanitizationUtil.sanitizeText(dangerousInput)
        assertEquals("Great discount on Pizza!", cleaned)
        assertFalse(cleaned.contains("<script>"))
        assertFalse(cleaned.contains("<b>"))
    }

    @Test
    fun sanitizeText_enforcesMaxLength() {
        val longInput = "a".repeat(200)
        val cleaned = SanitizationUtil.sanitizeText(longInput, maxLength = 50)
        assertEquals(50, cleaned.length)
    }

    @Test
    fun sanitizeQuery_stripsSpecialCharsAndExcessWhitespace() {
        val rawQuery = "  Shoes'; DROP TABLE deals;--  "
        val cleaned = SanitizationUtil.sanitizeQuery(rawQuery)
        assertEquals("Shoes DROP TABLE deals--", cleaned)
    }

    @Test
    fun sanitizePhone_normalizesNumber() {
        val raw = "+880 (171) 234-5678"
        val cleaned = SanitizationUtil.sanitizePhone(raw)
        assertEquals("+8801712345678", cleaned)
    }
}
