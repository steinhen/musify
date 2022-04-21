package me.henriquestein.configuration

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
open class CoverArtArchiveConfiguration(
    @Value("\${coverArtArchive.baseUrl}") private val baseUrl: String
) : WebClientConfiguration() {

    @Bean(name = [WEB_CLIENT])
    open fun webClient(): WebClient = createWebClient(baseUrl)

    companion object {
        const val WEB_CLIENT = "caaWebClient"
    }
}

@Configuration
open class MusicBrainzConfiguration(
    @Value("\${musicBraiz.baseUrl}") private val baseUrl: String
) : WebClientConfiguration() {

    @Bean(name = [WEB_CLIENT])
    open fun webClient(): WebClient = createWebClient(baseUrl)

    companion object {
        const val WEB_CLIENT = "musicBrainzWebClient"
    }
}

@Configuration
open class WikidataConfiguration(
    @Value("\${wikidata.baseUrl}") private val baseUrl: String
) : WebClientConfiguration() {

    @Bean(name = [WEB_CLIENT])
    open fun webClient(): WebClient = createWebClient(
        baseUrl,
        createWikidataExchangeStrategies()
    )

    private fun createWikidataExchangeStrategies(): ExchangeStrategies {
        val size = 16 * 1024 * 1024
        return ExchangeStrategies.builder()
            .codecs { codecs: ClientCodecConfigurer ->
                codecs.defaultCodecs().maxInMemorySize(size)
            }
            .build()
    }

    companion object {
        const val WEB_CLIENT = "wikidataWebClient"
    }
}

@Configuration
open class WikipediaConfiguration(
    @Value("\${wikipedia.baseUrl}") private val baseUrl: String
) : WebClientConfiguration() {

    @Bean(name = [WEB_CLIENT])
    open fun webClient(): WebClient = createWebClient(baseUrl)

    companion object {
        const val WEB_CLIENT = "wikipediaWebClient"
    }

}

open class WebClientConfiguration {

    fun createWebClient(
        basUrl: String,
        exchangeStrategies: ExchangeStrategies? = null
    ): WebClient {
        val builder = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(buildHttpClient()))
            .baseUrl(basUrl)
            .defaultHeaders {
                it.add(HttpHeaders.USER_AGENT, USER_AGENT)
                it.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            }

        exchangeStrategies?.let { builder.exchangeStrategies(it) }

        return builder.build()
    }

    private fun buildHttpClient() = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofMillis(5000))
        .followRedirect(true)
        .doOnConnected { conn ->
            conn.addHandlerLast(ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                .addHandlerLast(WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
        }

    companion object {
        private const val USER_AGENT = "musify / stein.henrique@gmail.com"
    }
}
