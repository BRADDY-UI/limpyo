package com.limpyo.app.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.limpyo.app.R
import com.limpyo.app.core.data.AppLanguage
import com.limpyo.app.core.designsystem.component.LimpyoTextButton
import com.limpyo.app.core.designsystem.component.PrimaryButton
import com.limpyo.app.core.designsystem.component.RadioRow
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.tweenMedium
import com.limpyo.app.core.designsystem.theme.tweenShort
import com.limpyo.app.core.auth.isBackendConfigured
import com.limpyo.app.core.auth.isGoogleConfigured
import com.limpyo.app.core.designsystem.component.SecondaryButton
import com.limpyo.app.feature.auth.AuthEntry
import com.limpyo.app.feature.auth.AuthFlow
import com.limpyo.app.feature.barangay.BarangayPicker
import kotlinx.serialization.Serializable

@Serializable private data object WelcomeRoute       // ONB-01
@Serializable private data object LanguageRoute      // ONB-02
@Serializable private data object BarangayStepRoute  // ONB-04
@Serializable private data object NotificationsRoute // ONB-06
@Serializable private data object AccountRoute       // ONB-03 + AUTH-01..03

/**
 * First run: Welcome -> Language -> [account: consent + Google or email] -> Barangay -> Notifications -> app.
 * Guests skip the account step (PLAN.md decision 4: guest mode kept, with limits).
 */
@Composable
fun OnboardingFlow(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    // Which Welcome button was tapped; null = explore as guest. Survives the language-switch restart.
    var entry by rememberSaveable { mutableStateOf<AuthEntry?>(null) }
    val rise = with(LocalDensity.current) { 24.dp.roundToPx() }
    NavHost(
        navController = nav,
        startDestination = WelcomeRoute,
        enterTransition = { fadeIn(tweenMedium()) + slideInVertically(tweenMedium()) { rise } },
        exitTransition = { fadeOut(tweenShort()) },
        popEnterTransition = { fadeIn(tweenShort()) },
        popExitTransition = { fadeOut(tweenShort()) },
    ) {
        composable<WelcomeRoute> {
            WelcomeStep(onStart = { chosen ->
                entry = chosen
                nav.navigate(LanguageRoute)
            })
        }
        composable<LanguageRoute> {
            LanguageStep(viewModel, onContinue = { nav.navigate(if (entry != null) AccountRoute else BarangayStepRoute) })
        }
        composable<AccountRoute> {
            AuthFlow(
                entry = entry ?: AuthEntry.Email,
                onSignedIn = { nav.navigate(BarangayStepRoute) { popUpTo(WelcomeRoute) } },
                onBack = { nav.popBackStack() },
            )
        }
        composable<BarangayStepRoute> {
            BarangayPicker(
                header = { StepHeader(stringResource(R.string.barangay_title), stringResource(R.string.barangay_body)) },
                onDone = { nav.navigate(NotificationsRoute) },
            )
        }
        composable<NotificationsRoute> {
            NotificationsStep(onDone = { viewModel.finish(onFinished) })
        }
    }
}

@Composable
private fun StepScaffold(content: @Composable ColumnScope.() -> Unit, bottom: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = content)
        Column(
            Modifier.fillMaxWidth().padding(LimpyoSpace.screen),
            verticalArrangement = Arrangement.spacedBy(LimpyoSpace.s),
            content = bottom,
        )
    }
}

@Composable
private fun StepHeader(title: String, body: String) {
    val c = LimpyoTheme.colors
    Column(Modifier.statusBarsPadding().padding(horizontal = LimpyoSpace.screen).padding(top = LimpyoSpace.x4, bottom = LimpyoSpace.s)) {
        Text(title, style = LimpyoTheme.type.headline, color = c.ink, modifier = Modifier.semantics { heading() })
        Spacer(Modifier.height(LimpyoSpace.s))
        Text(body, style = LimpyoTheme.type.body, color = c.muted)
    }
}

/** ONB-01 */
@Composable
private fun WelcomeStep(onStart: (AuthEntry?) -> Unit) {
    val c = LimpyoTheme.colors
    val t = LimpyoTheme.type
    StepScaffold(
        content = {
            Spacer(Modifier.height(LimpyoSpace.x6 * 2))
            Column(Modifier.padding(horizontal = LimpyoSpace.screen)) {
                Text("limpyo", style = t.display, color = c.ink, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(LimpyoSpace.m))
                Text(stringResource(R.string.onb_welcome_tagline), style = t.title, color = c.accentStrong)
                Spacer(Modifier.height(LimpyoSpace.l))
                Text(stringResource(R.string.onb_welcome_body), style = t.body, color = c.muted)
            }
        },
        bottom = {
            if (isGoogleConfigured) {
                PrimaryButton(stringResource(R.string.onb_google), onClick = { onStart(AuthEntry.Google) }, modifier = Modifier.fillMaxWidth())
            }
            if (isBackendConfigured) {
                SecondaryButton(stringResource(R.string.onb_email), onClick = { onStart(AuthEntry.Email) }, modifier = Modifier.fillMaxWidth())
            }
            LimpyoTextButton(stringResource(R.string.onb_explore), onClick = { onStart(null) }, modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.disclaimer),
                style = t.caption,
                color = c.muted,
                modifier = Modifier.padding(top = LimpyoSpace.s),
            )
        },
    )
}

/** ONB-02: picking a language switches the app immediately. */
@Composable
private fun LanguageStep(viewModel: OnboardingViewModel, onContinue: () -> Unit) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    StepScaffold(
        content = {
            StepHeader(stringResource(R.string.onb_language_title), stringResource(R.string.onb_language_body))
            Spacer(Modifier.height(LimpyoSpace.l))
            AppLanguage.entries.forEach { lang ->
                RadioRow(
                    title = stringResource(
                        when (lang) {
                            AppLanguage.English -> R.string.language_en
                            AppLanguage.Filipino -> R.string.language_fil
                            AppLanguage.Bisaya -> R.string.language_ceb
                        },
                    ),
                    selected = language == lang,
                    onClick = { viewModel.setLanguage(lang) },
                )
            }
        },
        bottom = { PrimaryButton(stringResource(R.string.onb_continue), onClick = onContinue, modifier = Modifier.fillMaxWidth()) },
    )
}

/** ONB-06: explain first, then ask (Android 13+ only shows the system dialog; older phones just continue). */
@Composable
private fun NotificationsStep(onDone: () -> Unit) {
    val c = LimpyoTheme.colors
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { onDone() }
    StepScaffold(
        content = {
            Spacer(Modifier.height(LimpyoSpace.x6))
            Icon(
                painterResource(LimpyoIcons.Notifications),
                contentDescription = null,
                tint = c.accentStrong,
                modifier = Modifier.padding(horizontal = LimpyoSpace.screen).size(LimpyoSize.iconLarge),
            )
            StepHeader(stringResource(R.string.notif_title), stringResource(R.string.notif_body))
        },
        bottom = {
            PrimaryButton(
                stringResource(R.string.notif_allow),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ask.launch(Manifest.permission.POST_NOTIFICATIONS) else onDone()
                },
                modifier = Modifier.fillMaxWidth(),
            )
            LimpyoTextButton(stringResource(R.string.notif_later), onClick = onDone, modifier = Modifier.fillMaxWidth())
        },
    )
}
