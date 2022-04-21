package me.henriquestein.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.testng.annotations.Test

class MusicBrainzArtistResponseTest {

    @Test
    fun `getWikidataId takes the id from url even when there is no slash`() {
        val wikidataId = build(
            setOf(createWikidataRelation(EXPECTED))
        ).getWikidataId()

        assertThat(wikidataId)
            .isNotNull()
            .isEqualTo(EXPECTED)
    }

    @Test
    fun `getWikidataId takes the id from url from string after last slash`() {
        val wikidataId = build(
            setOf(createWikidataRelation("https://url.com/$EXPECTED"))
        ).getWikidataId()

        assertThat(wikidataId)
            .isNotNull()
            .isEqualTo(EXPECTED)
    }

    @Test
    fun `getWikidataId returns null when there is no wikidata relation`() {
        val wikidataId = build(
            setOf(createRandomRelation())
        ).getWikidataId()

        assertThat(wikidataId).isNull()
    }

    @Test
    fun `getWikidataId returns null when there is no relation`() {
        val wikidataId = build(emptySet()).getWikidataId()

        assertThat(wikidataId).isNull()
    }

    @Test
    fun `getWikidataId takes the id from the first wikidata entry`() {
        val wikidataId = build(
            setOf(
                createRandomRelation(),
                createWikidataRelation(EXPECTED)
            )
        ).getWikidataId()

        assertThat(wikidataId)
            .isNotNull()
            .isEqualTo(EXPECTED)
    }

    private fun build(
        relations: Set<MusicBrainzArtistResponse.Relation>
    ) =
        MusicBrainzArtistResponse(
            mbId = "",
            name = "",
            gender = null,
            country = "",
            disambiguation = null,
            relations = relations,
            releases = setOf(MusicBrainzArtistResponse.Release("release", "title"))
        )

    private fun createWikidataRelation(url: String?) = MusicBrainzArtistResponse.Relation(
        MusicBrainzArtistResponse.WIKIDATA_TYPE,
        MusicBrainzArtistResponse.RelationUrl(url)
    )

    private fun createRandomRelation() = MusicBrainzArtistResponse.Relation(
        RANDOM,
        MusicBrainzArtistResponse.RelationUrl("https://url.com/$RANDOM")
    )

    companion object {
        private const val EXPECTED = "expected"
        private const val RANDOM = "random"
    }
}