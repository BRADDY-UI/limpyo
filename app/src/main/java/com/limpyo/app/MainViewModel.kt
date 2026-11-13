package com.limpyo.app

import androidx.lifecycle.ViewModel
import com.limpyo.app.core.data.ThemeSettingsRepository
import com.limpyo.app.core.designsystem.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * A ViewModel keeps screen state alive across rotation. Hilt passes in the repository.
 * Pattern for every screen (docs/ARCHITECTURE.md): state goes out as a StateFlow,
 * user actions come in as plain functions.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val themeSettings: ThemeSettingsRepository,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themeSettings.themeMode

    fun setThemeMode(mode: ThemeMode) = themeSettings.setThemeMode(mode)
}
