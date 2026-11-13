package com.limpyo.app.core.model

data class GeoPoint(val lat: Double, val lon: Double)

/**
 * One of the 26 barangays of General Santos. [id] matches public.barangays.id in the database.
 * [polygons]: a MultiPolygon as polygons -> rings -> points (first ring is the outline, the rest are holes).
 */
data class Barangay(
    val id: Int,
    val name: String,
    val center: GeoPoint,
    val approximate: Boolean,
    val polygons: List<List<List<GeoPoint>>>,
)
