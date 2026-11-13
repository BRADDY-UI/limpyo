package com.limpyo.app.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoMotion
import com.limpyo.app.core.designsystem.theme.LimpyoRadius
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.spec

/**
 * Segmented control (docs/DESIGN.md): raised track, surface thumb that slides with springDefault.
 * Used for Dark / Light / System and, later, Barangays / People on the leaderboard.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LimpyoTheme.colors
    val trackShape = RoundedCornerShape(LimpyoRadius.medium)
    val thumbShape = RoundedCornerShape(LimpyoRadius.small)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(LimpyoSize.touchMin)
            .clip(trackShape)
            .background(c.raised)
            .padding(LimpyoSpace.xs),
    ) {
        val segment = maxWidth / options.size
        val thumbX by animateDpAsState(segment * selectedIndex, LimpyoMotion.springDefault.spec(), label = "thumb")
        Box(
            Modifier
                .offset { IntOffset(thumbX.roundToPx(), 0) }
                .width(segment)
                .fillMaxHeight()
                .clip(thumbShape)
                .background(c.surface)
                .border(LimpyoSize.border, c.border, thumbShape),
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight().selectableGroup()) {
            options.forEachIndexed { index, label ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(thumbShape)
                        .selectable(selected = index == selectedIndex, role = Role.RadioButton) { onSelect(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = LimpyoTheme.type.label,
                        color = if (index == selectedIndex) c.ink else c.muted,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Big radio row (onboarding language choice, Settings > Language). 56dp, whole row is the target. */
@Composable
fun RadioRow(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, subtitle: String? = null) {
    val c = LimpyoTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = LimpyoSize.touchDriver)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = LimpyoSpace.screen, vertical = LimpyoSpace.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
            Text(title, style = LimpyoTheme.type.body, color = c.ink)
            if (subtitle != null) Text(subtitle, style = LimpyoTheme.type.caption, color = c.muted)
        }
        Spacer(Modifier.width(LimpyoSpace.m))
        // Radio mark: ring, filled dot when selected (accent on dark, accentStrong on light).
        Box(
            Modifier
                .size(LimpyoSize.iconSmall)
                .clip(CircleShape)
                .border(2.dp, if (selected) c.accentStrong else c.muted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(LimpyoSize.dot + 2.dp)
                    .graphicsLayer { alpha = if (selected) 1f else 0f }
                    .clip(CircleShape)
                    .background(c.accentStrong),
            )
        }
    }
}

/** Top bar for pushed screens: back button + title. Back also works with the system gesture. */
@Composable
fun TopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = LimpyoTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(LimpyoSize.touchDriver)
            .padding(horizontal = LimpyoSpace.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(LimpyoSize.touchMin)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(LimpyoIcons.Back), contentDescription = stringResource(R.string.cd_back), tint = c.ink)
        }
        Spacer(Modifier.width(LimpyoSpace.xs))
        Text(title, style = LimpyoTheme.type.title, color = c.ink, modifier = Modifier.semantics { heading() })
    }
}

/** Small caps heading above a group of rows. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = LimpyoTheme.type.overline,
        color = LimpyoTheme.colors.muted,
        modifier = modifier
            .padding(horizontal = LimpyoSpace.screen)
            .padding(top = LimpyoSpace.xxl, bottom = LimpyoSpace.s)
            .semantics { heading() },
    )
}
