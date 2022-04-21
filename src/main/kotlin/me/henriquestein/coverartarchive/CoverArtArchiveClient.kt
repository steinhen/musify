package me.henriquestein.coverartarchive

import kotlinx.coroutines.reactor.awaitSingle
import me.henriquestein.configuration.CoverArtArchiveConfiguration
import me.henriquestein.model.CoverArtArchiveReleaseGroupResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import kotlin.system.measureTimeMillis

@Component
class CoverArtArchiveClient(
    @Qualifier(CoverArtArchiveConfiguration.WEB_CLIENT)
    private val webClient: WebClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    suspend fun requestCoverArt(releaseGroupId: String): CoverArtArchiveReleaseGroupResponse? {
        logger.info("Loading cover for $releaseGroupId...")
        var response: CoverArtArchiveReleaseGroupResponse?
        val time = measureTimeMillis {
            response = webClient.get()
                .uri("/release-group/$releaseGroupId")
                .retrieve()
                .bodyToMono<CoverArtArchiveReleaseGroupResponse>()
                .awaitSingle()
        }
        logger.info("Took ${time}ms to load cover for $releaseGroupId")
        return response
    }
}