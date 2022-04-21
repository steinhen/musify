package me.henriquestein.wikidata

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.runBlocking
import me.henriquestein.ResourceUtil
import me.henriquestein.configuration.WikidataConfiguration
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.testng.annotations.AfterClass
import org.testng.annotations.Test

class WikidataClientTest {

    private val mockWebServer = MockWebServer()
    private val webClient = WikidataConfiguration(mockWebServer.url("/").toString()).webClient()
    private val sut = WikidataClient(webClient)

    @AfterClass
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `it should request wikidata by id`() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(ResourceUtil.readResource("wikidata_response.json"))
        )

        val wikidataId = "Q2831"
        val actual = sut.request(wikidataId)
        assertThat(actual).isNotNull()
        val entityName = actual!!.getWikiSummaryPageName(wikidataId)
        assertThat(entityName).isEqualTo("Michael_Jackson")
    }

}