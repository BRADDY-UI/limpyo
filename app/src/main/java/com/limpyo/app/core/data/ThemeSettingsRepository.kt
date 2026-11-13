package com.limpyo.app.core.data

import com.limpyo.app.core.designsystem.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the chosen theme. Phase 0 keeps it in memory (resets on restart);
 * Phase 1 saves it with DataStore when the real Settings screen arrives.
 *
 * @Singleton + @Inject constructor: Hilt creates exactly one of these and hands it
 * to anything that asks for it, so every screen sees the same theme.
 */
@Singleton
class ThemeSettingsRepository @Inject constructor() {
    private val mode = MutableStateFlow(ThemeMode.Dark)
    val themeMode: StateFlow<ThemeMode> = mode.asStateFlow()

    fun setThemeMode(value: ThemeMode) {
        mode.value = value
    }
}
