package com.limpyo.app.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.limpyo.app.core.designsystem.theme.tweenFast

/**
 * Press feedback from docs/DESIGN.md: scale to 0.98 and content alpha 0.85 over 100 ms.
 * Done in graphicsLayer, so it only repaints and never re-measures (smooth at 90 fps).
 */
fun Modifier.pressFeedback(interactionSource: InteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, tweenFast(), label = "pressScale")
    val alpha by animateFloatAsState(if (pressed) 0.85f else 1f, tweenFast(), label = "pressAlpha")
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}
