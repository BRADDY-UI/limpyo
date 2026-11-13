package com.limpyo.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.limpyo.app.R

// Both fonts are bundled "variable" fonts: one file holds every weight.
// We ask for the exact weights the type scale uses (docs/DESIGN.md section 3).
@OptIn(ExperimentalTextApi::class)
private fun inter(weight: Int) = Font(
    resId = R.font.inter,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

@OptIn(ExperimentalTextApi::class)
private fun spaceGrotesk(weight: Int) = Font(
    resId = R.font.space_grotesk,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val InterFamily = FontFamily(inter(400), inter(500), inter(600), inter(700))
val SpaceGroteskFamily = FontFamily(spaceGrotesk(500), spaceGrotesk(700))

private fun TypeSpec.toStyle() = TextStyle(
    fontFamily = if (font == TokenFont.Display) SpaceGroteskFamily else InterFamily,
    fontWeight = FontWeight(weight),
    fontSize = sizeSp.sp,
    lineHeight = lineSp.sp,
    letterSpacing = trackingEm.em,
)

@Immutable
data class LimpyoTypography(
    val display: TextStyle,
    val headline: TextStyle,
    val title: TextStyle,
    val titleSmall: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    /** Shown in caps by the caller: Text(text.uppercase(), style = overline). */
    val overline: TextStyle,
    /** Tabular figures, so numbers keep their width when they change (ranks, times). */
    val numeric: TextStyle,
) {
    fun toMaterial() = Typography(
        displayLarge = display, headlineMedium = headline, titleLarge = title,
        titleMedium = titleSmall, bodyLarge = body, bodyMedium = body,
        labelLarge = label, labelMedium = caption, labelSmall = overline,
    )

    companion object {
        val Default: LimpyoTypography = with(LimpyoTypeTokens) {
            LimpyoTypography(
                display = display.toStyle(),
                headline = headline.toStyle(),
                title = title.toStyle(),
                titleSmall = titleSmall.toStyle(),
                body = body.toStyle(),
                bodyStrong = bodyStrong.toStyle(),
                label = label.toStyle(),
                caption = caption.toStyle(),
                overline = overline.toStyle(),
                numeric = bodyStrong.toStyle().copy(fontFeatureSettings = "tnum"),
            )
        }
    }
}
