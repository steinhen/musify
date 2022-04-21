package me.henriquestein.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.henriquestein.coverartarchive.CoverArtArchiveClient
import me.henriquestein.model.ArtistSummary
import me.henriquestein.model.MusicBrainzArtistResponse
import me.henriquestein.model.WikipediaSummaryResponse
import me.henriquestein.musicbrainz.MusicBrainzClient
import me.henriquestein.wikidata.WikidataClient
import me.henriquestein.wikipedia.WikipediaClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientResponseException
import kotlin.system.measureTimeMillis

@Service("musifyService")
open class MusifyService(
    private val musicBrainzClient: MusicBrainzClient,
    private val wikidataClient: WikidataClient,
    private val wikipediaClient: WikipediaClient,
    private val coverArtArchiveClient: CoverArtArchiveClient
) : IMusifyService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun lookup(artistId: String): ArtistSummary = coroutineScope {
        logger.info("Fetching information from sources for artist id = [$artistId]...")
        var artistSummary: ArtistSummary
        val time = measureTimeMillis { artistSummary = lookupArtist(artistId) }
        logger.info("Took ${time}ms to load information for artist id = [$artistId]")
        return@coroutineScope artistSummary
    }

    private suspend fun lookupArtist(artistId: String) = coroutineScope {
        with(artistId.fetchArtistData()) {
            val albumsDeferred = this.releases.map { convertToAlbumAsync(it) }
            val wikipediaSummaryDeferred = this.fetchWikiPageName()?.let { requestSummaryAsync(it) }
            return@coroutineScope this.buildArtistSummaryWith(
                wikipediaSummaryDeferred.awaitToDescription(),
                albumsDeferred.awaitToSet()
            )
        }
    }

    private suspend fun String.fetchArtistData() = musicBrainzClient.requestArtist(this)

    private fun CoroutineScope.convertToAlbumAsync(release: MusicBrainzArtistResponse.Release) =
        async { release.convertToAlbum() }

    private suspend fun MusicBrainzArtistResponse.fetchWikiPageName(): String? {
        return try {
            this.getWikidataId()?.let {
                wikidataClient.request(it)?.getWikiSummaryPageName(it)
            }
        } catch (e: RuntimeException) {
            logger.error("Exception caught while fetching wiki page name.", e)
            null
        }
    }

    private fun CoroutineScope.requestSummaryAsync(pageName: String) =
        async { wikipediaClient.requestSummary(pageName) }

    private suspend fun MusicBrainzArtistResponse.Release.convertToAlbum() =
        ArtistSummary.Album(this.id, this.title, this.fetchCoverUrl())

    private suspend fun MusicBrainzArtistResponse.Release.fetchCoverUrl(): String? {
        return try {
            coverArtArchiveClient.requestCoverArt(this.id)?.getFirstImageUrl()
        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> logger.warn("Album cover = [${this.id}] not found. Will return empty.")
                else -> logger.warn(getUnableToFetchCoverMessage(this), e)
            }
            ""
        } catch (e: RuntimeException) {
            logger.error(getUnableToFetchCoverMessage(this), e)
            ""
        }
    }

    private fun getUnableToFetchCoverMessage(release: MusicBrainzArtistResponse.Release): String =
        "Exception caught while fetching album cover = [${release.id}]. Will return empty."

    private suspend fun List<Deferred<ArtistSummary.Album>>.awaitToSet() =
        this.awaitAll().toSet()

    private suspend fun Deferred<WikipediaSummaryResponse?>?.awaitToDescription() =
        this?.await()?.description ?: ""

    private fun MusicBrainzArtistResponse.buildArtistSummaryWith(
        wikipediaSummary: String,
        albums: Set<ArtistSummary.Album>
    ) = ArtistSummary(mbId, name, gender, country, disambiguation, wikipediaSummary, albums)
}