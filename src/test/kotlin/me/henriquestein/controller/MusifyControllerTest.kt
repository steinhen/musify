package me.henriquestein.controller

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import me.henriquestein.model.ArtistSummary
import me.henriquestein.service.IMusifyService
import org.springframework.http.HttpStatus
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class MusifyControllerTest {

    private val musifyServiceMock = mockk<IMusifyService>()

    private val webClient = WebTestClient.bindToController(MusifyController(musifyServiceMock)).build()

    @BeforeMethod
    fun cleanup() {
        clearMocks(musifyServiceMock)
    }

    @Test
    fun `it returns status 200 and response body as expected`() {
        coEvery { musifyServiceMock.lookup(any()) } returns
            ArtistSummary(
                mbid = MBID,
                name = ARTIST_NAME,
                gender = null,
                country = COUNTRY,
                disambiguation = "",
                description = DESCRIPTION,
                albums = setOf(
                    ArtistSummary.Album(
                        RELEASE_1_ID,
                        RELEASE_1_TITLE,
                        RELEASE_1_COVER_URL
                    )
                )
            )

        webClient.get()
            .uri(URI)
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

        coVerify(exactly = 1) { musifyServiceMock.lookup("5c210861-2ce2-4be3-9307-bbcfc361cc01") }

    }

    @Test
    fun `it returns status 400 when bad request to fetch artist`() {
        coEvery { musifyServiceMock.lookup(any()) } throws WebClientResponseException(
            /* statusCode = */ HttpStatus.BAD_REQUEST.value(),
            /* statusText = */ "",
            /* headers = */ null,
            /* body = */ null,
            /* charset = */ null
        )

        webClient.get()
            .uri(URI)
            .exchange()
            .expectStatus().isBadRequest

    }

    @Test
    fun `it returns status 404 when artist not found`() {
        coEvery { musifyServiceMock.lookup(any()) } throws WebClientResponseException(
            /* statusCode = */ HttpStatus.NOT_FOUND.value(),
            /* statusText = */ "",
            /* headers = */ null,
            /* body = */ null,
            /* charset = */ null
        )

        webClient.get()
            .uri(URI)
            .exchange()
            .expectStatus().isNotFound

    }

    @Test
    fun `it returns status 500 when request returns bad code`() {
        coEvery { musifyServiceMock.lookup(any()) } throws WebClientResponseException(
            /* statusCode = */ HttpStatus.INTERNAL_SERVER_ERROR.value(),
            /* statusText = */ "",
            /* headers = */ null,
            /* body = */ null,
            /* charset = */ null
        )

        webClient.get()
            .uri(URI)
            .exchange()
            .expectStatus().is5xxServerError
    }

    @Test
    fun `it returns status 500 when exception is caught`() {
        coEvery { musifyServiceMock.lookup(any()) } throws RuntimeException()

        webClient.get()
            .uri(URI)
            .exchange()
            .expectStatus().is5xxServerError

    }

    companion object {
        private const val URI = "/artist/5c210861-2ce2-4be3-9307-bbcfc361cc01"
        private const val RELEASE_1_ID = "release1"
        private const val RELEASE_1_TITLE = "title album 1"
        private const val RELEASE_1_COVER_URL = "https://cover.com/release1.jpg"
        private const val MBID = "5c210861-2ce2-4be3-9307-bbcfc361cc01"
        private const val ARTIST_NAME = "Pennywise"
        private const val COUNTRY = "US"
        private const val DESCRIPTION = "wiki description"
    }
}