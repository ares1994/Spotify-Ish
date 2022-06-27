package com.arepade.spotifyish.repository


import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.SearchArtistsQuery
import com.arepade.spotifyish.database.Artist
import com.arepade.spotifyish.database.ArtistDatabase
import com.arepade.spotifyish.utils.ARTIST_WORKS_REQUEST_SIZE

class SpotifyIshRepository(private val client: ApolloClient, private val database: ArtistDatabase) {

    suspend fun searchArtists(
        size: Int,
        name: String,
        after: String?
    ): SpotifyIshResponse<SearchArtistsQuery.Data?> {


        return try {
            val result = client.query(SearchArtistsQuery(name, size, after)).execute()

            val artists = result.data?.search?.artists

            if (artists != null && !result.hasErrors()) {

                SpotifyIshResponse.Result(result.data)
            } else {
                SpotifyIshResponse.Error(
                    result.errors?.get(0)?.message ?: "Failed to retrieve artists"
                )
            }


        } catch (e: ApolloException) {
            SpotifyIshResponse.Error(e.message ?: "Failed to retrieve artists")
        }

    }

    fun insertArtist(artist: Artist) {
        database.artistsDao.insert(artist)
    }

    fun deleteArtist(artist: Artist) {
        database.artistsDao.deleteArtist(artist.mbId)
    }


    fun getArtists(): List<Artist> {
        return database.artistsDao.getArtists()
    }


    suspend fun getArtistDetails(mbID: String): SpotifyIshResponse<LookupQuery.Data?> {

        return try {
            val result = client.query(LookupQuery(mbID, ARTIST_WORKS_REQUEST_SIZE)).execute()

            val artist = result.data?.lookup?.artist

            if (artist != null && !result.hasErrors()) {

                SpotifyIshResponse.Result(result.data)
            } else {
                SpotifyIshResponse.Error(
                    result.errors?.get(0)?.message ?: "Failed to retrieve artist's information"
                )
            }


        } catch (e: ApolloException) {
            SpotifyIshResponse.Error(e.message ?: "Failed to retrieve artist's information")
        }

    }


    sealed class SpotifyIshResponse<T> {
        class Result<T>(val result: T) : SpotifyIshResponse<T>()
        class Error<T>(val error: String) : SpotifyIshResponse<T>()
    }


}