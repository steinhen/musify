package me.henriquestein.model

import com.fasterxml.jackson.annotation.JsonProperty

data class MusicBrainzArtistResponse(
    @JsonProperty("id") val mbId: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("gender") val gender: String?,
    @JsonProperty("country") val country: String,
    @JsonProperty("disambiguation") val disambiguation: String?,
    @JsonProperty("relations") val relations: Set<Relation>,
    @JsonProperty("release-groups") val releases: Set<Release>
) {

    fun getWikidataId() = relations.firstOrNull { it.type == WIKIDATA_TYPE }
        ?.url
        ?.resource
        ?.substringAfterLast("/")

    data class Relation(
        @JsonProperty("type") val type: String,
        @JsonProperty("url") val url: RelationUrl,
    )

    data class RelationUrl(@JsonProperty("resource") val resource: String?)

    data class Release(
        @JsonProperty("id") val id: String,
        @JsonProperty("title") val title: String
    )

    companion object {
        const val WIKIDATA_TYPE = "wikidata"
    }
}





