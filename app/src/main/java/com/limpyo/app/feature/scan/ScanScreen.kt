package com.limpyo.app.feature.scan

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.feature.common.PlaceholderScreen

/** R-SCN. Built in Phase 6. */
@Composable
fun ScanScreen() = PlaceholderScreen(
    icon = LimpyoIcons.Scan,
    title = stringResource(R.string.scan_empty_title),
    body = stringResource(R.string.scan_empty_body),
)
