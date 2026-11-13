package com.limpyo.app.core.auth

import android.content.Intent
import com.limpyo.app.core.data.LanguageRepository
import com.limpyo.app.core.data.SettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Bumped when the privacy notice changes; stored in profiles.consent_version. */
const val CONSENT_VERSION = 1

sealed interface AuthState {
    data object Loading : AuthState
    data object Guest : AuthState
    data class SignedIn(val userId: String, val email: String?) : AuthState
}

/** What went wrong, in words the UI can translate. Never shows raw server text. */
enum class AuthError { InvalidCredentials, EmailNotConfirmed, EmailTaken, WeakPassword, RateLimited, Network, NotConfigured, Unknown }

class AuthException(val error: AuthError) : Exception(error.name)

@Serializable
data class MyProfile(
    val display_name: String? = null,
    val barangay_id: Int? = null,
    val language: String = "en",
)

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val settings: SettingsRepository,
    private val languages: LanguageRepository,
) {
    val state: Flow<AuthState> = supabase.auth.sessionStatus.map { s ->
        when (s) {
            is SessionStatus.Authenticated -> AuthState.SignedIn(s.session.user?.id.orEmpty(), s.session.user?.email)
            is SessionStatus.NotAuthenticated, is SessionStatus.RefreshFailure -> AuthState.Guest
            SessionStatus.Initializing -> AuthState.Loading
        }
    }

    /** True after a password-reset link opened the app: show "choose a new password". */
    private val _recovering = MutableStateFlow(false)
    val recovering: StateFlow<Boolean> = _recovering.asStateFlow()

    suspend fun signUp(email: String, password: String, displayName: String) = guard {
        supabase.auth.signUpWith(Email, redirectUrl = REDIRECT_CONFIRM) {
            this.email = email.trim()
            this.password = password
            // Only these two are read by the database trigger; a role here is ignored there.
            data = buildJsonObject {
                put("display_name", displayName.trim())
                put("language", languages.current().tag)
            }
        }
    }

    suspend fun signIn(email: String, password: String) = guard {
        supabase.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        syncProfile()
    }

    suspend fun resendConfirmation(email: String) = guard {
        supabase.auth.resendEmail(OtpType.Email.SIGNUP, email.trim(), redirectUrl = REDIRECT_CONFIRM)
    }

    suspend fun sendPasswordReset(email: String) = guard {
        supabase.auth.resetPasswordForEmail(email.trim(), redirectUrl = REDIRECT_RESET)
    }

    suspend fun setNewPassword(password: String) = guard {
        supabase.auth.updateUser { this.password = password }
        _recovering.value = false
    }

    suspend fun signOut() {
        runCatching { supabase.auth.signOut() }.onFailure { Timber.w(it, "sign out") }
    }

    /** Handles limpyo://auth/... links from confirmation and reset emails. */
    fun handleDeeplink(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme != AUTH_SCHEME || data.host != AUTH_HOST) return
        if (data.path == "/reset") _recovering.value = true
        supabase.handleDeeplinks(intent)
    }

    /** Called after any sign-in (email or Google): copy this device's choices into the profile once. */
    suspend fun syncProfile() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val barangay = settings.barangayId.first()
        runCatching {
            supabase.from("profiles").update(
                buildJsonObject {
                    put("language", languages.current().tag)
                    put("consent_version", CONSENT_VERSION)
                    if (barangay != null) put("barangay_id", barangay)
                },
            ) { filter { eq("id", user.id) } }
        }.onFailure { Timber.w(it, "profile sync failed; will retry next sign-in") }
    }

    /** Signed-in users also keep their barangay in their profile (guests only on the device). */
    suspend fun updateBarangay(id: Int) {
        val user = supabase.auth.currentUserOrNull() ?: return
        runCatching {
            supabase.from("profiles").update(buildJsonObject { put("barangay_id", id) }) { filter { eq("id", user.id) } }
        }.onFailure { Timber.w(it, "barangay sync failed") }
    }

    suspend fun myProfile(): MyProfile? {
        val user = supabase.auth.currentUserOrNull() ?: return null
        return runCatching {
            supabase.from("profiles").select { filter { eq("id", user.id) } }.decodeSingle<MyProfile>()
        }.getOrNull()
    }

    private suspend fun guard(block: suspend () -> Unit) {
        if (!isBackendConfigured) throw AuthException(AuthError.NotConfigured)
        try {
            block()
        } catch (e: RestException) {
            throw AuthException(classify(e.error, e.description))
        } catch (e: HttpRequestException) {
            throw AuthException(AuthError.Network)
        }
    }

    private fun classify(code: String?, description: String?): AuthError {
        val text = "${code.orEmpty()} ${description.orEmpty()}".lowercase()
        return when {
            "invalid_credentials" in text || "invalid login" in text -> AuthError.InvalidCredentials
            "email_not_confirmed" in text || "not confirmed" in text -> AuthError.EmailNotConfirmed
            "user_already_exists" in text || "already registered" in text -> AuthError.EmailTaken
            "weak_password" in text || "password should" in text -> AuthError.WeakPassword
            "rate" in text || "too many" in text -> AuthError.RateLimited
            else -> AuthError.Unknown
        }
    }
}
