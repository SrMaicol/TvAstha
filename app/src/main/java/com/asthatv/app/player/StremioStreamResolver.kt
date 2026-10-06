package com.asthatv.app.player

import com.asthatv.app.model.StremioStream

/** Converts Stremio v3 stream objects into sources understood by AsthaTV. */
object StremioStreamResolver {
    fun resolve(stream: StremioStream): PlaybackSource? {
        stream.infoHash?.takeIf { it.isNotBlank() }?.let { hash ->
            return PlaybackSource.Magnet(
                uri = magnetFromInfoHash(hash),
                fileIndex = stream.fileIdx
            )
        }
        stream.url?.takeIf { it.startsWith("magnet:?", ignoreCase = true) }?.let {
            return PlaybackSource.Magnet(it, stream.fileIdx)
        }
        stream.url?.takeIf { it.startsWith("http://") || it.startsWith("https://") }?.let {
            return PlaybackSource.Direct(it)
        }
        return null
    }

    fun magnetFromInfoHash(infoHash: String): String {
        val clean = infoHash.trim()
        return "magnet:?xt=urn:btih:$clean" +
            "&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce" +
            "&tr=udp%3A%2F%2Fopen.stealth.si%3A80%2Fannounce"
    }
}
