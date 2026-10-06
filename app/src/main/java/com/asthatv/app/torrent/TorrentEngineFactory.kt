package com.asthatv.app.torrent

import android.content.Context

object TorrentEngineFactory {
    fun create(context: Context): TorrentEngine = LibtorrentEngine(context)
}
