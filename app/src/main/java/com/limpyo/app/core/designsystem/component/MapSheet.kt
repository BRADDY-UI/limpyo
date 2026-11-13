package com.limpyo.app.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import com.limpyo.app.core.designsystem.theme.LimpyoMotion
import com.limpyo.app.core.designsystem.theme.LimpyoRadius
import com.limpyo.app.core.designsystem.theme.LimpyoSize
import com.limpyo.app.core.designsystem.theme.LimpyoSpace
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.spec
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class SheetStop { Peek, Half, Full }

/**
 * The sheet that slides over the map (docs/DESIGN.md: MapSheet).
 * Three snap points: peek (148dp), half (50%), full. It follows your finger, then settles
 * with the springSheet spring. When full, the content scrolls inside; dragging down at the
 * top of the content pulls the sheet down again.
 *
 * Plain words: [offsetPx] is how far the sheet's top edge is from the top of the screen.
 * Smaller offset = sheet is higher.
 */
@Composable
fun MapSheet(
    modifier: Modifier = Modifier,
    initialStop: SheetStop = SheetStop.Peek,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LimpyoTheme.colors
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    BoxWithConstraints(modifier.fillMaxSize()) {
        val heightPx = constraints.maxHeight.toFloat()
        val peekPx = with(density) { LimpyoSize.sheetPeek.toPx() }
        // "Full" stops just under the status bar, so the clock and icons never cover content.
        val topPx = WindowInsets.statusBars.getTop(density).toFloat()
        val stops = remember(heightPx, peekPx, topPx) {
            mapOf(
                SheetStop.Full to topPx,
                SheetStop.Half to heightPx * 0.5f,
                SheetStop.Peek to (heightPx - peekPx).coerceAtLeast(0f),
            )
        }
        val minOffset = stops.getValue(SheetStop.Full)
        val maxOffset = stops.getValue(SheetStop.Peek)
        val offsetPx = remember { Animatable(stops.getValue(initialStop)) }
        LaunchedEffect(minOffset, maxOffset) { offsetPx.updateBounds(minOffset, maxOffset) }

        // Pick a stop from where the sheet is and how fast it was flung.
        fun settle(velocity: Float) {
            val current = offsetPx.value
            val target = when {
                velocity < -1200f -> stops.values.filter { it < current - 1f }.maxOrNull() ?: minOffset
                velocity > 1200f -> stops.values.filter { it > current + 1f }.minOrNull() ?: maxOffset
                else -> stops.values.minBy { abs(it - current) }
            }
            scope.launch {
                offsetPx.animateTo(target, LimpyoMotion.springSheet.spec(), initialVelocity = velocity)
            }
            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }

        val dragState = rememberDraggableState { delta ->
            scope.launch { offsetPx.snapTo(offsetPx.value + delta) }
        }

        // Lets the inner scroll and the sheet drag hand off to each other.
        val nested = remember(stops) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // Finger moving up while the sheet isn't full: move the sheet first.
                    return if (available.y < 0 && offsetPx.value > minOffset) {
                        val before = offsetPx.value
                        val after = (before + available.y).coerceAtLeast(minOffset)
                        scope.launch { offsetPx.snapTo(after) }
                        Offset(0f, after - before)
                    } else Offset.Zero
                }

                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    // Content is at its top and the finger keeps moving down: pull the sheet down.
                    return if (available.y > 0 && source == NestedScrollSource.UserInput) {
                        val before = offsetPx.value
                        val after = (before + available.y).coerceAtMost(maxOffset)
                        scope.launch { offsetPx.snapTo(after) }
                        Offset(0f, after - before)
                    } else Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    val notAtStop = stops.values.none { abs(it - offsetPx.value) < 1f }
                    return if (notAtStop) {
                        settle(available.y)
                        available
                    } else Velocity.Zero
                }
            }
        }

        val shape = RoundedCornerShape(topStart = LimpyoRadius.sheet, topEnd = LimpyoRadius.sheet)
        Column(
            Modifier
                .offset { IntOffset(0, offsetPx.value.roundToInt()) }
                .fillMaxSize()
                .clip(shape)
                .background(c.surface)
                .border(LimpyoSize.border, c.border, shape)
                .nestedScroll(nested)
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity -> settle(velocity) },
                ),
        ) {
            Box(Modifier.fillMaxWidth().padding(vertical = LimpyoSpace.m), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(LimpyoSize.sheetHandleWidth, LimpyoSize.sheetHandleHeight)
                        .clip(CircleShape)
                        .background(c.border),
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LimpyoSpace.screen)
                    .padding(bottom = LimpyoSpace.x3),
                content = content,
            )
        }
    }
}
