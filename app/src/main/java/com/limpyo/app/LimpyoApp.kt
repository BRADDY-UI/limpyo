package com.limpyo.app

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * The app's starting point, created once when Android launches Limpyo.
 * @HiltAndroidApp switches on Hilt (dependency injection) for the whole app.
 */
@HiltAndroidApp
class LimpyoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            // Logs appear in Logcat only on debug builds; release builds print nothing.
            Timber.plant(Timber.DebugTree())
            // Proves Firebase Cloud Messaging is wired up: register() makes Firebase call
            // LimpyoMessagingService.onRegistered with this device's push token.
            // Phase 3 sends that token to Supabase.
            FirebaseMessaging.getInstance().register().addOnFailureListener { error ->
                Timber.tag("LimpyoPush").w(error, "FCM registration failed")
            }
        }
    }
}
