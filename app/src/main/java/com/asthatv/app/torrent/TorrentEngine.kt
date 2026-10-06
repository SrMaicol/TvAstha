package com.asthatv.app.torrent

import kotlinx.coroutines.flow.StateFlow

interface TorrentEngine {
    val state: StateFlow<TorrentSessionState>

    /** Adds a magnet, obtains metadata and returns the video files available in it. */
    suspend fun inspectMagnet(magnetUri: String): List<TorrentVideoFile>

    /** Starts/continues the selected file and returns a local HTTP URL consumable by Media3. */
    suspend fun streamMagnet(magnetUri: String, fileIndex: Int? = null): String

    /** Re-prioritizes pieces around the byte position after a player seek. */
    fun seek(bytePosition: Long)

    fun stop()
    fun shutdown()
}
