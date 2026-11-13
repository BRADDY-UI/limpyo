package com.limpyo.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.ThemeMode
import com.limpyo.app.core.designsystem.theme.isDarkTheme
import com.limpyo.app.navigation.LimpyoRoot
import dagger.hilt.android.AndroidEntryPoint

/**
 * The only Activity. Everything on screen is Compose, drawn by [LimpyoRoot].
 * It's an AppCompatActivity so the in-app language switch works on Android 8-12 too.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the splash until the saved theme is known (a few ms), so there's no theme flash.
        splash.setKeepOnScreenCondition { viewModel.themeMode.value == null }
        enableEdgeToEdge()

        setContent {
            val saved by viewModel.themeMode.collectAsStateWithLifecycle()
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
                LimpyoRoot()
            }
        }
    }
}
