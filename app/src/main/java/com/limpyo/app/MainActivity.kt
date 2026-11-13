package com.limpyo.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.ThemeMode
import com.limpyo.app.core.designsystem.theme.isDarkTheme
import com.limpyo.app.core.auth.AuthRepository
import com.limpyo.app.feature.auth.NewPasswordScreen
import com.limpyo.app.feature.onboarding.OnboardingFlow
import com.limpyo.app.navigation.LimpyoRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The only Activity. Everything on screen is Compose, drawn by [LimpyoRoot].
 * It's an AppCompatActivity so the in-app language switch works on Android 8-12 too.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject lateinit var auth: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the splash until the saved theme is known (a few ms), so there's no theme flash.
        splash.setKeepOnScreenCondition { viewModel.themeMode.value == null || viewModel.onboardingDone.value == null }
        enableEdgeToEdge()
        auth.handleDeeplink(intent) // opened from a sign-up or reset email

        setContent {
            val recovering by auth.recovering.collectAsStateWithLifecycle()
            val saved by viewModel.themeMode.collectAsStateWithLifecycle()
            val onboarded by viewModel.onboardingDone.collectAsStateWithLifecycle()
            val themeMode = saved ?: ThemeMode.Dark
            val dark = isDarkTheme(themeMode)

            // Light status-bar icons on Night Route, dark icons on Day Route.
            DisposableEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }

            LimpyoTheme(themeMode) {
                Box(Modifier.fillMaxSize().background(LimpyoTheme.colors.ground)) {
                    when (onboarded) {
                        null -> Unit // splash is still showing
                        false -> OnboardingFlow(onFinished = {}) // DataStore flips onboardingDone; this recomposes
                        true -> LimpyoRoot()
                    }
                    // After a password-reset link: choose a new password before anything else.
                    if (recovering) {
                        Box(Modifier.fillMaxSize().background(LimpyoTheme.colors.ground)) { NewPasswordScreen() }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        auth.handleDeeplink(intent)
    }
}
