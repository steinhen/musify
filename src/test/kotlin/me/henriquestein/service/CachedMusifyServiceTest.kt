package me.henriquestein.service

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.henriquestein.configuration.CacheConfiguration
import me.henriquestein.model.ArtistSummary
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.time.Duration
import kotlin.time.toKotlinDuration

class CachedMusifyServiceTest {
    private val service: MusifyService = mockk()
    private val artistSummary: ArtistSummary = mockk()

    @BeforeMethod
    fun setup() {
        clearMocks(service)
        coEvery { service.lookup(any()) } returns artistSummary
    }

    @Test
    fun `it caches the lookup response`() = runBlocking {
        val sut = CachedMusifyService(
            service,
            CacheConfiguration(Duration.ofSeconds(1)).cache()
        )

        sut.lookup("1")
        sut.lookup("1")

        coVerify(exactly = 1) { service.lookup("1") }
    }

    @Test
    fun `it expires cache after configured duration`() = runBlocking {
        val duration = Duration.ofNanos(100)

        val sut = CachedMusifyService(
            service,
            CacheConfiguration(duration).cache()
        )

        sut.lookup("1")
        delay(duration.toKotlinDuration().inWholeMilliseconds)
        sut.lookup("1")

        coVerify(exactly = 2) { service.lookup("1") }
    }
}