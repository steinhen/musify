package me.henriquestein.service

import com.github.benmanes.caffeine.cache.Cache
import me.henriquestein.model.ArtistSummary
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component("cachedMusifyService")
open class CachedMusifyService(
    @Qualifier("musifyService") private val service: IMusifyService,
    @Qualifier("cache") private val cache: Cache<String, ArtistSummary>
) : IMusifyService {

    override suspend fun lookup(artistId: String): ArtistSummary {
        return cache.getIfPresent(artistId) ?: loadAndCache(artistId)
    }

    private suspend fun loadAndCache(artistId: String): ArtistSummary {
        return service.lookup(artistId).also { cache.put(artistId, it) }
    }
}