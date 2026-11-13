package com.limpyo.app.feature.you

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limpyo.app.R
import com.limpyo.app.core.auth.isBackendConfigured
import com.limpyo.app.core.designsystem.component.LimpyoCard
import com.limpyo.app.core.designsystem.component.ListRow
import com.limpyo.app.core.designsystem.component.PrimaryButton
import com.limpyo.app.core.designsystem.component.SecondaryButton
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme

/** R-YOU (basic). Level, points and badges arrive in Phase 7. */
@Composable
fun YouScreen(
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: YouViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val c = LimpyoTheme.colors
    val t = LimpyoTheme.type
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
        Text(
            stringResource(R.string.tab_you),
            style = t.headline,
            color = c.ink,
            modifier = Modifier.padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.l),
        )
        LimpyoCard(Modifier.fillMaxWidth().padding(horizontal = LimpyoSpace.screen)) {
            val email = state.email
            if (state.signedIn) {
                Text(state.name ?: email.orEmpty(), style = t.titleSmall, color = c.ink)
                if (email != null) Text(stringResource(R.string.you_signed_in_as, email), style = t.caption, color = c.muted)
            } else {
                Text(stringResource(R.string.you_guest_title), style = t.titleSmall, color = c.ink)
                Spacer(Modifier.height(LimpyoSpace.xs))
                Text(stringResource(R.string.you_guest_body), style = t.body, color = c.muted)
            }
            Spacer(Modifier.height(LimpyoSpace.s))
            Text(state.barangay ?: stringResource(R.string.you_no_barangay), style = t.label, color = c.muted)
            if (!state.signedIn && !state.loading && isBackendConfigured) {
                Spacer(Modifier.height(LimpyoSpace.l))
                PrimaryButton(stringResource(R.string.you_sign_in), onClick = onSignIn, modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(LimpyoSpace.l))
        ListRow(stringResource(R.string.you_settings), leadingIcon = LimpyoIcons.Settings, showChevron = true, onClick = onOpenSettings)
        ListRow(stringResource(R.string.you_about), leadingIcon = LimpyoIcons.Info, showChevron = true, onClick = onOpenAbout)
        if (state.signedIn) {
            Spacer(Modifier.height(LimpyoSpace.l))
            SecondaryButton(
                stringResource(R.string.you_sign_out),
                onClick = viewModel::signOut,
                modifier = Modifier.fillMaxWidth().padding(horizontal = LimpyoSpace.screen),
            )
        }
        Spacer(Modifier.height(LimpyoSpace.xxl))
        Text(
            stringResource(R.string.disclaimer),
            style = t.caption,
            color = c.muted,
            modifier = Modifier.padding(horizontal = LimpyoSpace.screen),
        )
    }
}
