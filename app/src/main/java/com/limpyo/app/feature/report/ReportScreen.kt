package com.limpyo.app.feature.report

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.feature.common.PlaceholderScreen

/** R-REP. Built in Phase 5. */
@Composable
fun ReportScreen() = PlaceholderScreen(
    icon = LimpyoIcons.Report,
    title = stringResource(R.string.report_empty_title),
    body = stringResource(R.string.report_empty_body),
)
