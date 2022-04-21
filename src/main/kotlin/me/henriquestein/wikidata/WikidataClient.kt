package me.henriquestein.wikidata

import me.henriquestein.configuration.WikidataConfiguration
import me.henriquestein.model.WikidataResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull

@Component
class WikidataClient(
    @Qualifier(WikidataConfiguration.WEB_CLIENT)
    private val webClient: WebClient
) {
    suspend fun request(wikidataId: String): WikidataResponse? {
        return webClient.get()
            .uri("/Special:EntityData/$wikidataId.json")
            .retrieve()
            .awaitBodyOrNull()
    }
}