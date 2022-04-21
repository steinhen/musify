package me.henriquestein.integrationtests

import me.henriquestein.ResourceUtil
import me.henriquestein.configuration.CacheConfiguration
import me.henriquestein.configuration.CoverArtArchiveConfiguration
import me.henriquestein.configuration.MusicBrainzConfiguration
import me.henriquestein.configuration.WikidataConfiguration
import me.henriquestein.configuration.WikipediaConfiguration
import me.henriquestein.controller.MusifyController
import me.henriquestein.coverartarchive.CoverArtArchiveClient
import me.henriquestein.musicbrainz.MusicBrainzClient
import me.henriquestein.service.CachedMusifyService
import me.henriquestein.service.MusifyService
import me.henriquestein.wikidata.WikidataClient
import me.henriquestein.wikipedia.WikipediaClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.time.Duration

class MusifyIntegrationTest {

    private val mockWebServer = MockWebServer().apply {
        start(8081)
    }

    private val mockWebServerBaseUrl = mockWebServer.url("").toString()

    // TODO this should be somehow injected through spring, but I struggled
    //  with it because maybe there are some complications by using testng
    //  I did not want to spend more time to investigate it further so I went
    //  with defining the whole application context manually
    private val controller = MusifyController(
        CachedMusifyService(
            MusifyService(
                MusicBrainzClient(MusicBrainzConfiguration(mockWebServerBaseUrl).webClient()),
                WikidataClient(WikidataConfiguration(mockWebServerBaseUrl).webClient()),
                WikipediaClient(WikipediaConfiguration(mockWebServerBaseUrl).webClient()),
                CoverArtArchiveClient(CoverArtArchiveConfiguration(mockWebServerBaseUrl).webClient())
            ),
            CacheConfiguration(Duration.ofSeconds(10)).cache()
        )
    )

    private val webClient = WebTestClient.bindToController(controller).build()

    @BeforeMethod
    fun setup() {
        webClient.mutate().responseTimeout(Duration.ofDays(1))
        mockWebServer.dispatcher = myDispatcher
    }

    @AfterClass
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `it returns status 200 and response body as expected`() {
        webClient.get()
            .uri("/artist/5c210861-2ce2-4be3-9307-bbcfc361cc01")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.mbid").isEqualTo(MBID)
            .jsonPath("$.name").isEqualTo(ARTIST_NAME)
            .jsonPath("$.gender").isEmpty
            .jsonPath("$.country").isEqualTo(COUNTRY)
            .jsonPath("$.disambiguation").isEmpty
            .jsonPath("$.description").isEqualTo(DESCRIPTION)
            .jsonPath("$.albums[0].id").isEqualTo(RELEASE_1_ID)
            .jsonPath("$.albums[0].title").isEqualTo(RELEASE_1_TITLE)
            .jsonPath("$.albums[0].imageUrl").isEqualTo(RELEASE_1_COVER_URL)
    }

    private val myDispatcher = object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(request: RecordedRequest): MockResponse {
            val path = request.path
            return when {
                path.isCoverArtArchivePath() -> respondWithBodyFrom("cover_art_archive_response.json")
                path.isMusicBraizPath() -> respondWithBodyFrom("music_brainz_artist_response.json")
                path.isWikidataPath() -> respondWithBodyFrom("wikidata_response.json")
                path.isWikipediaPath() -> respondWithBodyFrom("wikipedia_response.json")
                else -> MockResponse().setResponseCode(404)
            }
        }

        private fun respondWithBodyFrom(filename: String) =
            MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ResourceUtil.readResource("integrationtest/$filename"))

        private fun String?.isCoverArtArchivePath() = this?.contains("/release-group") ?: false
        private fun String?.isMusicBraizPath() = this?.contains("artist") ?: false
        private fun String?.isWikidataPath() = this?.contains("Special:EntityData") ?: false
        private fun String?.isWikipediaPath() = this?.contains("summary") ?: false
    }

    companion object {
        private const val RELEASE_1_ID = "bdb7df87-2848-30dc-9b65-86f88056dc61"
        private const val RELEASE_1_TITLE = "Pennywise"
        private const val RELEASE_1_COVER_URL =
            "http://coverartarchive.org/release/bd7aeb3e-c3ad-4ea3-a0cc-69935c1a196f/4802623604.jpg"
        private const val MBID = "5c210861-2ce2-4be3-9307-bbcfc361cc01"
        private const val ARTIST_NAME = "Pennywise"
        private const val COUNTRY = "US"
        private const val DESCRIPTION = "American punk rock band"
    }
}