package com.limpyo.app.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.limpyo.app.core.designsystem.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Device settings that survive restarts (DataStore = a small key/value file in app storage).
 * Nothing personal: theme, whether onboarding is done, and the barangay picked on this device.
 * The app language is stored by Android itself (see LanguageRepository).
 */
@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val data: Flow<Preferences> = context.settingsStore.data
        // A broken settings file must never crash the app: fall back to defaults.
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val themeMode: Flow<ThemeMode> = data.map { prefs ->
        ThemeMode.entries.firstOrNull { it.name == prefs[THEME] } ?: ThemeMode.Dark
    }

    val onboardingDone: Flow<Boolean> = data.map { it[ONBOARDING_DONE] ?: false }

    /** Barangay chosen on this device (guests and signed-in users; signed-in users also save it to their profile). */
    val barangayId: Flow<Int?> = data.map { it[BARANGAY_ID] }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsStore.edit { it[THEME] = mode.name }
    }

    suspend fun setBarangayId(id: Int) {
        context.settingsStore.edit { it[BARANGAY_ID] = id }
    }

    suspend fun setOnboardingDone() {
        context.settingsStore.edit { it[ONBOARDING_DONE] = true }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val BARANGAY_ID = intPreferencesKey("barangay_id")
    }
}
