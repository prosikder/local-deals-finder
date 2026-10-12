package com.localdeals.finder.util

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CryptoManagerTest {

    @Test
    fun encryptAndDecrypt_preservesOriginalString() {
        val originalUserId = "user_admin_999"
        val encrypted = CryptoManager.encrypt(originalUserId)

        assertNotEquals(originalUserId, encrypted)
        assertTrue(encrypted.startsWith("enc_v1:"))

        val decrypted = CryptoManager.decrypt(encrypted)
        assertEquals(originalUserId, decrypted)
    }

    @Test
    fun decryptLegacyPlaintext_returnsRawString() {
        val legacyId = "plain_user_123"
        val decrypted = CryptoManager.decrypt(legacyId)
        assertEquals(legacyId, decrypted)
    }

    @Test
    fun encryptEmpty_returnsEmpty() {
        assertEquals("", CryptoManager.encrypt(""))
        assertEquals("", CryptoManager.decrypt(""))
    }
}
