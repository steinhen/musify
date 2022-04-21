package me.henriquestein.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.testng.annotations.Test


class WikidataResponseTest {

    @Test
    fun `getWikiSummaryPageName takes the last string after slash from enwiki site link url`() {
        val actual = createWikidata(mapOf(wikidata)).getWikiSummaryPageName(WIKIDATA_ID)
        assertThat(actual)
            .isNotNull()
            .isEqualTo(WikidataResponse.ENWIKI_SITELINK_KEY)
    }

    @Test
    fun `getWikiSummaryPageName returns null when no wikidata`() {
        val actual = createWikidata(mapOf(nonWikidata)).getWikiSummaryPageName(WIKIDATA_ID)
        assertThat(actual)
            .isNull()
    }

    companion object {
        private const val WIKIDATA_ID = "Q1234"
        private val wikidata = WIKIDATA_ID to WikidataResponse.EntityData(
            mapOf(
                createSiteLinkEntry(WikidataResponse.ENWIKI_SITELINK_KEY),
                createSiteLinkEntry("anything_else"),
            )
        )
        private val nonWikidata = "another_thing" to WikidataResponse.EntityData(
            mapOf(
                createSiteLinkEntry(WikidataResponse.ENWIKI_SITELINK_KEY),
                createSiteLinkEntry("anything_else"),
            )
        )

        private fun createWikidata(map: Map<String, WikidataResponse.EntityData>) = WikidataResponse(map)

        private fun createSiteLinkEntry(key: String) = key to WikidataResponse.SiteLink("https://url.com/$key")
    }

}