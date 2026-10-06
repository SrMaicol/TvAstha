package com.asthatv.app.torrent

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.RandomAccessFile
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Tiny loopback-only HTTP range bridge. Media3 can consume this URL like any progressive
 * source. It never binds to LAN interfaces, so downloaded data is not exposed to the network.
 */
class GrowingFileHttpServer(
    private val file: File,
    private val expectedLength: Long,
    private val onRangeRequested: (Long) -> Unit = {}
) : AutoCloseable {
    private val running = AtomicBoolean(false)
    private val workers = Executors.newCachedThreadPool()
    private var server: ServerSocket? = null

    val url: String
        get() = "http://127.0.0.1:${server?.localPort ?: error("Server not started")}/stream"

    fun start() {
        if (!running.compareAndSet(false, true)) return
        server = ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"))
        workers.execute {
            while (running.get()) {
                try { workers.execute { handle(server!!.accept()) } }
                catch (_: Throwable) { if (!running.get()) break }
            }
        }
    }

    private fun handle(socket: Socket) = socket.use { s ->
        s.soTimeout = 30_000
        val reader = BufferedReader(InputStreamReader(s.getInputStream()))
        val request = reader.readLine() ?: return
        var range: String? = null
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break
            if (line.startsWith("Range:", true)) range = line.substringAfter(':').trim()
        }
        if (!request.startsWith("GET ") && !request.startsWith("HEAD ")) return

        val start = range?.substringAfter("bytes=")?.substringBefore('-')?.toLongOrNull() ?: 0L
        val requestedEnd = range?.substringAfter('-')?.takeIf { it.isNotBlank() }?.toLongOrNull()
        val end = (requestedEnd ?: (expectedLength - 1)).coerceAtMost(expectedLength - 1)
        if (start !in 0 until expectedLength || end < start) return
        onRangeRequested(start)

        // For sequential playback, wait briefly until libtorrent has materialized data.
        // The player retries/range-seeks while the swarm catches up.
        var waited = 0
        while (running.get() && (!file.exists() || file.length() <= start) && waited < 30_000) {
            Thread.sleep(100)
            waited += 100
        }
        if (!file.exists() || file.length() <= start) {
            s.getOutputStream().write("HTTP/1.1 503 Service Unavailable\r\nConnection: close\r\n\r\n".toByteArray())
            return
        }

        val availableEnd = minOf(end, file.length() - 1)
        val count = availableEnd - start + 1
        val partial = range != null || start > 0 || availableEnd < expectedLength - 1
        val headers = buildString {
            append(if (partial) "HTTP/1.1 206 Partial Content\r\n" else "HTTP/1.1 200 OK\r\n")
            append("Accept-Ranges: bytes\r\n")
            append("Content-Type: application/octet-stream\r\n")
            append("Content-Length: $count\r\n")
            if (partial) append("Content-Range: bytes $start-$availableEnd/$expectedLength\r\n")
            append("Connection: close\r\n\r\n")
        }
        val out = s.getOutputStream()
        out.write(headers.toByteArray())
        if (request.startsWith("HEAD ")) return

        RandomAccessFile(file, "r").use { raf ->
            raf.seek(start)
            val buffer = ByteArray(128 * 1024)
            var left = count
            while (left > 0 && running.get()) {
                val n = raf.read(buffer, 0, minOf(buffer.size.toLong(), left).toInt())
                if (n <= 0) break
                out.write(buffer, 0, n)
                left -= n
            }
        }
    }

    override fun close() {
        running.set(false)
        runCatching { server?.close() }
        workers.shutdownNow()
    }
}
