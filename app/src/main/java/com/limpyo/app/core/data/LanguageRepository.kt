package com.limpyo.app.core.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** The 3 app languages. [tag] matches the res/values-b+... folders and the profiles.language column. */
enum class AppLanguage(val tag: String) {
    English("en"),
    Filipino("fil"),
    Bisaya("ceb"),
    ;

    companion object {
        fun fromTag(tag: String?): AppLanguage? = entries.firstOrNull { it.tag == tag }
    }
}

/**
 * Per-app language (Android 13+ also shows it in system Settings > Apps > Limpyo > Language).
 * AppCompat stores the choice and restarts the screen in the new language.
 */
@Singleton
class LanguageRepository @Inject constructor() {

    /** The language the app is showing now. If none was picked, follows the phone (Filipino/Bisaya phones get theirs). */
    fun current(): AppLanguage {
        val picked = AppCompatDelegate.getApplicationLocales()[0]?.language
        return AppLanguage.fromTag(picked)
            ?: AppLanguage.fromTag(Locale.getDefault().language)
            ?: AppLanguage.English
    }

    fun set(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}
