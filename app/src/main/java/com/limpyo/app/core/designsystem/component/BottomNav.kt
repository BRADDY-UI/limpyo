package com.limpyo.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.tweenShort

data class BottomNavItem(
    val label: String,
    @param:DrawableRes val icon: Int,
    @param:DrawableRes val selectedIcon: Int,
)

/**
 * Bottom navigation: 72dp + the system gesture bar, ground background, 1dp top divider.
 * Active item: filled icon + accent (dark) or accentStrong (light).
 */
@Composable
fun LimpyoBottomNav(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LimpyoTheme.colors
    Column(modifier.fillMaxWidth().background(c.ground)) {
        Spacer(Modifier.fillMaxWidth().height(LimpyoSize.border).background(c.divider))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(LimpyoSize.bottomNav)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedIndex
                val tint by animateColorAsState(
                    if (selected) c.accentStrong else c.muted,
                    tweenShort(),
                    label = "navTint",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            onClick = { onSelect(index) },
                            role = Role.Tab,
                            interactionSource = null,
                            indication = ripple(bounded = false, radius = LimpyoSize.touchDriver / 2),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painterResource(if (selected) item.selectedIcon else item.icon),
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(LimpyoSize.icon),
                        )
                        Spacer(Modifier.height(LimpyoSpace.xs))
                        Text(item.label, style = LimpyoTheme.type.caption, color = tint)
                    }
                }
            }
        }
    }
}
