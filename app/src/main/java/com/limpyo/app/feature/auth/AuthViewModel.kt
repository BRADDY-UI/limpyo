package com.limpyo.app.feature.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limpyo.app.core.auth.AuthError
import com.limpyo.app.core.auth.AuthException
import com.limpyo.app.core.auth.AuthRepository
import com.limpyo.app.core.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Strength { Weak, Okay, Strong }

/** Simple, explainable strength meter. The server enforces the real minimum (10 characters). */
fun passwordStrength(p: String): Strength {
    var score = 0
    if (p.length >= MIN_PASSWORD) score++
    if (p.length >= 14) score++
    if (p.any { it.isDigit() }) score++
    if (p.any { it.isUpperCase() } && p.any { it.isLowerCase() }) score++
    if (p.any { !it.isLetterOrDigit() }) score++
    return when {
        p.length < MIN_PASSWORD || score <= 2 -> Strength.Weak
        score <= 3 -> Strength.Okay
        else -> Strength.Strong
    }
}

const val MIN_PASSWORD = 10

fun isValidEmail(s: String) = Patterns.EMAIL_ADDRESS.matcher(s.trim()).matches()

fun isValidName(s: String) = s.trim().length in 2..30

data class AuthUiState(
    val busy: Boolean = false,
    val error: AuthError? = null,
    val pendingEmail: String = "",
    val resendWait: Int = 0,
    val resetSent: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AuthRepository,
    /** Needed by the native Google sign-in helper (compose-auth). */
    val supabase: SupabaseClient,
) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    val authState: StateFlow<AuthState> = auth.state.stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)

    fun clearError() = _ui.update { it.copy(error = null) }

    fun signIn(email: String, password: String, onDone: () -> Unit) = run { auth.signIn(email, password); onDone() }

    fun signUp(name: String, email: String, password: String, onSent: () -> Unit) = run {
        auth.signUp(email, password, name)
        _ui.update { it.copy(pendingEmail = email.trim()) }
        startCooldown()
        onSent()
    }

    fun resend() {
        if (_ui.value.resendWait > 0) return
        run { auth.resendConfirmation(_ui.value.pendingEmail); startCooldown() }
    }

    fun sendReset(email: String) = run { auth.sendPasswordReset(email); _ui.update { it.copy(resetSent = true) } }

    fun setNewPassword(password: String) = run { auth.setNewPassword(password) }

    fun onGoogleSignedIn(onDone: () -> Unit) {
        viewModelScope.launch {
            auth.syncProfile()
            onDone()
        }
    }

    fun onGoogleFailed() = _ui.update { it.copy(error = AuthError.Unknown) }

    private fun run(block: suspend () -> Unit) {
        if (_ui.value.busy) return
        viewModelScope.launch {
            _ui.update { it.copy(busy = true, error = null) }
            try {
                block()
            } catch (e: AuthException) {
                _ui.update { it.copy(error = e.error) }
            } finally {
                _ui.update { it.copy(busy = false) }
            }
        }
    }

    private fun startCooldown() {
        viewModelScope.launch {
            for (s in 60 downTo 0) {
                _ui.update { it.copy(resendWait = s) }
                if (s > 0) delay(1_000)
            }
        }
    }
}
