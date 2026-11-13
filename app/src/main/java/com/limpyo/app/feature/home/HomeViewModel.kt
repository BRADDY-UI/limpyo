package com.limpyo.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limpyo.app.core.data.BarangayRepository
import com.limpyo.app.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(val barangayName: String? = null)

@HiltViewModel
class HomeViewModel @Inject constructor(
    settings: SettingsRepository,
    barangays: BarangayRepository,
) : ViewModel() {
    val state: StateFlow<HomeUiState> = settings.barangayId
        .map { id -> HomeUiState(barangayName = id?.let { barangays.byId(it)?.name }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
