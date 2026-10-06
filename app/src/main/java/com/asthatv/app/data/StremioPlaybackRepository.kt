package com.asthatv.app.data

import com.asthatv.app.model.StremioStream
import com.asthatv.app.player.PlaybackSource
import com.asthatv.app.player.StremioStreamResolver
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Optimized repository fetching Stremio streams concurrently. */
class StremioPlaybackRepository {
    suspend fun sources(addon: StremioAddonClient, type: String, id: String): List<Pair<StremioStream, PlaybackSource>> =
        coroutineScope {
            val streams = addon.streams(type, id)
            streams.map { stream ->
                async {
                    StremioStreamResolver.resolve(stream)?.let { stream to it }
                }
            }.awaitAll().filterNotNull()
        }
}
