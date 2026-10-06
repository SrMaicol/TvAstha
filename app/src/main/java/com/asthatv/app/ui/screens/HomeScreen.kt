package com.asthatv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.asthatv.app.data.DemoCatalog
import com.asthatv.app.model.MediaItemUi
import com.asthatv.app.ui.components.MediaRow
import com.asthatv.app.ui.components.PremiumCategoryRail
import com.asthatv.app.ui.components.TvCategory

@Composable
fun HomeScreen(onOpen: (MediaItemUi) -> Unit) {
    var featured by remember { mutableStateOf(DemoCatalog.hero) }
    var selectedCategory by remember { mutableStateOf("all") }
    val categories = remember { listOf(
        TvCategory("all", "Inicio"), TvCategory("movies", "Películas"),
        TvCategory("series", "Series"), TvCategory("scifi", "Ciencia ficción"),
        TvCategory("thriller", "Thriller"), TvCategory("action", "Acción"),
        TvCategory("new", "Novedades")
    ) }
    val visibleSections = remember(selectedCategory) {
        when (selectedCategory) {
            "movies" -> DemoCatalog.sections.filter { it.id == "movies" || it.id == "trending" }
            "series" -> DemoCatalog.sections.filter { it.id == "series" || it.id == "trending" }
            "new" -> DemoCatalog.sections.filter { it.id == "new" || it.id == "trending" }
            "scifi" -> DemoCatalog.filteredSections("Sci‑Fi")
            "thriller" -> DemoCatalog.filteredSections("Thriller")
            "action" -> DemoCatalog.filteredSections("Action")
            else -> DemoCatalog.sections
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = featured.backdrop,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(430.dp).align(Alignment.TopCenter)
        )
        Box(
            Modifier.fillMaxWidth().height(440.dp).background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = .22f), Color.Black.copy(alpha = .62f), Color.Black)
                )
            )
        )
        Box(
            Modifier.fillMaxWidth(.62f).height(440.dp).background(
                Brush.horizontalGradient(listOf(Color.Black.copy(alpha = .86f), Color.Transparent))
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "hero", contentType = "hero") {
                Hero(featured)
            }
            item(key = "category-rail", contentType = "category-rail") {
                PremiumCategoryRail(
                    categories = categories,
                    selectedId = selectedCategory,
                    onSelected = { selectedCategory = it.id }
                )
            }
            items(
                visibleSections,
                key = { it.id },
                contentType = { "media-row" }
            ) { section ->
                MediaRow(
                    section = section,
                    onFocused = { featured = it },
                    onOpen = onOpen
                )
            }
        }
    }
}

@Composable
private fun Hero(media: MediaItemUi) {
    Column(
        modifier = Modifier.height(370.dp).fillMaxWidth().padding(start = 48.dp, top = 72.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("ASTHA", color = Color.White.copy(alpha = .68f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(media.title, color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            "${media.year}   ${media.rating} ★   ${media.genre}",
            color = Color.White.copy(alpha = .70f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            media.description,
            color = Color.White.copy(alpha = .76f),
            fontSize = 15.sp,
            lineHeight = 21.sp,
            modifier = Modifier.width(540.dp)
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Selecciona un póster para ver detalles", color = Color.White.copy(alpha = .62f), fontSize = 13.sp)
        }
    }
}
