package me.henriquestein.controller

import me.henriquestein.model.ArtistSummary
import me.henriquestein.service.IMusifyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/artist")
class MusifyController(@Qualifier("cachedMusifyService") private val musifyService: IMusifyService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{artistId}")
    suspend fun getById(@PathVariable artistId: String): ArtistSummary {
        try {
            logger.info("Received request with artist id = [$artistId]")
            return musifyService.lookup(artistId)
        } catch (e: WebClientResponseException) {
            when (e.statusCode) {
                HttpStatus.NOT_FOUND -> throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message, e)
                HttpStatus.BAD_REQUEST -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message, e)
                else -> throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
            }
        } catch (e: RuntimeException) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message, e)
        }
    }

}