package me.henriquestein.coverartarchive

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.runBlocking
import me.henriquestein.ResourceUtil
import me.henriquestein.configuration.CoverArtArchiveConfiguration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testng.annotations.AfterClass
import org.testng.annotations.Test

class CoverArtArchiveClientTest {
    private val mockWebServer = MockWebServer()
    private val webClient = CoverArtArchiveConfiguration(mockWebServer.url("/").toString()).webClient()
    private val sut = CoverArtArchiveClient(webClient)

    @AfterClass
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `it should request release groups`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ResourceUtil.readResource("cover_art_archive_response.json"))
        )

        val actual = sut.requestCoverArt("bdb7df87-2848-30dc-9b65-86f88056dc61")
        assertThat(actual).isNotNull()
        assertThat(actual!!.getFirstImageUrl())
            .isEqualTo("http://coverartarchive.org/release/bd7aeb3e-c3ad-4ea3-a0cc-69935c1a196f/4802623604.jpg")
    }
}