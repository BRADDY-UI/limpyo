package com.limpyo.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limpyo.app.core.data.SettingsRepository
import com.limpyo.app.core.designsystem.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * App-wide state for MainActivity. [themeMode] is null until the saved choice is read,
 * which keeps the splash screen up for those few milliseconds, so a Light-mode user
 * never sees a flash of the dark theme.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    settings: SettingsRepository,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode?> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)

    /** Also null until read, so the right first screen (onboarding or Home) shows with no flash. */
    val onboardingDone: StateFlow<Boolean?> = settings.onboardingDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
