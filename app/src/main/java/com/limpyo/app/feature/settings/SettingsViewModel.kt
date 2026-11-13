package com.limpyo.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limpyo.app.core.data.AppLanguage
import com.limpyo.app.core.data.LanguageRepository
import com.limpyo.app.core.data.SettingsRepository
import com.limpyo.app.core.designsystem.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Immutable screen state (docs/ARCHITECTURE.md: UiState + StateFlow + event functions). */
data class SettingsUiState(
    val language: AppLanguage = AppLanguage.English,
    val themeMode: ThemeMode = ThemeMode.Dark,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val languages: LanguageRepository,
) : ViewModel() {

    val state: StateFlow<SettingsUiState> = settings.themeMode
        .map { SettingsUiState(language = languages.current(), themeMode = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState(language = languages.current()))

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    /** Switching language restarts the screen in the new language (Android does this). */
    fun setLanguage(language: AppLanguage) {
        if (language != languages.current()) languages.set(language)
    }
}
