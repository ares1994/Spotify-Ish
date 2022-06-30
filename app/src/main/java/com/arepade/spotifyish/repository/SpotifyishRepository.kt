package com.arepade.spotifyish.repository


import androidx.lifecycle.LiveData
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.SearchArtistsQuery
import com.arepade.spotifyish.database.model.Artist
import com.arepade.spotifyish.database.ArtistDatabase
import com.arepade.spotifyish.utils.ARTIST_WORKS_REQUEST_SIZE

class SpotifyIshRepository(private val client: ApolloClient, private val database: ArtistDatabase) :
    Repository {


    override val bookmarkedArtists: LiveData<List<Artist>> = database.artistsDao.getAllArtists()

    override suspend fun searchArtists(
        size: Int,
        name: String,
        after: String?
    ): Repository.Response<SearchArtistsQuery.Data?> {


        return try {
            val result = client.query(SearchArtistsQuery(name, size, after)).execute()

            val artists = result.data?.search?.artists

            if (artists != null && !result.hasErrors()) {

                Repository.Response.Result(result.data)
            } else {
                Repository.Response.Error(
                    result.errors?.get(0)?.message ?: "Failed to retrieve artists"
                )
            }


        } catch (e: ApolloException) {
            Repository.Response.Error(e.message ?: "Failed to retrieve artists")
        }

    }

    override fun insertArtist(artist: Artist) {
        database.artistsDao.insert(artist)
    }

    override fun deleteArtist(artist: Artist) {
        database.artistsDao.deleteArtist(artist.mbId)
    }


    override fun getArtists(): List<Artist> {
        return database.artistsDao.getArtists()
    }


    override suspend fun getArtistDetails(mbID: String): Repository.Response<LookupQuery.Data?> {

        return try {
            val result = client.query(LookupQuery(mbID, ARTIST_WORKS_REQUEST_SIZE)).execute()

            val artist = result.data?.lookup?.artist

            if (artist != null && !result.hasErrors()) {

                Repository.Response.Result(result.data)
            } else {
                Repository.Response.Error(
                    result.errors?.get(0)?.message ?: "Failed to retrieve artist's information"
                )
            }


        } catch (e: ApolloException) {
            Repository.Response.Error(e.message ?: "Failed to retrieve artist's information")
        }

    }


}

interface Repository {

    val bookmarkedArtists: LiveData<List<Artist>>

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