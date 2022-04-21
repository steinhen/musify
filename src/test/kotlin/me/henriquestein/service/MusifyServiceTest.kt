package me.henriquestein.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.henriquestein.coverartarchive.CoverArtArchiveClient
import me.henriquestein.model.ArtistSummary
import me.henriquestein.model.CoverArtArchiveReleaseGroupResponse
import me.henriquestein.model.MusicBrainzArtistResponse
import me.henriquestein.model.WikidataResponse
import me.henriquestein.model.WikipediaSummaryResponse
import me.henriquestein.musicbrainz.MusicBrainzClient
import me.henriquestein.wikidata.WikidataClient
import me.henriquestein.wikipedia.WikipediaClient
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class MusifyServiceTest {

    private val musicBrainzClient: MusicBrainzClient = mockk()
    private val wikidataClient: WikidataClient = mockk()
    private val wikipediaClient: WikipediaClient = mockk()
    private val coverArtArchiveClient: CoverArtArchiveClient = mockk()
    private val sut = MusifyService(
        musicBrainzClient,
        wikidataClient,
        wikipediaClient,
        coverArtArchiveClient
    )

    @BeforeMethod
    fun setup() {
        clearMocks(musicBrainzClient, wikidataClient, wikidataClient, coverArtArchiveClient)
        coEvery { musicBrainzClient.requestArtist(any()) } returns ARTIST
        coEvery { wikidataClient.request(any()) } returns WIKIDATA
        coEvery { wikipediaClient.requestSummary(any()) } returns WIKIPEDIA_SUMMARY
        coEvery { coverArtArchiveClient.requestCoverArt(any()) } returnsMany listOf(COVER_RESPONSE_1, COVER_RESPONSE_2)
    }

    @Test
    fun `it should put together all data from different sources`() = runBlocking {
        val actual = sut.lookup("5c210861-2ce2-4be3-9307-bbcfc361cc01")

        assertThat(actual)
            .isNotNull()
            .isEqualTo(
                ArtistSummary(
                    mbid = MBID,
                    name = ARTIST_NAME,
                    gender = null,
                    country = COUNTRY,
                    disambiguation = "",
                    description = DESCRIPTION,
                    albums = setOf(
                        ArtistSummary.Album(RELEASE_1_ID, RELEASE_1_TITLE, RELEASE_1_COVER_URL),
                        ArtistSummary.Album(RELEASE_2_ID, RELEASE_2_TITLE, RELEASE_2_COVER_URL),
                    )
                )
            )
    }

    @Test
    fun `it should handle exception when not able to load wikidata and keep description empty`() = runBlocking {
        coEvery { wikidataClient.request(any()) } throws RuntimeException("Something went wrong!")
        val actual = sut.lookup("5c210861-2ce2-4be3-9307-bbcfc361cc01")
        assertThat(actual)
            .isNotNull()
            .isEqualTo(
                ArtistSummary(
                    mbid = MBID,
                    name = ARTIST_NAME,
                    gender = null,
                    country = COUNTRY,
                    disambiguation = "",
                    description = "",
                    albums = setOf(
                        ArtistSummary.Album(RELEASE_1_ID, RELEASE_1_TITLE, RELEASE_1_COVER_URL),
                        ArtistSummary.Album(RELEASE_2_ID, RELEASE_2_TITLE, RELEASE_2_COVER_URL),
                    )
                )
            )
    }

    @Test
    fun `it should handle runtime exception when not able to load cover art and keep url empty`() = runBlocking {
        coEvery { coverArtArchiveClient.requestCoverArt(any()) } throws RuntimeException()

        val actual = sut.lookup("5c210861-2ce2-4be3-9307-bbcfc361cc01")

        assertThat(actual)
            .isNotNull()
            .isEqualTo(
                ArtistSummary(
                    mbid = MBID,
                    name = ARTIST_NAME,
                    gender = null,
                    country = COUNTRY,
                    disambiguation = "",
                    description = DESCRIPTION,
                    albums = setOf(
                        ArtistSummary.Album(RELEASE_1_ID, RELEASE_1_TITLE, ""),
                        ArtistSummary.Album(RELEASE_2_ID, RELEASE_2_TITLE, ""),
                    )
                )
            )
    }

    @Test
    fun `it should handle cover not found when not able to load cover art and keep url empty`() = runBlocking {
        coEvery { coverArtArchiveClient.requestCoverArt(any()) } throws WebClientResponseException(
            /* statusCode = */ HttpStatus.NOT_FOUND.value(),
            /* statusText = */ "",
            /* headers = */ null,
            /* body = */ null,
            /* charset = */ null
        )

        val actual = sut.lookup("5c210861-2ce2-4be3-9307-bbcfc361cc01")

        assertThat(actual)
            .isNotNull()
            .isEqualTo(
                ArtistSummary(
                    mbid = MBID,
                    name = ARTIST_NAME,
                    gender = null,
                    country = COUNTRY,
                    disambiguation = "",
                    description = DESCRIPTION,
                    albums = setOf(
                        ArtistSummary.Album(RELEASE_1_ID, RELEASE_1_TITLE, ""),
                        ArtistSummary.Album(RELEASE_2_ID, RELEASE_2_TITLE, ""),
                    )
                )
            )
    }

    @Test
    fun `it should handle webclient exception and keep url empty`() = runBlocking {
        coEvery { coverArtArchiveClient.requestCoverArt(any()) } throws WebClientResponseException(
            /* statusCode = */ HttpStatus.BAD_REQUEST.value(),
            /* statusText = */ "",
            /* headers = */ null,
            /* body = */ null,
            /* charset = */ null
        )
        val actual = sut.lookup("5c210861-2ce2-4be3-9307-bbcfc361cc01")

        assertThat(actual)
            .isNotNull()
            .isEqualTo(
                ArtistSummary(
                    mbid = MBID,
                    name = ARTIST_NAME,
                    gender = null,
                    country = COUNTRY,
                    disambiguation = "",
                    description = DESCRIPTION,
                    albums = setOf(
                        ArtistSummary.Album(RELEASE_1_ID, RELEASE_1_TITLE, ""),
                        ArtistSummary.Album(RELEASE_2_ID, RELEASE_2_TITLE, ""),
                    )
                )
            )
    }

    companion object {
        private const val WIKIDATA_ID = "Q876876"
        private val WIKIDATA_RELATION = MusicBrainzArtistResponse.Relation(
            type = MusicBrainzArtistResponse.WIKIDATA_TYPE,
            url = MusicBrainzArtistResponse.RelationUrl("some/$WIKIDATA_ID")
        )
        private const val RELEASE_1_ID = "release1"
        private const val RELEASE_1_TITLE = "title album 1"
        private const val RELEASE_2_ID = "release2"
        private const val RELEASE_2_TITLE = "title album 2"
        private val RELEASE_1 = MusicBrainzArtistResponse.Release(id = RELEASE_1_ID, title = RELEASE_1_TITLE)
        private val RELEASE_2 = MusicBrainzArtistResponse.Release(id = RELEASE_2_ID, title = RELEASE_2_TITLE)
        private const val RELEASE_1_COVER_URL = "https://cover.com/release1.jpg"
        private const val RELEASE_2_COVER_URL = "https://cover.com/release2.jpg"
        private const val MBID = "5c210861-2ce2-4be3-9307-bbcfc361cc01"
        private const val ARTIST_NAME = "Pennywise"
        private const val COUNTRY = "US"
        private val ARTIST = MusicBrainzArtistResponse(
            mbId = MBID,
            name = ARTIST_NAME,
            gender = null,
            country = COUNTRY,
            disambiguation = "",
            relations = setOf(WIKIDATA_RELATION),
            releases = setOf(RELEASE_1, RELEASE_2)
        )
        private val WIKIDATA = WikidataResponse(
            mapOf(
                WIKIDATA_ID to WikidataResponse.EntityData(
                    mapOf(WikidataResponse.ENWIKI_SITELINK_KEY to WikidataResponse.SiteLink("some/artist"))
                )
            )
        )
        private const val DESCRIPTION = "wiki description"
        private val WIKIPEDIA_SUMMARY = WikipediaSummaryResponse(
            description = DESCRIPTION,
            extract = "some other long text"
        )
        private val COVER_RESPONSE_1 = CoverArtArchiveReleaseGroupResponse(
            setOf(CoverArtArchiveReleaseGroupResponse.Image(true, RELEASE_1_COVER_URL))
        )
        private val COVER_RESPONSE_2 = CoverArtArchiveReleaseGroupResponse(
            setOf(CoverArtArchiveReleaseGroupResponse.Image(true, RELEASE_2_COVER_URL))
        )
    }
}