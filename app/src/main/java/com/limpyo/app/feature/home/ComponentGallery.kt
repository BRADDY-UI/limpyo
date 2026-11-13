package com.limpyo.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.component.CategoryChip
import com.limpyo.app.core.designsystem.component.DangerButton
import com.limpyo.app.core.designsystem.component.IconPill
import com.limpyo.app.core.designsystem.component.LimpyoCard
import com.limpyo.app.core.designsystem.component.LimpyoTextButton
import com.limpyo.app.core.designsystem.component.ListRow
import com.limpyo.app.core.designsystem.component.PrimaryButton
import com.limpyo.app.core.designsystem.component.SecondaryButton
import com.limpyo.app.core.designsystem.component.StatusChip
import com.limpyo.app.core.designsystem.component.StatusTone
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.model.WasteCategory

/** Debug-only page that shows every v0 component, so you can check both themes on the phone. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentGallery() {
    val c = LimpyoTheme.colors
    val t = LimpyoTheme.type
    val selected = remember { mutableStateOf(WasteCategory.Recyclable) }
    val gap = Arrangement.spacedBy(LimpyoSpace.s)

    Column(verticalArrangement = Arrangement.spacedBy(LimpyoSpace.xxl)) {
        Text(stringResource(R.string.debug_gallery_title).uppercase(), style = t.overline, color = c.muted)

        Column(verticalArrangement = gap) {
            Text("Display", style = t.display, color = c.ink)
            Text("Headline", style = t.headline, color = c.ink)
            Text("Title", style = t.title, color = c.ink)
            Text("Title small", style = t.titleSmall, color = c.ink)
            Text("Body text. Truck 2 is about 12 min away.", style = t.body, color = c.ink)
            Text("Body strong", style = t.bodyStrong, color = c.ink)
            Text("Muted caption · 6:30 AM", style = t.caption, color = c.muted)
            Text("0123456789", style = t.numeric, color = c.ink)
        }

        Column(verticalArrangement = gap) {
            PrimaryButton(stringResource(R.string.sample_primary), onClick = {}, modifier = Modifier.fillMaxWidth())
            SecondaryButton(stringResource(R.string.sample_secondary), onClick = {}, modifier = Modifier.fillMaxWidth())
            PrimaryButton(stringResource(R.string.sample_disabled), onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
            DangerButton(stringResource(R.string.sample_danger), onClick = {}, modifier = Modifier.fillMaxWidth())
            LimpyoTextButton(stringResource(R.string.sample_text), onClick = {})
        }

        FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
            StatusChip(stringResource(R.string.status_scheduled), StatusTone.Neutral)
            StatusChip(stringResource(R.string.status_on_the_way), StatusTone.Success)
            StatusChip(stringResource(R.string.status_collected), StatusTone.Success, showDot = false)
            StatusChip(stringResource(R.string.status_delayed), StatusTone.Warning)
            StatusChip(stringResource(R.string.status_missed), StatusTone.Danger)
            StatusChip(stringResource(R.string.status_info), StatusTone.Info)
        }

        FlowRow(horizontalArrangement = gap, verticalArrangement = gap) {
            WasteCategory.entries.forEach { category ->
                CategoryChip(
                    category = category,
                    selected = selected.value == category,
                    onClick = { selected.value = category },
                )
            }
        }

        LimpyoCard(Modifier.fillMaxWidth(), onClick = {}) {
            Text(stringResource(R.string.sample_card_title).uppercase(), style = t.overline, color = c.muted)
            Spacer(Modifier.height(LimpyoSpace.s))
            Text(stringResource(R.string.sample_card_body), style = t.bodyStrong, color = c.ink)
        }

        LimpyoCard(Modifier.fillMaxWidth()) {
            ListRow(
                title = stringResource(R.string.sample_row_title),
                subtitle = stringResource(R.string.sample_row_subtitle),
                leadingIcon = LimpyoIcons.Notifications,
                trailingText = stringResource(R.string.sample_row_value),
                showChevron = true,
                onClick = {},
            )
        }

        IconPill(LimpyoIcons.LocateMe, stringResource(R.string.cd_locate_me), onClick = {}, showBadge = true)
    }
}
