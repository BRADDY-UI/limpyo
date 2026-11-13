package com.limpyo.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.limpyo.app.core.designsystem.theme.LimpyoRadius
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme

/** Card: surface, 20 radius, 1dp border, no shadow. Depth comes from borders, not shadows. */
@Composable
fun LimpyoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LimpyoTheme.colors
    val shape = RoundedCornerShape(LimpyoRadius.large)
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.pressFeedback(interaction) else Modifier)
            .clip(shape)
            .background(c.surface)
            .border(LimpyoSize.border, c.border, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(interaction, ripple(), role = Role.Button, onClick = onClick)
                } else Modifier,
            )
            .padding(LimpyoSpace.l),
        content = content,
    )
}

/** Floating pill over the map: barangay switcher, status. 48dp tall, full radius. */
@Composable
fun TopPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
) {
    val c = LimpyoTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressFeedback(interaction)
            .height(LimpyoSize.pill)
            .clip(CircleShape)
            .background(c.surface)
            .border(LimpyoSize.border, c.border, CircleShape)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick)
            .padding(horizontal = LimpyoSpace.l),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = c.ink, modifier = Modifier.size(LimpyoSize.iconSmall))
            Spacer(Modifier.width(LimpyoSpace.s))
        }
        Text(label, style = LimpyoTheme.type.label, color = c.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Round 48x48 icon button over the map (notifications, locate me). [contentDescription] is required for TalkBack. */
@Composable
fun IconPill(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
) {
    val c = LimpyoTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressFeedback(interaction)
            .size(LimpyoSize.pill)
            .clip(CircleShape)
            .background(c.surface)
            .border(LimpyoSize.border, c.border, CircleShape)
            .clickable(interaction, ripple(), role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = contentDescription, tint = c.ink, modifier = Modifier.size(LimpyoSize.icon))
        if (showBadge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(LimpyoSpace.m)
                    .size(LimpyoSize.dot)
                    .clip(CircleShape)
                    .background(c.danger),
            )
        }
    }
}

/** Settings-style row: leading icon, title + subtitle, optional trailing text and chevron. 56-72dp. */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    @DrawableRes leadingIcon: Int? = null,
    trailingText: String? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val c = LimpyoTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = LimpyoSize.touchDriver)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(painterResource(leadingIcon), contentDescription = null, tint = c.ink, modifier = Modifier.size(LimpyoSize.icon))
            Spacer(Modifier.width(LimpyoSpace.l))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = LimpyoTheme.type.body, color = c.ink)
            if (subtitle != null) Text(subtitle, style = LimpyoTheme.type.caption, color = c.muted)
        }
        if (trailingText != null) {
            Spacer(Modifier.width(LimpyoSpace.m))
            Text(trailingText, style = LimpyoTheme.type.label, color = c.muted)
        }
        if (showChevron) {
            Spacer(Modifier.width(LimpyoSpace.s))
            Icon(painterResource(com.limpyo.app.core.designsystem.icon.LimpyoIcons.ChevronRight), null, tint = c.muted)
        }
    }
}

/** Empty state: one icon, one line of title, one line of body, an optional action. No illustrations. */
@Composable
fun EmptyState(
    @DrawableRes icon: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = LimpyoTheme.colors
    Column(
        modifier = modifier.padding(horizontal = LimpyoSpace.x3),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = c.muted, modifier = Modifier.size(LimpyoSize.iconLarge))
        Spacer(Modifier.height(LimpyoSpace.l))
        Text(title, style = LimpyoTheme.type.titleSmall, color = c.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(LimpyoSpace.xs))
        Text(body, style = LimpyoTheme.type.body, color = c.muted, textAlign = TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(LimpyoSpace.xxl))
            PrimaryButton(actionLabel, onAction)
        }
    }
}
