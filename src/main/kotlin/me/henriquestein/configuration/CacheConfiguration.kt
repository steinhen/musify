package me.henriquestein.configuration

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import me.henriquestein.model.ArtistSummary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
open class CacheConfiguration(
    @Value("\${cache.expireAfter}") private val duration: Duration
) {

    @Bean(name = ["cache"])
    open fun cache(): Cache<String, ArtistSummary> =
        Caffeine.newBuilder()
            .expireAfterWrite(duration)
            .build()

}