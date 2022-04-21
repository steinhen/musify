package me.henriquestein.model

import com.fasterxml.jackson.annotation.JsonProperty

data class WikipediaSummaryResponse(
    @JsonProperty("description") val description: String,
    @JsonProperty("extract") val extract: String,
)
