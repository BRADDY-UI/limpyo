package com.limpyo.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limpyo.app.R
import com.limpyo.app.core.data.AppLanguage
import com.limpyo.app.core.designsystem.component.ListRow
import com.limpyo.app.core.designsystem.component.RadioRow
import com.limpyo.app.core.designsystem.component.SectionLabel
import com.limpyo.app.core.designsystem.component.SegmentedControl
import com.limpyo.app.core.designsystem.component.TopBar
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.ThemeMode

/** R-SET. Phase 1: language, theme, about. Notifications and privacy rows arrive with their features. */
@Composable
fun SettingsScreen(onBack: () -> Unit, onOpenAbout: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(state, onBack, onOpenAbout, viewModel::setLanguage, viewModel::setTheme)
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onTheme: (ThemeMode) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(stringResource(R.string.settings_title), onBack)

        SectionLabel(stringResource(R.string.settings_language))
        AppLanguage.entries.forEach { lang ->
            RadioRow(
                title = stringResource(
                    when (lang) {
                        AppLanguage.English -> R.string.language_en
                        AppLanguage.Filipino -> R.string.language_fil
                        AppLanguage.Bisaya -> R.string.language_ceb
                    },
                ),
                selected = state.language == lang,
                onClick = { onLanguage(lang) },
            )
        }

        SectionLabel(stringResource(R.string.settings_theme))
        val modes = ThemeMode.entries
        SegmentedControl(
            options = modes.map {
                stringResource(
                    when (it) {
                        ThemeMode.Dark -> R.string.theme_dark
                        ThemeMode.Light -> R.string.theme_light
                        ThemeMode.System -> R.string.theme_system
                    },
                )
            },
            selectedIndex = modes.indexOf(state.themeMode),
            onSelect = { onTheme(modes[it]) },
            modifier = Modifier.padding(horizontal = LimpyoSpace.screen),
        )

        Spacer(Modifier.height(LimpyoSpace.xxl))
        ListRow(
            title = stringResource(R.string.settings_about),
            leadingIcon = LimpyoIcons.Info,
            showChevron = true,
            onClick = onOpenAbout,
        )
        Spacer(Modifier.height(LimpyoSpace.x3))
    }
}
