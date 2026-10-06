package com.asthatv.app.data

import com.asthatv.app.model.StremioManifest
import com.asthatv.app.model.StremioStream
import com.asthatv.app.model.StremioSubtitle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPInputStream

/**
 * Optimized Stremio Addon v3 HTTP client.
 * Features:
 * - In-memory LRU/TTL caching for manifest, catalog, meta, streams, and subtitles.
 * - Automatic retry with exponential backoff for transient network errors.
 * - Gzip compression & optimized HTTP connection parameters (keep-alive).
 * - Network + JSON parsing on Dispatchers.IO.
 */
class StremioAddonClient(manifestUrl: String) {
    private val manifestUri = URI(manifestUrl)
    private val baseUrl: String

    init {
        require(manifestUri.scheme == "https") { "Only HTTPS addon URLs are accepted" }
        require(manifestUri.path.endsWith("/manifest.json")) { "URL must end with /manifest.json" }
        baseUrl = manifestUrl.removeSuffix("/manifest.json").trimEnd('/')
    }

    private data class CacheEntry(val data: String, val timestamp: Long)
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val cacheTtlMs = 5 * 60 * 1000L // 5 minutes TTL

    suspend fun manifest(): StremioManifest = withContext(Dispatchers.IO) {
        val json = getCachedJson("$baseUrl/manifest.json")
        StremioManifest(
            id = json.optString("id"),
            version = json.optString("version"),
            name = json.optString("name"),
            description = json.optString("description"),
            resources = json.optJSONArray("resources").stringValues(),
            types = json.optJSONArray("types").stringValues()
        )
    }

    suspend fun catalog(type: String, id: String, extra: Map<String, String> = emptyMap()): JSONObject =
        withContext(Dispatchers.IO) {
            val suffix = if (extra.isEmpty()) "" else {
                "/" + extra.entries.joinToString("&") { entry ->
                    "${enc(entry.key)}=${enc(entry.value)}"
                }
            }
            getCachedJson("$baseUrl/catalog/${enc(type)}/${enc(id)}$suffix.json")
        }

    suspend fun meta(type: String, id: String): JSONObject = withContext(Dispatchers.IO) {
        getCachedJson("$baseUrl/meta/${enc(type)}/${enc(id)}.json")
    }

    suspend fun streams(type: String, id: String): List<StremioStream> = withContext(Dispatchers.IO) {
        val array = getCachedJson("$baseUrl/stream/${enc(type)}/${enc(id)}.json").optJSONArray("streams")
            ?: JSONArray()
        buildList {
            for (i in 0 until array.length()) {
                val s = array.optJSONObject(i) ?: continue
                add(
                    StremioStream(
                        name = s.optString("name").takeIf { it.isNotBlank() },
                        title = s.optString("title").takeIf { it.isNotBlank() },
                        url = s.optString("url").takeIf { it.isNotBlank() },
                        infoHash = s.optString("infoHash").takeIf { it.isNotBlank() },
                        fileIdx = if (s.has("fileIdx")) s.optInt("fileIdx") else null,
                        externalUrl = s.optString("externalUrl").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    suspend fun subtitles(type: String, id: String): List<StremioSubtitle> = withContext(Dispatchers.IO) {
        val array = getCachedJson("$baseUrl/subtitles/${enc(type)}/${enc(id)}.json").optJSONArray("subtitles")
            ?: JSONArray()
        buildList {
            for (i in 0 until array.length()) {
                val s = array.optJSONObject(i) ?: continue
                val url = s.optString("url")
                if (url.isBlank()) continue
                add(StremioSubtitle(s.optString("id"), url, s.optString("lang")))
            }
        }
    }

    private suspend fun getCachedJson(url: String): JSONObject {
        val now = System.currentTimeMillis()
        cache[url]?.let { entry ->
            if (now - entry.timestamp < cacheTtlMs) {
                return JSONObject(entry.data)
            }
        }
        val body = getJsonWithRetry(url)
        if (cache.size > 100) {
            val oldestKey = cache.entries.minByOrNull { it.value.timestamp }?.key
            if (oldestKey != null) cache.remove(oldestKey)
        }
        cache[url] = CacheEntry(body, now)
        return JSONObject(body)
    }

    private suspend fun getJsonWithRetry(url: String): String {
        var currentDelay = 400L
        val maxRetries = 3
        for (attempt in 1..maxRetries) {
            try {
                return executeGet(url)
            } catch (e: IOException) {
                if (attempt == maxRetries) throw e
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        throw IOException("Failed to fetch $url after $maxRetries attempts")
    }

    private fun executeGet(url: String): String {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 6_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Accept-Encoding", "gzip")
            connection.setRequestProperty("User-Agent", "AsthaTV/0.1 (Optimized)")
            connection.setRequestProperty("Connection", "keep-alive")
            connection.instanceFollowRedirects = true
            val code = connection.responseCode
            if (code !in 200..299) error("Addon returned HTTP $code")
            val inputStream = if (connection.contentEncoding?.contains("gzip", ignoreCase = true) == true) {
                GZIPInputStream(connection.inputStream)
            } else {
                connection.inputStream
            }
            inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun enc(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun JSONArray?.stringValues(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (i in 0 until length()) {
                when (val value = opt(i)) {
                    is String -> add(value)
                    is JSONObject -> value.optString("name").takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }
    }
}
