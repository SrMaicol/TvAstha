package com.asthatv.app.model

data class MediaItemUi(
    val id: String,
    val title: String,
    val poster: String,
    val backdrop: String,
    val year: String,
    val rating: String,
    val description: String,
    val genre: String = "Drama"
)

data class MediaSection(
    val id: String,
    val title: String,
    val items: List<MediaItemUi>
)

data class StremioManifest(
    val id: String,
    val version: String,
    val name: String,
    val description: String,
    val resources: List<String>,
    val types: List<String>
)

data class StremioStream(
    val name: String?,
    val title: String?,
    val url: String?,
    val infoHash: String?,
    val fileIdx: Int?,
    val externalUrl: String?
)

data class StremioSubtitle(
    val id: String?,
    val url: String,
    val lang: String?
)
