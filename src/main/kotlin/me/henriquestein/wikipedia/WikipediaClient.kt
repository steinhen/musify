package me.henriquestein.wikipedia

import me.henriquestein.configuration.WikipediaConfiguration
import me.henriquestein.model.WikipediaSummaryResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Component
class WikipediaClient(
    @Qualifier(WikipediaConfiguration.WEB_CLIENT)
    private val webClient: WebClient
) {
    suspend fun requestSummary(pageName: String): WikipediaSummaryResponse? {
        return webClient.get()
            .uri("/page/summary/$pageName")
            .retrieve()
            .awaitBodyOrNull()
    }
}