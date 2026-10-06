package com.asthatv.app.torrent

import android.content.Context
import com.frostwire.jlibtorrent.Priority
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.TorrentFlags
import com.frostwire.jlibtorrent.TorrentInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Native BitTorrent backend powered by libtorrent 2.x (jlibtorrent JNI).
 *
 * Network/torrent work never runs on Compose's UI thread. Metadata is resolved first,
 * only the selected video file is given download priority, and sequential-download is
 * enabled for low-latency playback. The local HTTP bridge serves the growing file to Media3.
 */
class LibtorrentEngine(context: Context) : TorrentEngine {
    private val appContext = context.applicationContext
    private val session = SessionManager(false)
    private val root = File(appContext.cacheDir, "astha-torrents").apply { mkdirs() }
    private val metadataCache = ConcurrentHashMap<String, TorrentInfo>()
    private val _state = MutableStateFlow(TorrentSessionState())
    override val state: StateFlow<TorrentSessionState> = _state

    private var bridge: GrowingFileHttpServer? = null
    private var currentInfo: TorrentInfo? = null
    private var currentFile: TorrentVideoFile? = null

    init {
        // SessionManager starts libtorrent's alert/network worker threads. DHT/PEX are on by default.
        session.start()
    }

    override suspend fun inspectMagnet(magnetUri: String): List<TorrentVideoFile> = withContext(Dispatchers.IO) {
        val info = resolveMetadata(magnetUri)
        videoFiles(info)
    }

    override suspend fun streamMagnet(magnetUri: String, fileIndex: Int?): String = withContext(Dispatchers.IO) {
        val info = resolveMetadata(magnetUri)
        val videos = videoFiles(info)
        require(videos.isNotEmpty()) { "The torrent does not contain a supported video file" }

        val selected = fileIndex?.let { wanted -> videos.firstOrNull { it.index == wanted } }
            ?: videos.maxByOrNull { it.size }!!

        val priorities = Priority.array(Priority.IGNORE, info.numFiles())
        priorities[selected.index] = Priority.SEVEN

        // Sequential mode is useful for playback; file priority prevents downloading extras.
        session.download(
            info,
            root,
            null,
            priorities,
            null,
            TorrentFlags.SEQUENTIAL_DOWNLOAD
        )

        currentInfo = info
        currentFile = selected
        val mediaFile = File(root, selected.path)

        bridge?.close()
        bridge = GrowingFileHttpServer(
            file = mediaFile,
            expectedLength = selected.size,
            onRangeRequested = { start -> seek(start) }
        ).also { it.start() }

        _state.value = TorrentSessionState(name = selected.name, buffering = true)
        bridge!!.url
    }

    override fun seek(bytePosition: Long) {
        // The HTTP range request drives playback position. Sequential mode resumes naturally
        // from downloaded data; a future advanced scheduler can additionally set piece deadlines.
        _state.value = _state.value.copy(buffering = true)
    }

    override fun stop() {
        bridge?.close()
        bridge = null
        currentInfo = null
        currentFile = null
        _state.value = TorrentSessionState()
    }

    override fun shutdown() {
        stop()
        // SessionManager.stop() may block; callers should invoke shutdown away from the UI thread.
        session.stop()
    }

    private fun resolveMetadata(magnetUri: String): TorrentInfo {
        metadataCache[magnetUri]?.let { return it }
        val bytes = session.fetchMagnet(magnetUri, 35, root)
            ?: error("Could not obtain torrent metadata. Check peers/DHT or the magnet URI.")
        return TorrentInfo(bytes).also { metadataCache[magnetUri] = it }
    }

    private fun videoFiles(info: TorrentInfo): List<TorrentVideoFile> {
        val storage = info.files()
        val extensions = setOf("mkv", "mp4", "webm", "avi", "mov", "m4v", "ts", "m2ts")
        return (0 until info.numFiles()).mapNotNull { index ->
            val path = storage.filePath(index)
            val ext = path.substringAfterLast('.', "").lowercase()
            if (ext !in extensions) null else TorrentVideoFile(
                index = index,
                name = File(path).name,
                size = storage.fileSize(index),
                path = path
            )
        }.sortedByDescending { it.size }
    }
}
