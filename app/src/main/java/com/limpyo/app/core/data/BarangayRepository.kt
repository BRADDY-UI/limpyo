package com.limpyo.app.core.data

import android.content.Context
import com.limpyo.app.core.model.Barangay
import com.limpyo.app.core.model.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The 26 barangays, read once from the bundled file (works offline, no account needed).
 * Phase 2 adds a refresh from Supabase when the server's content version changes.
 */
@Singleton
class BarangayRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val mutex = Mutex()
    private var cache: List<Barangay>? = null

    suspend fun all(): List<Barangay> = mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            context.assets.open("barangays.json").bufferedReader().use { BarangayParser.parse(it.readText()) }
        }.also { cache = it }
    }

    suspend fun byId(id: Int): Barangay? = all().firstOrNull { it.id == id }

    suspend fun at(point: GeoPoint): Barangay? = BarangayLocator(all()).find(point)
}
