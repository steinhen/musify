package me.henriquestein.musicbrainz

import kotlinx.coroutines.reactor.awaitSingle
import me.henriquestein.configuration.MusicBrainzConfiguration
import me.henriquestein.model.MusicBrainzArtistResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class MusicBrainzClient(
    @Qualifier(MusicBrainzConfiguration.WEB_CLIENT)
    private val webClient: WebClient
) {
    suspend fun requestArtist(artistMbId: String): MusicBrainzArtistResponse {
        if (artistMbId.isEmpty()) throw RuntimeException("artistMbId cannot be empty!")
        return webClient.get()
            .uri("/artist/$artistMbId?&fmt=json&inc=url-rels+release-groups")
            .retrieve()
            .bodyToMono(MusicBrainzArtistResponse::class.java)
            .awaitSingle()
    }
}
