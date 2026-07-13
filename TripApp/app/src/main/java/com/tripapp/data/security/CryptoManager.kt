package com.tripapp.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 資安核心：
 * 1. SQLCipher 資料庫密碼：不是寫死在程式碼裡，而是隨機產生後，
 *    透過 Android Keystore 保護的 EncryptedSharedPreferences 存放。
 *    Keystore 的金鑰不會離開硬體安全區域（TEE/StrongBox），
 *    就算 App 資料夾被讀出，密碼本身也是加密狀態。
 * 2. 敏感文件（護照掃描等）在上傳雲端前，用 AES-256-GCM 逐檔加密，
 *    金鑰同樣由 Keystore 保護，雲端只會存到密文。
 */
class CryptoManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /** 取得（或首次產生）本機資料庫加密密碼，供 SQLCipher 使用 */
    fun getOrCreateDbPassphrase(): ByteArray {
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) return android.util.Base64.decode(existing, android.util.Base64.NO_WRAP)

        val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, android.util.Base64.encodeToString(random, android.util.Base64.NO_WRAP))
            .apply()
        return random
    }

    /** 每個行程一把檔案加密金鑰，用於加密護照掃描等敏感文件 */
    private fun getOrCreateFileKey(tripId: String): SecretKey {
        val alias = "$KEY_FILE_PREFIX$tripId"
        val existing = prefs.getString(alias, null)
        if (existing != null) {
            val raw = android.util.Base64.decode(existing, android.util.Base64.NO_WRAP)
            return javax.crypto.spec.SecretKeySpec(raw, "AES")
        }
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        prefs.edit()
            .putString(alias, android.util.Base64.encodeToString(key.encoded, android.util.Base64.NO_WRAP))
            .apply()
        return key
    }

    fun encryptFile(tripId: String, plainBytes: ByteArray): ByteArray {
        val key = getOrCreateFileKey(tripId)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(plainBytes)
        // 前 12 bytes 存 IV，後面接密文，解密時再拆開
        return iv + cipherText
    }

    fun decryptFile(tripId: String, encrypted: ByteArray): ByteArray {
        val key = getOrCreateFileKey(tripId)
        val iv = encrypted.copyOfRange(0, 12)
        val cipherText = encrypted.copyOfRange(12, encrypted.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(cipherText)
    }

    companion object {
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
        private const val KEY_FILE_PREFIX = "file_key_"
    }
}
