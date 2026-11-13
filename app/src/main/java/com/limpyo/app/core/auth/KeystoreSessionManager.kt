package com.limpyo.app.core.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores the Supabase login session encrypted at rest (docs/SECURITY.md, Android app).
 * The AES key lives in the Android Keystore: it never leaves the phone's secure hardware and
 * can't be copied out, so a copied app folder is useless on another device.
 * Backups are off (allowBackup=false), so the file isn't copied anywhere either.
 */
class KeystoreSessionManager(context: Context) : SessionManager {
    private val file = File(context.noBackupFilesDir, "session.bin")
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) = withContext(Dispatchers.IO) {
        val plain = json.encodeToString(UserSession.serializer(), session).toByteArray()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val sealed = cipher.iv + cipher.doFinal(plain)
        file.writeText(Base64.encodeToString(sealed, Base64.NO_WRAP))
    }

    /** Throws when there is no usable session; supabase-kt calls loadSessionOrNull(), which turns that into null. */
    override suspend fun loadSession(): UserSession = withContext(Dispatchers.IO) {
        check(file.exists()) { "no stored session" }
        try {
            val sealed = Base64.decode(file.readText(), Base64.NO_WRAP)
            val iv = sealed.copyOfRange(0, IV_BYTES)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, iv))
            }
            json.decodeFromString(UserSession.serializer(), String(cipher.doFinal(sealed, IV_BYTES, sealed.size - IV_BYTES)))
        } catch (e: Exception) {
            // Key reset (e.g. new lock screen) or corrupt file: sign the user out instead of crashing.
            Timber.w(e, "Stored session unreadable; signing out")
            file.delete()
            throw IllegalStateException("stored session unreadable", e)
        }
    }

    override suspend fun deleteSession() {
        withContext(Dispatchers.IO) { file.delete() }
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
        }.generateKey()
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "limpyo_session_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
