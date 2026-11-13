package com.limpyo.app.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.icon.color
import com.limpyo.app.core.designsystem.icon.label
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.tweenShort
import com.limpyo.app.core.model.WasteCategory

enum class StatusTone { Neutral, Success, Warning, Danger, Info }

/** Status chip (Scheduled, On the way, Collected, Delayed...). The color never carries the meaning alone: the label does. */
@Composable
fun StatusChip(label: String, tone: StatusTone, modifier: Modifier = Modifier, showDot: Boolean = true) {
    val c = LimpyoTheme.colors
    val (container, content) = when (tone) {
        StatusTone.Neutral -> c.raised to c.ink
        StatusTone.Success -> c.accentContainer to c.accentStrong
        StatusTone.Warning -> c.warningContainer to c.warning
        StatusTone.Danger -> c.dangerContainer to c.danger
        StatusTone.Info -> c.infoContainer to c.info
    }
    val bg by animateColorAsState(container, tweenShort(), label = "chipBg")
    val fg by animateColorAsState(content, tweenShort(), label = "chipFg")
    Row(
        modifier = modifier
            .height(LimpyoSize.chip)
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = LimpyoSpace.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDot) {
            Spacer(Modifier.size(LimpyoSize.dot).clip(CircleShape).background(fg))
            Spacer(Modifier.width(LimpyoSpace.s))
        }
        Text(label, style = LimpyoTheme.type.label, color = fg)
    }
}

/** Waste category chip: category icon + label. Selectable variant shows a check (report and scan flows). */
@Composable
fun CategoryChip(
    category: WasteCategory,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = LimpyoTheme.colors
    val tint = category.color()
    val border by animateColorAsState(if (selected) tint else c.border, tweenShort(), label = "catBorder")
    Row(
        modifier = modifier
            .height(LimpyoSize.chip)
            .clip(CircleShape)
            .background(if (selected) c.raised else c.surface)
            .border(LimpyoSize.border, border, CircleShape)
            .then(
                if (onClick != null) Modifier.selectable(selected = selected, role = Role.Checkbox, onClick = onClick) else Modifier,
            )
            .padding(horizontal = LimpyoSpace.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(if (selected) LimpyoIcons.Check else LimpyoIcons.category(category)),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(LimpyoSize.iconSmall),
        )
        Spacer(Modifier.width(LimpyoSpace.s))
        Text(category.label(), style = LimpyoTheme.type.label, color = c.ink)
    }
}
