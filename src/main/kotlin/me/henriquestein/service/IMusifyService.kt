package me.henriquestein.service

import me.henriquestein.model.ArtistSummary

interface IMusifyService {
    suspend fun lookup(artistId: String): ArtistSummary
}