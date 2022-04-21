package me.henriquestein.wikipedia

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.runBlocking
import me.henriquestein.ResourceUtil
import me.henriquestein.configuration.WikipediaConfiguration
import me.henriquestein.model.WikipediaSummaryResponse
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testng.annotations.AfterClass
import org.testng.annotations.Test

class WikipediaClientTest {

    private val mockWebServer = MockWebServer()
    private val webClient = WikipediaConfiguration(mockWebServer.url("/").toString()).webClient()
    private val sut = WikipediaClient(webClient)

    @AfterClass
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `it should request summary info`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ResourceUtil.readResource("wikipedia_response.json"))
        )

        assertThat(sut.requestSummary("Michael_Jackson"))
            .isNotNull()
            .isEqualTo(
                WikipediaSummaryResponse(
                    description = "American singer, songwriter, and dancer (1958–2009)",
                    extract = "Michael Joseph Jackson was an American singer, songwriter, and dancer. Dubbed the \"King of Pop\", he is regarded as one of the most significant cultural figures of the 20th century. Over a four-decade career, his contributions to music, dance, and fashion, along with his publicized personal life, made him a global figure in popular culture. Jackson influenced artists across many music genres; through stage and video performances, he popularized complicated dance moves such as the moonwalk, to which he gave the name, as well as the robot. He is the most awarded individual music artist in history."
                )
            )
    }
}