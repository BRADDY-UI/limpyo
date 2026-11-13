package com.limpyo.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limpyo.app.core.data.AppLanguage
import com.limpyo.app.core.data.LanguageRepository
import com.limpyo.app.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val languages: LanguageRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val _language = MutableStateFlow(languages.current())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        languages.set(lang) // the screen restarts in the new language; the flow keeps its place
    }

    fun finish(onFinished: () -> Unit) {
        viewModelScope.launch {
            settings.setOnboardingDone()
            onFinished()
        }
    }
}
