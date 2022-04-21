package me.henriquestein.model

data class ArtistSummary(
    val mbid: String,
    val name: String,
    val gender: String?,
    val country: String,
    val disambiguation: String?,
    val description: String,
    val albums: Set<Album>
) {
    data class Album(
        val id: String,
        val title: String,
        val imageUrl: String?
    )
}