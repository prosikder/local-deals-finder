package com.localdeals.finder.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "LocalDealsFinderSessionKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PREFIX = "enc_v1:"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Fallback key for non-Android JVM environments (e.g. unit tests without AndroidKeyStore)
    private val fallbackKeyBytes = byteArrayOf(
        0x1f, 0x2e, 0x3d, 0x4c, 0x5b, 0x6a, 0x79, 0x88.toByte(),
        0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte(),
        0x99.toByte(), 0xaa.toByte(), 0xbb.toByte(), 0xcc.toByte(),
        0xdd.toByte(), 0xee.toByte(), 0xff.toByte(), 0x00,
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    )

    private fun getSecretKey(): Key {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val builder = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            } else {
                keyStore.getKey(KEY_ALIAS, null)
            }
        } catch (e: Exception) {
            // Fallback for Robolectric/JVM testing where AndroidKeyStore provider is not registered
            SecretKeySpec(fallbackKeyBytes, "AES")
        }
    }

    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val key = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            PREFIX + encodeBase64(combined)
        } catch (e: Exception) {
            // Fail-safe fallback if crypto fails
            plaintext
        }
    }

    fun decrypt(ciphertext: String): String {
        if (ciphertext.isEmpty()) return ""
        if (!ciphertext.startsWith(PREFIX)) {
            // Legacy unencrypted plaintext session
            return ciphertext
        }
        return try {
            val base64Data = ciphertext.removePrefix(PREFIX)
            val combined = decodeBase64(base64Data)
            if (combined.size <= GCM_IV_LENGTH) return ciphertext

            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // If decryption fails, return empty to trigger re-login gracefully
            ""
        }
    }

    private fun encodeBase64(data: ByteArray): String {
        return try {
            Base64.encodeToString(data, Base64.NO_WRAP)
        } catch (e: Exception) {
            java.util.Base64.getEncoder().encodeToString(data)
        }
    }

    private fun decodeBase64(data: String): ByteArray {
        return try {
            Base64.decode(data, Base64.NO_WRAP)
        } catch (e: Exception) {
            java.util.Base64.getDecoder().decode(data)
        }
    }
}
