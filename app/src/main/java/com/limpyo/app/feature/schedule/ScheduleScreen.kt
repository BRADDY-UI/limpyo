package com.limpyo.app.feature.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.feature.common.PlaceholderScreen

/** R-SCH. Built in Phase 2. */
@Composable
fun ScheduleScreen() = PlaceholderScreen(
    icon = LimpyoIcons.Schedule,
    title = stringResource(R.string.schedule_empty_title),
    body = stringResource(R.string.schedule_empty_body),
)
