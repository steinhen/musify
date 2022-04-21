package me.henriquestein.model

import com.fasterxml.jackson.annotation.JsonProperty

data class WikidataResponse(@JsonProperty("entities") val entities: Map<String, EntityData>) {

    fun getWikiSummaryPageName(wikidataId: String) = entities[wikidataId]
        ?.siteLinks
        ?.filterKeys { it == ENWIKI_SITELINK_KEY }
        ?.firstNotNullOf { it.value }
        ?.entityName

    data class EntityData(@JsonProperty("sitelinks") val siteLinks: Map<String, SiteLink>)

    data class SiteLink(@JsonProperty("url") val url: String) {
        val entityName = url.substringAfterLast("/")
    }

    companion object {
        const val ENWIKI_SITELINK_KEY = "enwiki"
    }
}



