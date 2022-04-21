package me.henriquestein.musicbrainz

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.runBlocking
import me.henriquestein.ResourceUtil
import me.henriquestein.configuration.MusicBrainzConfiguration
import me.henriquestein.model.MusicBrainzArtistResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testng.annotations.AfterClass
import org.testng.annotations.Test

class MusicBrainzClientTest {

    private val mockWebServer = MockWebServer()
    private val webClient = MusicBrainzConfiguration(mockWebServer.url("/").toString()).webClient()
    private val sut = MusicBrainzClient(webClient)

    @AfterClass
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `it requests MB for artist data`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ResourceUtil.readResource("music_brainz_artist_response.json"))
        )

        assertThat(sut.requestArtist("5c210861-2ce2-4be3-9307-bbcfc361cc01"))
            .isEqualTo(
                MusicBrainzArtistResponse(
                    mbId = "5c210861-2ce2-4be3-9307-bbcfc361cc01",
                    name = "Pennywise",
                    gender = null,
                    country = "US",
                    disambiguation = "",
                    relations = setOf(RELATION),
                    releases = setOf(RELEASE_1, RELEASE_2)
                )
            )
    }

    @Test(expectedExceptions = [RuntimeException::class])
    fun `it throws exception if artistMbId is empty`() = runBlocking {
        sut.requestArtist("")
    }

    companion object {
        private const val RELEASE_1_ID = "bdb7df87-2848-30dc-9b65-86f88056dc61"
        private const val RELEASE_1_TITLE = "Pennywise"
        private const val RELEASE_2_ID = "2c959b11-1c24-3a7c-a835-b282f23fe8df"
        private const val RELEASE_2_TITLE = "Unknown Road"
        private val RELEASE_1 = MusicBrainzArtistResponse.Release(id = RELEASE_1_ID, title = RELEASE_1_TITLE)
        private val RELEASE_2 = MusicBrainzArtistResponse.Release(id = RELEASE_2_ID, title = RELEASE_2_TITLE)
        private val RELATION = MusicBrainzArtistResponse.Relation(
            type = "wikidata",
            url = MusicBrainzArtistResponse.RelationUrl("https://www.wikidata.org/wiki/Q754051")
        )
    }
}