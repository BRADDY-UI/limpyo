package com.limpyo.app.feature.you

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limpyo.app.core.auth.AuthRepository
import com.limpyo.app.core.auth.AuthState
import com.limpyo.app.core.data.BarangayRepository
import com.limpyo.app.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class YouUiState(
    val signedIn: Boolean = false,
    val loading: Boolean = true,
    val name: String? = null,
    val email: String? = null,
    val barangay: String? = null,
)

@HiltViewModel
class YouViewModel @Inject constructor(
    private val auth: AuthRepository,
    settings: SettingsRepository,
    barangays: BarangayRepository,
) : ViewModel() {
    val state: StateFlow<YouUiState> = combine(auth.state, settings.barangayId) { a, brgy ->
        val barangayName = brgy?.let { barangays.byId(it)?.name }
        when (a) {
            AuthState.Loading -> YouUiState(loading = true)
            AuthState.Guest -> YouUiState(loading = false, barangay = barangayName)
            is AuthState.SignedIn -> YouUiState(
                signedIn = true,
                loading = false,
                name = auth.myProfile()?.display_name,
                email = a.email,
                barangay = barangayName,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), YouUiState())

    fun signOut() {
        viewModelScope.launch { auth.signOut() }
    }
}
