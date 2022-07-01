package com.arepade.spotifyish.repository

import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.SearchArtistsQuery
import com.arepade.spotifyish.database.model.Artist
import kotlinx.coroutines.flow.Flow

interface Repository {

    val bookmarkedArtists: Flow<List<Artist>>

    suspend fun searchArtists(
        size: Int,
        name: String,
        after: String?
    ): Response<SearchArtistsQuery.Data?>

    fun insertArtist(artist: Artist)

    fun deleteArtist(artist: Artist)

    fun getArtists(): List<Artist>

    suspend fun getArtistDetails(mbID: String): Response<LookupQuery.Data?>

    sealed class Response<T> {
        class Result<T>(val result: T) : Response<T>()
        class Error<T>(val error: String) : Response<T>()
    }

}