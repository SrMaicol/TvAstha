package com.asthatv.app.torrent

data class TorrentVideoFile(
    val index: Int,
    val name: String,
    val size: Long,
    val path: String
)

data class TorrentSessionState(
    val name: String = "",
    val progress: Float = 0f,
    val downloadRate: Long = 0L,
    val uploadRate: Long = 0L,
    val peers: Int = 0,
    val seeds: Int = 0,
    val buffering: Boolean = true,
    val error: String? = null
)
