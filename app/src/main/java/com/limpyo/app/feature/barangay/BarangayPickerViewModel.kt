package com.limpyo.app.feature.barangay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limpyo.app.core.auth.AuthRepository
import com.limpyo.app.core.data.BarangayRepository
import com.limpyo.app.core.data.SettingsRepository
import com.limpyo.app.core.location.CoarseLocation
import com.limpyo.app.core.model.Barangay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

enum class LocateStatus { Idle, Locating, Failed, Outside }

data class BarangayPickerUiState(
    val all: List<Barangay> = emptyList(),
    val query: String = "",
    val selectedId: Int? = null,
    val locate: LocateStatus = LocateStatus.Idle,
) {
    /** Search ignores case and accents ("niño" finds "nino"). */
    val shown: List<Barangay>
        get() = if (query.isBlank()) all else all.filter { fold(it.name).contains(fold(query.trim())) }

    private fun fold(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase()
}

@HiltViewModel
class BarangayPickerViewModel @Inject constructor(
    private val barangays: BarangayRepository,
    private val settings: SettingsRepository,
    private val location: CoarseLocation,
    private val auth: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(BarangayPickerUiState())
    val state: StateFlow<BarangayPickerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val list = barangays.all()
            val saved = settings.barangayId.first()
            _state.update { it.copy(all = list, selectedId = saved ?: it.selectedId) }
        }
    }

    fun onQuery(q: String) = _state.update { it.copy(query = q.take(40)) }

    fun select(id: Int) = _state.update { it.copy(selectedId = id, locate = LocateStatus.Idle) }

    /** Call after the location permission was granted (or denied: then it reports Failed). */
    fun locate() {
        viewModelScope.launch {
            _state.update { it.copy(locate = LocateStatus.Locating) }
            val point = location.current()
            if (point == null) {
                _state.update { it.copy(locate = LocateStatus.Failed) }
                return@launch
            }
            val found = barangays.at(point)
            _state.update {
                if (found == null) it.copy(locate = LocateStatus.Outside)
                else it.copy(selectedId = found.id, query = "", locate = LocateStatus.Idle)
            }
        }
    }

    /** Saves the choice on this device. */
    fun save(onSaved: () -> Unit) {
        val id = _state.value.selectedId ?: return
        viewModelScope.launch {
            settings.setBarangayId(id)
            auth.updateBarangay(id) // no-op for guests
            onSaved()
        }
    }
}
