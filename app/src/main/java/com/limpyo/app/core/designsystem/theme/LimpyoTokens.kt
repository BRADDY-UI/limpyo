// GENERATED from design/tokens.json by design/scripts/build-tokens.mjs. Do not edit by hand.
package com.limpyo.app.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Every color role in one theme (Night Route or Day Route). */
@Immutable
data class LimpyoColors(
    val ground: Color,
    val surface: Color,
    val raised: Color,
    val border: Color,
    val divider: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val accentStrong: Color,
    val onAccent: Color,
    val accentContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val danger: Color,
    val dangerContainer: Color,
    val info: Color,
    val infoContainer: Color,
    val scrim: Color,
    val isDark: Boolean,
)

/** Waste category colors. Always shown with an icon and a label, never color alone. */
@Immutable
data class LimpyoCategoryColors(
    val biodegradable: Color,
    val recyclable: Color,
    val residual: Color,
    val special: Color,
    val ewaste: Color,
)

enum class TokenFont { Display, Ui }

/** One text style from the type scale (sizes in sp, tracking in em). */
data class TypeSpec(val font: TokenFont, val sizeSp: Int, val lineSp: Int, val weight: Int, val trackingEm: Float)

data class SpringToken(val dampingRatio: Float, val stiffness: Float)

object LimpyoPalettes {
    val Night = LimpyoColors(
        ground = Color(0xFF0B1210),
        surface = Color(0xFF141E1A),
        raised = Color(0xFF1E2B26),
        border = Color(0xFF2C3C36),
        divider = Color(0xFF26352F),
        ink = Color(0xFFEAF5EF),
        muted = Color(0xFF9DB3A8),
        accent = Color(0xFFB6F23A),
        accentStrong = Color(0xFFB6F23A),
        onAccent = Color(0xFF0B1210),
        accentContainer = Color(0xFF25331B),
        warning = Color(0xFFFFC15E),
        warningContainer = Color(0xFF3A2E12),
        danger = Color(0xFFFF9A73),
        dangerContainer = Color(0xFF3B2018),
        info = Color(0xFF7CC4FF),
        infoContainer = Color(0xFF15283A),
        scrim = Color(0x99000000),
        isDark = true,
    )

    val Day = LimpyoColors(
        ground = Color(0xFFF4F7F5),
        surface = Color(0xFFFFFFFF),
        raised = Color(0xFFEAF0EC),
        border = Color(0xFFD3DDD8),
        divider = Color(0xFFE2E9E5),
        ink = Color(0xFF0B1210),
        muted = Color(0xFF4E6159),
        accent = Color(0xFFB6F23A),
        accentStrong = Color(0xFF3D6B00),
        onAccent = Color(0xFF0B1210),
        accentContainer = Color(0xFFE6F4D0),
        warning = Color(0xFF8A5300),
        warningContainer = Color(0xFFFBEBD2),
        danger = Color(0xFFB3261E),
        dangerContainer = Color(0xFFFBE3E0),
        info = Color(0xFF0B5CAD),
        infoContainer = Color(0xFFDDEBF9),
        scrim = Color(0x99000000),
        isDark = false,
    )

    val NightCategories = LimpyoCategoryColors(
        biodegradable = Color(0xFF4DD6C1),
        recyclable = Color(0xFF7CC4FF),
        residual = Color(0xFFC9D1CD),
        special = Color(0xFFFF9A73),
        ewaste = Color(0xFFC8A6FF),
    )

    val DayCategories = LimpyoCategoryColors(
        biodegradable = Color(0xFF00695C),
        recyclable = Color(0xFF0B5CAD),
        residual = Color(0xFF4E5A55),
        special = Color(0xFFB3261E),
        ewaste = Color(0xFF6A3FB5),
    )
}

object LimpyoTypeTokens {
    val display = TypeSpec(font = TokenFont.Display, sizeSp = 34, lineSp = 40, weight = 700, trackingEm = 0f)
    val headline = TypeSpec(font = TokenFont.Display, sizeSp = 28, lineSp = 34, weight = 700, trackingEm = 0f)
    val title = TypeSpec(font = TokenFont.Display, sizeSp = 22, lineSp = 28, weight = 700, trackingEm = 0f)
    val titleSmall = TypeSpec(font = TokenFont.Ui, sizeSp = 17, lineSp = 24, weight = 600, trackingEm = 0f)
    val body = TypeSpec(font = TokenFont.Ui, sizeSp = 16, lineSp = 24, weight = 400, trackingEm = 0f)
    val bodyStrong = TypeSpec(font = TokenFont.Ui, sizeSp = 16, lineSp = 24, weight = 600, trackingEm = 0f)
    val label = TypeSpec(font = TokenFont.Ui, sizeSp = 14, lineSp = 20, weight = 500, trackingEm = 0f)
    val caption = TypeSpec(font = TokenFont.Ui, sizeSp = 13, lineSp = 18, weight = 500, trackingEm = 0f)
    val overline = TypeSpec(font = TokenFont.Ui, sizeSp = 12, lineSp = 16, weight = 600, trackingEm = 0.08f)
}

/** Spacing scale in dp. */
object LimpyoSpace {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val x3 = 32.dp
    val x4 = 40.dp
    val x5 = 48.dp
    val x6 = 64.dp
    val screen = 20.dp
}

object LimpyoRadius {
    val small = 8.dp
    val medium = 12.dp
    val large = 20.dp
    val sheet = 28.dp
}

object LimpyoSize {
    val touchMin = 48.dp
    val icon = 24.dp
    val iconSmall = 20.dp
    val iconLarge = 48.dp
    val dot = 8.dp
    val sheetHandleWidth = 40.dp
    val sheetHandleHeight = 4.dp
    val touchDriver = 56.dp
    val pill = 48.dp
    val button = 52.dp
    val chip = 32.dp
    val textField = 56.dp
    val bottomNav = 72.dp
    val sheetPeek = 148.dp
    val border = 1.dp
    val focusRing = 2.dp
}

object LimpyoMotion {
    const val durationFast = 100
    const val durationShort = 160
    const val durationMedium = 240
    const val durationLong = 360
    val easeStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val easeEnter = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val easeExit = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    val springDefault = SpringToken(dampingRatio = 0.9f, stiffness = 400f)
    val springSheet = SpringToken(dampingRatio = 0.85f, stiffness = 300f)
    val springSnappy = SpringToken(dampingRatio = 0.7f, stiffness = 700f)
}
