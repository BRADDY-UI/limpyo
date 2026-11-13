package com.limpyo.app.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
 * Not personal data: theme here; onboarding and guest barangay come in step 1C.
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

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsStore.edit { it[THEME] = mode.name }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_mode")
    }
}
