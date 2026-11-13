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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.component.LimpyoCard
import com.limpyo.app.core.designsystem.component.ListRow
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.icon.LimpyoIcons

/** R-YOU (basic). Guest card now; profile, level and badges arrive with sign-in and points. */
@Composable
fun YouScreen(onOpenSettings: () -> Unit, onOpenAbout: () -> Unit) {
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
            Text(stringResource(R.string.you_guest_title), style = t.titleSmall, color = c.ink)
            Spacer(Modifier.height(LimpyoSpace.xs))
            Text(stringResource(R.string.you_guest_body), style = t.body, color = c.muted)
        }
        Spacer(Modifier.height(LimpyoSpace.l))
        ListRow(stringResource(R.string.you_settings), leadingIcon = LimpyoIcons.Settings, showChevron = true, onClick = onOpenSettings)
        ListRow(stringResource(R.string.you_about), leadingIcon = LimpyoIcons.Info, showChevron = true, onClick = onOpenAbout)
        Spacer(Modifier.height(LimpyoSpace.xxl))
        Text(
            stringResource(R.string.disclaimer),
            style = t.caption,
            color = c.muted,
            modifier = Modifier.padding(horizontal = LimpyoSpace.screen),
        )
    }
}
