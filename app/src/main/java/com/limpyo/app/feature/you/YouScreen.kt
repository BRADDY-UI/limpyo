package com.limpyo.app.feature.you

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.limpyo.app.BuildConfig
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.component.EmptyState
import com.limpyo.app.core.designsystem.component.PrimaryButton
import com.limpyo.app.core.designsystem.component.SecondaryButton
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.ThemeMode

/** R-YOU. Phase 0: guest placeholder, plus a debug-only theme switch for testing. */
@Composable
fun YouScreen(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    val c = LimpyoTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LimpyoSpace.screen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyState(
            icon = LimpyoIcons.You,
            title = stringResource(R.string.you_empty_title),
            body = stringResource(R.string.you_empty_body),
        )

        if (BuildConfig.DEBUG) {
            Spacer(Modifier.height(LimpyoSpace.x4))
            Text(stringResource(R.string.debug_theme).uppercase(), style = LimpyoTheme.type.overline, color = c.muted)
            Spacer(Modifier.height(LimpyoSpace.m))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LimpyoSpace.s)) {
                listOf(
                    ThemeMode.Dark to R.string.theme_dark,
                    ThemeMode.Light to R.string.theme_light,
                    ThemeMode.System to R.string.theme_system,
                ).forEach { (mode, label) ->
                    val modifier = Modifier.weight(1f)
                    if (mode == themeMode) {
                        PrimaryButton(stringResource(label), onClick = {}, modifier = modifier)
                    } else {
                        SecondaryButton(stringResource(label), onClick = { onThemeModeChange(mode) }, modifier = modifier)
                    }
                }
            }
        }

        Spacer(Modifier.height(LimpyoSpace.x4))
        Text(
            stringResource(R.string.disclaimer),
            style = LimpyoTheme.type.caption,
            color = c.muted,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.version_label, BuildConfig.VERSION_NAME),
            style = LimpyoTheme.type.caption,
            color = c.muted,
        )
    }
}
