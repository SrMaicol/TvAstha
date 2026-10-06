package com.asthatv.app.player

import com.asthatv.app.torrent.TorrentEngine

/** Resolves direct and P2P sources to a Media3-consumable URI. */
class PlaybackCoordinator(private val torrentEngine: TorrentEngine) {
    suspend fun prepare(source: PlaybackSource): String = when (source) {
        is PlaybackSource.Direct -> source.url
        is PlaybackSource.Magnet -> torrentEngine.streamMagnet(source.uri, source.fileIndex)
        is PlaybackSource.Torrent -> torrentEngine.streamMagnet(
            StremioStreamResolver.magnetFromInfoHash(source.infoHash), source.fileIndex
        )
    }

    fun stop() = torrentEngine.stop()
}
