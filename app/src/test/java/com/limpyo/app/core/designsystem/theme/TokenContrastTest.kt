package com.limpyo.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * docs/DESIGN.md contrast rule: body text >= 4.5:1, large text and icons >= 3:1.
 * If someone changes a color in tokens.json and breaks readability, this test fails in CI.
 */
class TokenContrastTest {

    private fun channel(v: Float): Double {
        val c = v.toDouble()
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    private fun luminance(color: Color) =
        0.2126 * channel(color.red) + 0.7152 * channel(color.green) + 0.0722 * channel(color.blue)

    private fun ratio(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private data class Pair(val name: String, val fg: Color, val bg: Color, val min: Double)

    private fun pairs(theme: String, p: LimpyoColors, cat: LimpyoCategoryColors): List<Pair> {
        val text = 4.5
        val icon = 3.0
        return listOf(
            Pair("$theme ink on ground", p.ink, p.ground, text),
            Pair("$theme ink on surface", p.ink, p.surface, text),
            Pair("$theme ink on raised", p.ink, p.raised, text),
            Pair("$theme muted on surface", p.muted, p.surface, text),
            Pair("$theme muted on ground", p.muted, p.ground, text),
            Pair("$theme muted on raised (disabled button)", p.muted, p.raised, text),
            Pair("$theme onAccent on accent (primary button)", p.onAccent, p.accent, text),
            Pair("$theme accentStrong text on surface", p.accentStrong, p.surface, text),
            Pair("$theme accentStrong on ground (active nav)", p.accentStrong, p.ground, text),
            Pair("$theme accentStrong on accentContainer (chip)", p.accentStrong, p.accentContainer, text),
            Pair("$theme warning on warningContainer", p.warning, p.warningContainer, text),
            Pair("$theme danger on dangerContainer", p.danger, p.dangerContainer, text),
            Pair("$theme info on infoContainer", p.info, p.infoContainer, text),
            Pair("$theme warning on surface", p.warning, p.surface, text),
            Pair("$theme danger on surface", p.danger, p.surface, text),
            Pair("$theme info on surface", p.info, p.surface, text),
            Pair("$theme biodegradable icon", cat.biodegradable, p.surface, icon),
            Pair("$theme recyclable icon", cat.recyclable, p.surface, icon),
            Pair("$theme residual icon", cat.residual, p.surface, icon),
            Pair("$theme special icon", cat.special, p.surface, icon),
            Pair("$theme ewaste icon", cat.ewaste, p.surface, icon),
        )
    }

    @TestFactory
    fun allPairsMeetContrast(): List<DynamicTest> =
        (pairs("Night", LimpyoPalettes.Night, LimpyoPalettes.NightCategories) +
            pairs("Day", LimpyoPalettes.Day, LimpyoPalettes.DayCategories))
            .map { pair ->
                DynamicTest.dynamicTest(pair.name) {
                    val r = ratio(pair.fg, pair.bg)
                    assertTrue(r >= pair.min, "${pair.name}: %.2f:1 is below %.1f:1".format(r, pair.min))
                }
            }
}
