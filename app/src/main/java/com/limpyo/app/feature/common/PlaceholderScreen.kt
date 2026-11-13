package com.limpyo.app.feature.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.limpyo.app.core.designsystem.component.EmptyState

/** Phase 0 stand-in for a tab that gets built in a later phase. */
@Composable
fun PlaceholderScreen(@DrawableRes icon: Int, title: String, body: String) {
    Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
        EmptyState(icon = icon, title = title, body = body)
    }
}
