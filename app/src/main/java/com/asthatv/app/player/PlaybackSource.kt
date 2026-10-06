package com.asthatv.app.player

sealed interface PlaybackSource {
    data class Direct(val url: String) : PlaybackSource
    data class Torrent(val infoHash: String, val fileIndex: Int? = null) : PlaybackSource
    data class Magnet(val uri: String, val fileIndex: Int? = null) : PlaybackSource
}
