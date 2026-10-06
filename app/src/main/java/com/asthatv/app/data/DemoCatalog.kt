package com.asthatv.app.data

import com.asthatv.app.model.MediaItemUi
import com.asthatv.app.model.MediaSection

object DemoCatalog {
    private val titles = listOf(
        "Beyond the Horizon", "Neon District", "The Last Signal", "Zero Hour",
        "Silent Orbit", "Black Tide", "Afterlight", "Northern Line",
        "Echo Protocol", "Night Runner", "The Archive", "Red Valley"
    )

    private val posterSeeds = listOf(
        "cinema-01", "cinema-02", "cinema-03", "cinema-04", "cinema-05", "cinema-06",
        "cinema-07", "cinema-08", "cinema-09", "cinema-10", "cinema-11", "cinema-12"
    )

    private fun item(index: Int): MediaItemUi {
        val n = index % titles.size
        return MediaItemUi(
            id = "demo-$index",
            title = titles[n],
            poster = "https://picsum.photos/seed/${posterSeeds[n]}/420/630",
            backdrop = "https://picsum.photos/seed/${posterSeeds[n]}-wide/1280/720",
            year = (2026 - (index % 8)).toString(),
            rating = String.format("%.1f", 7.1 + (index % 19) / 10.0),
            description = "Una experiencia cinematográfica creada para demostrar la interfaz premium de Astha TV, navegación por control remoto y carga progresiva.",
            genre = listOf("Drama", "Sci‑Fi", "Thriller", "Action")[index % 4]
        )
    }

    val hero = item(2)

    val sections: List<MediaSection> = listOf(
        MediaSection("continue", "Continuar viendo", (0..11).map(::item)),
        MediaSection("trending", "Tendencias", (12..27).map(::item)),
        MediaSection("movies", "Películas para ti", (28..43).map(::item)),
        MediaSection("series", "Series destacadas", (44..59).map(::item)),
        MediaSection("new", "Recién añadidos", (60..75).map(::item))
    )

    fun filteredSections(genre: String): List<MediaSection> = sections.mapNotNull { section ->
        val filtered = section.items.filter { it.genre == genre }
        if (filtered.isEmpty()) null else section.copy(items = filtered)
    }
}
