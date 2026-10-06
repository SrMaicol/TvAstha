package com.asthatv.app.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.asthatv.app.player.PlaybackCoordinator
import com.asthatv.app.player.PlaybackSource
import com.asthatv.app.torrent.TorrentEngine
import com.asthatv.app.torrent.TorrentSessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PlayerScreen(
    source: PlaybackSource,
    torrentEngine: TorrentEngine,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coordinator = remember(torrentEngine) { PlaybackCoordinator(torrentEngine) }
    val torrentState by torrentEngine.state.collectAsState()
    var preparedUrl by remember(source) { mutableStateOf<String?>(null) }
    var prepareError by remember(source) { mutableStateOf<String?>(null) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    LaunchedEffect(source) {
        runCatching { withContext(Dispatchers.IO) { coordinator.prepare(source) } }
            .onSuccess { preparedUrl = it }
            .onFailure { prepareError = it.message ?: "No se pudo preparar el stream" }
    }

    LaunchedEffect(preparedUrl) {
        preparedUrl?.let {
            player.setMediaItem(MediaItem.fromUri(it))
            player.prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
            coordinator.stop()
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = true
                    isFocusable = true
                    isFocusableInTouchMode = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    requestFocus()
                }
            },
            update = { it.player = player }
        )

        if (source is PlaybackSource.Magnet || source is PlaybackSource.Torrent) {
            TorrentHud(torrentState, Modifier.align(Alignment.TopEnd).padding(28.dp))
        }

        if (preparedUrl == null && prepareError == null) {
            Text("Conectando con fuentes P2P…", color = Color.White.copy(alpha = .75f),
                modifier = Modifier.align(Alignment.Center), fontSize = 18.sp)
        }
        prepareError?.let {
            Text(it, color = Color.White, modifier = Modifier.align(Alignment.Center).padding(40.dp))
        }
    }
}

@Composable
private fun TorrentHud(state: TorrentSessionState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.Black.copy(alpha = .62f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (state.name.isNotBlank()) Text(state.name, color = Color.White, fontSize = 12.sp)
        Text(
            "↓ ${formatRate(state.downloadRate)}   ↑ ${formatRate(state.uploadRate)}   " +
                "Peers ${state.peers}   Seeds ${state.seeds}",
            color = Color.White.copy(alpha = .72f), fontSize = 12.sp
        )
        Text("Torrent ${(state.progress * 100).toInt()}%", color = Color.White.copy(alpha = .52f), fontSize = 11.sp)
    }
}

private fun formatRate(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB/s".format(bytes / 1_048_576.0)
    bytes >= 1024 -> "%.0f KB/s".format(bytes / 1024.0)
    else -> "$bytes B/s"
}
