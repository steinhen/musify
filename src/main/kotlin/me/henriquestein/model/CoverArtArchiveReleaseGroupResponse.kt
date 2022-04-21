package me.henriquestein.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CoverArtArchiveReleaseGroupResponse(@JsonProperty("images") val images: Set<Image>) {
    fun getFirstImageUrl() = images.firstOrNull { it.front }?.imageUrl

    data class Image(
        @JsonProperty("front") val front: Boolean,
        @JsonProperty("image") val imageUrl: String
    )
}

