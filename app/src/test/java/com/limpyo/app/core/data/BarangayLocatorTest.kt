package com.limpyo.app.core.data

import com.limpyo.app.core.model.GeoPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.io.File

/** Uses the real bundled file, so a bad regeneration of barangays.json fails here. */
class BarangayLocatorTest {
    private val barangays = BarangayParser.parse(File("src/main/assets/barangays.json").readText())
    private val locator = BarangayLocator(barangays)

    @Test
    fun `all 26 barangays load in id order`() {
        assertEquals(26, barangays.size)
        assertEquals((1..26).toList(), barangays.map { it.id })
        assertEquals("Apopong", barangays.first().name)
        assertEquals("Upper Labay", barangays.last().name)
    }

    @Test
    fun `every barangay center is inside its own area`() {
        for (b in barangays) {
            assertEquals(b.name, locator.find(b.center)?.name, "center of ${b.name}")
        }
    }

    @Test
    fun `a point in Lagao is Lagao`() {
        assertEquals("Lagao", locator.find(GeoPoint(lat = 6.1281, lon = 125.1904))?.name)
    }

    @Test
    fun `Manila is not in General Santos`() {
        assertNull(locator.find(GeoPoint(lat = 14.5995, lon = 120.9842)))
    }

    @Test
    fun `a point just outside the city edge but near a center falls back to the nearest barangay`() {
        // Slightly off the outline near Siguel's coast, still within the 3 km fallback.
        val near = barangays.first { it.name == "Siguel" }.center.let { GeoPoint(it.lat - 0.001, it.lon) }
        assertEquals("Siguel", locator.find(near)?.name)
    }
}
