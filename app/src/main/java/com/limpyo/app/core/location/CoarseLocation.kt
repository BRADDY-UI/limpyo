package com.limpyo.app.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.limpyo.app.core.model.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * One rough location fix (about 100 m - 3 km), used only to pre-select the barangay.
 * Residents are never tracked: no updates, nothing stored, nothing sent (docs/SECURITY.md privacy).
 */
@Singleton
class CoarseLocation @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** Returns null if permission is missing, location is off, or no fix arrives within 10 s. */
    @SuppressLint("MissingPermission") // checked by hasPermission() first
    suspend fun current(): GeoPoint? {
        if (!hasPermission()) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { cont ->
                val cancel = CancellationTokenSource()
                cont.invokeOnCancellation { cancel.cancel() }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancel.token)
                    .addOnSuccessListener { loc -> cont.resume(loc?.let { GeoPoint(it.latitude, it.longitude) }) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }
    }
}
