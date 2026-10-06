package com.asthatv.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.asthatv.app.model.MediaItemUi
import com.asthatv.app.player.PlaybackSource
import com.asthatv.app.torrent.TorrentEngineFactory
import com.asthatv.app.ui.components.PremiumSidebar
import com.asthatv.app.ui.screens.*

private sealed interface Screen {
    data object Shell : Screen
    data class Details(val media: MediaItemUi) : Screen
    data class Player(val source: PlaybackSource) : Screen
}

@Composable
fun AsthaTvApp() {
    val context = LocalContext.current
    val torrentEngine = remember { TorrentEngineFactory.create(context) }
    var screen by remember { mutableStateOf<Screen>(Screen.Shell) }
    var destination by remember { mutableStateOf("home") }

    DisposableEffect(Unit) { onDispose { torrentEngine.shutdown() } }

    when (val current = screen) {
        Screen.Shell -> Box(Modifier.fillMaxSize()) {
            when (destination) {
                "home" -> HomeScreen(onOpen = { screen = Screen.Details(it) })
                "search" -> PlaceholderScreen("Buscar", "Encuentra películas, series y contenido de tus addons.")
                "library" -> PlaceholderScreen("Biblioteca", "Tu colección, listas y contenido guardado.")
                "addons" -> PlaceholderScreen("Addons", "Gestiona fuentes y catálogos instalados.")
                "downloads" -> PlaceholderScreen("Descargas", "Contenido disponible y transferencias activas.")
                else -> PlaceholderScreen("Ajustes", "Reproducción, perfiles, red, subtítulos y privacidad.")
            }
            PremiumSidebar(selectedId = destination, onSelected = { destination = it.id })
        }
        is Screen.Details -> {
            BackHandler { screen = Screen.Shell }
            DetailsScreen(
                media = current.media,
                onBack = { screen = Screen.Shell },
                onPlayDemo = { screen = Screen.Player(PlaybackSource.Direct("https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")) }
            )
        }
        is Screen.Player -> {
            BackHandler { screen = Screen.Shell }
            PlayerScreen(current.source, torrentEngine, onBack = { screen = Screen.Shell })
        }
    }
}
