package com.limpyo.app.feature.auth

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.limpyo.app.R
import com.limpyo.app.core.auth.AuthError
import com.limpyo.app.core.auth.AuthState
import com.limpyo.app.core.auth.isGoogleConfigured
import com.limpyo.app.core.designsystem.component.LimpyoTextButton
import com.limpyo.app.core.designsystem.component.LimpyoTextField
import com.limpyo.app.core.designsystem.component.PrimaryButton
import com.limpyo.app.core.designsystem.component.SecondaryButton
import com.limpyo.app.core.designsystem.component.StatusChip
import com.limpyo.app.core.designsystem.component.StatusTone
import com.limpyo.app.core.designsystem.component.TopBar
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.tweenShort
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.serialization.Serializable

/** Until the site is on Cloudflare Pages, the privacy policy is read on GitHub. */
private const val PRIVACY_URL = "https://github.com/BRADDY-UI/limpyo/blob/main/docs/PRIVACY.md"

enum class AuthEntry { Google, Email }

@Serializable private data object ConsentRoute // ONB-03
@Serializable private data object SignInRoute  // AUTH-01
@Serializable private data object SignUpRoute  // AUTH-02
@Serializable private data object VerifyRoute  // AUTH-03
@Serializable private data object ResetRoute   // AUTH-04

/**
 * Account flow. Consent (ONB-03) always comes first: nobody gets an account before agreeing.
 * Google: consent -> Google sheet -> done. Email: consent -> sign in (or sign up -> check inbox) -> done.
 * [onSignedIn] runs once a session exists (including when the email link is opened later).
 */
@Composable
fun AuthFlow(entry: AuthEntry, onSignedIn: () -> Unit, onBack: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    val auth by viewModel.authState.collectAsStateWithLifecycle()
    LaunchedEffect(auth) { if (auth is AuthState.SignedIn) onSignedIn() }

    val google = viewModel.supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> viewModel.onGoogleSignedIn(onSignedIn)
                is NativeSignInResult.ClosedByUser -> Unit
                else -> viewModel.onGoogleFailed()
            }
        },
    )

    NavHost(
        navController = nav,
        startDestination = ConsentRoute,
        enterTransition = { fadeIn(tweenShort()) },
        exitTransition = { fadeOut(tweenShort()) },
    ) {
        composable<ConsentRoute> {
            ConsentScreen(
                onBack = onBack,
                onAgree = {
                    if (entry == AuthEntry.Google && isGoogleConfigured) google.startFlow() else nav.navigate(SignInRoute)
                },
                googleError = viewModel.ui.collectAsStateWithLifecycle().value.error != null && entry == AuthEntry.Google,
            )
        }
        composable<SignInRoute> {
            SignInScreen(
                viewModel,
                onBack = { nav.popBackStack() },
                onCreate = { nav.navigate(SignUpRoute) },
                onForgot = { nav.navigate(ResetRoute) },
            )
        }
        composable<SignUpRoute> {
            SignUpScreen(viewModel, onBack = { nav.popBackStack() }, onSent = { nav.navigate(VerifyRoute) })
        }
        composable<VerifyRoute> { VerifyScreen(viewModel, onBack = { nav.popBackStack() }) }
        composable<ResetRoute> { ResetScreen(viewModel, onBack = { nav.popBackStack() }) }
    }
}

@Composable
private fun FormScaffold(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().imePadding()) {
        TopBar(title, onBack)
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.s),
            verticalArrangement = Arrangement.spacedBy(LimpyoSpace.l),
            content = content,
        )
    }
}

@Composable
private fun ErrorText(error: AuthError?) {
    if (error == null) return
    Text(
        stringResource(
            when (error) {
                AuthError.InvalidCredentials -> R.string.auth_err_invalid
                AuthError.EmailNotConfirmed -> R.string.auth_err_unconfirmed
                AuthError.EmailTaken -> R.string.auth_err_taken
                AuthError.WeakPassword -> R.string.auth_err_weak
                AuthError.RateLimited -> R.string.auth_err_rate
                AuthError.Network -> R.string.auth_err_network
                AuthError.NotConfigured -> R.string.auth_err_not_configured
                AuthError.Unknown -> R.string.auth_err_unknown
            },
        ),
        style = LimpyoTheme.type.body,
        color = LimpyoTheme.colors.danger,
    )
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String, helper: String?, imeAction: ImeAction) {
    var visible by rememberSaveable { mutableStateOf(false) }
    LimpyoTextField(
        value = value,
        onValueChange = onChange,
        label = label,
        helper = helper,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
    )
    LimpyoTextButton(
        stringResource(if (visible) R.string.auth_hide_password else R.string.auth_show_password),
        onClick = { visible = !visible },
    )
}

/** ONB-03: five plain bullets, link to the full policy, Agree. */
@Composable
private fun ConsentScreen(onBack: () -> Unit, onAgree: () -> Unit, googleError: Boolean) {
    val c = LimpyoTheme.colors
    val context = LocalContext.current
    FormScaffold(stringResource(R.string.consent_title), onBack) {
        listOf(R.string.consent_1, R.string.consent_2, R.string.consent_3, R.string.consent_4, R.string.consent_5).forEach {
            Row {
                Text("•", style = LimpyoTheme.type.bodyStrong, color = c.accentStrong)
                Spacer(Modifier.width(LimpyoSpace.m))
                Text(stringResource(it), style = LimpyoTheme.type.body, color = c.ink)
            }
        }
        LimpyoTextButton(stringResource(R.string.consent_policy), onClick = {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_URL.toUri()))
            } catch (_: ActivityNotFoundException) {
            }
        })
        if (googleError) Text(stringResource(R.string.auth_err_google), style = LimpyoTheme.type.body, color = c.danger)
        PrimaryButton(stringResource(R.string.consent_agree), onClick = onAgree, modifier = Modifier.fillMaxWidth())
    }
}

/** AUTH-01 */
@Composable
private fun SignInScreen(viewModel: AuthViewModel, onBack: () -> Unit, onCreate: () -> Unit, onForgot: () -> Unit) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    FormScaffold(stringResource(R.string.auth_sign_in), onBack) {
        LimpyoTextField(
            email, { email = it; viewModel.clearError() }, stringResource(R.string.auth_email),
            keyboardType = KeyboardType.Email, imeAction = ImeAction.Next,
        )
        PasswordField(password, { password = it; viewModel.clearError() }, stringResource(R.string.auth_password), null, ImeAction.Done)
        ErrorText(ui.error)
        PrimaryButton(
            stringResource(R.string.auth_sign_in),
            onClick = { viewModel.signIn(email, password) {} },
            enabled = !ui.busy && isValidEmail(email) && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        )
        LimpyoTextButton(stringResource(R.string.auth_forgot), onClick = onForgot)
        SecondaryButton(stringResource(R.string.auth_no_account), onClick = onCreate, modifier = Modifier.fillMaxWidth())
    }
}

/** AUTH-02 */
@Composable
private fun SignUpScreen(viewModel: AuthViewModel, onBack: () -> Unit, onSent: () -> Unit) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val strength = passwordStrength(password)
    FormScaffold(stringResource(R.string.auth_sign_up), onBack) {
        LimpyoTextField(
            name, { name = it.take(30) }, stringResource(R.string.auth_display_name),
            helper = stringResource(R.string.auth_display_name_help),
            error = if (name.isNotEmpty() && !isValidName(name)) stringResource(R.string.auth_err_name) else null,
            imeAction = ImeAction.Next,
        )
        LimpyoTextField(
            email, { email = it; viewModel.clearError() }, stringResource(R.string.auth_email),
            error = if (email.isNotEmpty() && !isValidEmail(email)) stringResource(R.string.auth_err_email) else null,
            keyboardType = KeyboardType.Email, imeAction = ImeAction.Next,
        )
        PasswordField(password, { password = it }, stringResource(R.string.auth_password), stringResource(R.string.auth_password_help), ImeAction.Done)
        if (password.isNotEmpty()) {
            StatusChip(
                stringResource(
                    when (strength) {
                        Strength.Weak -> R.string.auth_strength_weak
                        Strength.Okay -> R.string.auth_strength_ok
                        Strength.Strong -> R.string.auth_strength_strong
                    },
                ),
                when (strength) {
                    Strength.Weak -> StatusTone.Danger
                    Strength.Okay -> StatusTone.Warning
                    Strength.Strong -> StatusTone.Success
                },
            )
        }
        ErrorText(ui.error)
        PrimaryButton(
            stringResource(R.string.auth_sign_up),
            onClick = { viewModel.signUp(name, email, password, onSent) },
            enabled = !ui.busy && isValidName(name) && isValidEmail(email) && password.length >= MIN_PASSWORD,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** AUTH-03: the session arrives when the email link opens the app; AuthFlow then finishes by itself. */
@Composable
private fun VerifyScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    FormScaffold(stringResource(R.string.auth_verify_title), onBack) {
        Text(stringResource(R.string.auth_verify_body, ui.pendingEmail), style = LimpyoTheme.type.body, color = LimpyoTheme.colors.ink)
        ErrorText(ui.error)
        PrimaryButton(stringResource(R.string.auth_open_mail), modifier = Modifier.fillMaxWidth(), onClick = {
            try {
                context.startActivity(
                    Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            } catch (_: ActivityNotFoundException) {
            }
        })
        SecondaryButton(
            if (ui.resendWait > 0) stringResource(R.string.auth_resend_wait, ui.resendWait) else stringResource(R.string.auth_resend),
            onClick = viewModel::resend,
            enabled = ui.resendWait == 0 && !ui.busy,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** AUTH-04 (request). The new-password step opens from the email link: see NewPasswordScreen. */
@Composable
private fun ResetScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    FormScaffold(stringResource(R.string.auth_reset_title), onBack) {
        Text(stringResource(R.string.auth_reset_body), style = LimpyoTheme.type.body, color = LimpyoTheme.colors.muted)
        LimpyoTextField(email, { email = it }, stringResource(R.string.auth_email), keyboardType = KeyboardType.Email)
        ErrorText(ui.error)
        if (ui.resetSent) Text(stringResource(R.string.auth_reset_sent), style = LimpyoTheme.type.body, color = LimpyoTheme.colors.accentStrong)
        PrimaryButton(
            stringResource(R.string.auth_reset_send),
            onClick = { viewModel.sendReset(email) },
            enabled = !ui.busy && isValidEmail(email) && !ui.resetSent,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** AUTH-04 (second half): shown over the app after a reset link signed the user in. */
@Composable
fun NewPasswordScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var password by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().imePadding()) {
        Spacer(Modifier.height(LimpyoSpace.x4))
        Column(
            Modifier.padding(LimpyoSpace.screen).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(LimpyoSpace.l),
        ) {
            Text(stringResource(R.string.auth_new_password_title), style = LimpyoTheme.type.headline, color = LimpyoTheme.colors.ink)
            PasswordField(password, { password = it }, stringResource(R.string.auth_password), stringResource(R.string.auth_password_help), ImeAction.Done)
            ErrorText(ui.error)
            PrimaryButton(
                stringResource(R.string.auth_save_password),
                onClick = { viewModel.setNewPassword(password) },
                enabled = !ui.busy && password.length >= MIN_PASSWORD,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
