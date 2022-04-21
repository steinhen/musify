package me.henriquestein.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.testng.annotations.Test


class CoverArtArchiveReleaseGroupResponseTest {

    @Test
    fun `it should get first front image`() {
        val releaseGroupResponse = CoverArtArchiveReleaseGroupResponse(
            setOf(
                CoverArtArchiveReleaseGroupResponse.Image(false, ANOTHER_URL),
                CoverArtArchiveReleaseGroupResponse.Image(true, EXPECTED),
            )
        )

        assertThat(releaseGroupResponse.getFirstImageUrl())
            .isNotNull()
            .isEqualTo(EXPECTED)
    }

    @Test
    fun `it should return null when release has no front image`() {
        val releaseGroupResponse = CoverArtArchiveReleaseGroupResponse(
            setOf(CoverArtArchiveReleaseGroupResponse.Image(false, ANOTHER_URL))
        )

        assertThat(releaseGroupResponse.getFirstImageUrl()).isNull()
    }

    companion object {
        private const val EXPECTED = "htts://expected.com"
        private const val ANOTHER_URL = "htts://another.com"
    }
}