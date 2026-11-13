package com.limpyo.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.limpyo.app.BuildConfig
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.component.EmptyState
import com.limpyo.app.core.designsystem.component.IconPill
import com.limpyo.app.core.designsystem.component.MapSheet
import com.limpyo.app.core.designsystem.component.StatusChip
import com.limpyo.app.core.designsystem.component.StatusTone
import com.limpyo.app.core.designsystem.component.TopPill
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme

/**
 * R-HOME. Phase 0 shows the final layout with no data:
 * map area (empty for now) -> floating pills -> MapSheet.
 * Debug builds put the component gallery inside the sheet; drag it up to see everything.
 */
@Composable
fun HomeScreen() {
    val c = LimpyoTheme.colors
    Box(Modifier.fillMaxSize()) {
        // Layer 1: the map (MapLibre arrives in Phase 4).
        EmptyState(
            icon = LimpyoIcons.Truck,
            title = stringResource(R.string.home_empty_title),
            body = stringResource(R.string.home_empty_body),
            modifier = Modifier.align(Alignment.Center).padding(bottom = LimpyoSpace.x6 * 2),
        )

        // Layer 2: floating pills.
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopPill(label = stringResource(R.string.home_city), onClick = {}, icon = LimpyoIcons.Place)
            IconPill(
                icon = LimpyoIcons.Notifications,
                contentDescription = stringResource(R.string.cd_notifications),
                onClick = {},
            )
        }

        // Layer 3: the sheet.
        MapSheet {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.home_no_truck), style = LimpyoTheme.type.titleSmall, color = c.ink)
                    Text(stringResource(R.string.home_no_truck_next), style = LimpyoTheme.type.caption, color = c.muted)
                }
                StatusChip(stringResource(R.string.status_scheduled), StatusTone.Neutral)
            }
            if (BuildConfig.DEBUG) {
                Spacer(Modifier.height(LimpyoSpace.x3))
                ComponentGallery()
            }
        }
    }
}
