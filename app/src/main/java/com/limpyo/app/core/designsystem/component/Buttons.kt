package com.limpyo.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.limpyo.app.core.designsystem.theme.LimpyoRadius
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme

private enum class ButtonKind { Primary, Secondary, Danger }

/** The one main action on a screen. Lime fill, dark text. Light haptic tick on press. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
) = LimpyoButton(ButtonKind.Primary, text, onClick, modifier, enabled, icon)

/** Secondary action: outline only. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    @DrawableRes icon: Int? = null,
) = LimpyoButton(ButtonKind.Secondary, text, onClick, modifier, enabled, icon)

/** Destructive action (delete account, cancel shift). Always pair with a confirm dialog. */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = LimpyoButton(ButtonKind.Danger, text, onClick, modifier, enabled, null)

@Composable
private fun LimpyoButton(
    kind: ButtonKind,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    @DrawableRes icon: Int?,
) {
    val c = LimpyoTheme.colors
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(LimpyoRadius.medium)

    val (bg: Color, fg: Color, border: BorderStroke?) = when {
        !enabled -> Triple(c.raised, c.muted, null)
        kind == ButtonKind.Primary -> Triple(c.accent, c.onAccent, null)
        kind == ButtonKind.Danger -> Triple(c.dangerContainer, c.danger, null)
        else -> Triple(Color.Transparent, c.ink, BorderStroke(LimpyoSize.border, c.border))
    }

    Row(
        modifier = modifier
            .pressFeedback(interaction)
            .defaultMinSize(minWidth = LimpyoSize.touchMin)
            .height(LimpyoSize.button)
            .clip(shape)
            .background(bg)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = fg),
                enabled = enabled,
                role = Role.Button,
            ) {
                if (kind == ButtonKind.Primary) haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            }
            .padding(horizontal = LimpyoSpace.xxl),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = fg, modifier = Modifier.size(LimpyoSize.iconSmall))
            Spacer(Modifier.width(LimpyoSpace.s))
        }
        Text(
            text = text,
            style = LimpyoTheme.type.bodyStrong,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Low-emphasis action, text only. Lime on dark; dark green on light (never lime text on white). */
@Composable
fun LimpyoTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = LimpyoTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressFeedback(interaction)
            .defaultMinSize(minWidth = LimpyoSize.touchMin, minHeight = LimpyoSize.touchMin)
            .clip(RoundedCornerShape(LimpyoRadius.medium))
            .clickable(interactionSource = interaction, indication = ripple(), enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = LimpyoSpace.m),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = LimpyoTheme.type.bodyStrong, color = if (enabled) c.accentStrong else c.muted)
    }
}
