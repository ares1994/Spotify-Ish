package com.arepade.spotifyish.repository

import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.SearchArtistsQuery
import com.arepade.spotifyish.database.ArtistDatabase
import com.arepade.spotifyish.database.model.Artist
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class FakeRepository(private val database: ArtistDatabase) : Repository {

    override val bookmarkedArtists: Flow<List<Artist>> = database.artistsDao.getAllArtists()


    override suspend fun searchArtists(
        size: Int,
        name: String,
        after: String?
    ): Repository.Response<SearchArtistsQuery.Data?> {
        return when (Random.nextBoolean()) {
            true -> {
                Repository.Response.Result(
                    SearchArtistsQuery.Data(
                        SearchArtistsQuery.Search(
                            SearchArtistsQuery.Artists(
                                listOf(
                                    SearchArtistsQuery.Node(
                                        "Logic", "44", SearchArtistsQuery.Rating(5.0),
                                        SearchArtistsQuery.FanArt(emptyList())
                                    )
                                ),
                                SearchArtistsQuery.PageInfo(
                                    null, null, false
                                )
                            )
                        )
                    )
                )
            }
            false -> {
                Repository.Response.Error(
                    "Failed to retrieve artists"
                )
            }
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
        return when (Random.nextBoolean()) {
            true -> {
                Repository.Response.Result(
                    LookupQuery.Data(
                        LookupQuery.Lookup(
                            LookupQuery.Artist(
                                "US",
                                LookupQuery.Releases(
                                    listOf(
                                        LookupQuery.Node(
                                            LookupQuery.CoverArtArchive(
                                                emptyList()
                                            ),
                                            "The Incredible True Story",
                                            "44"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            }
            false -> {
                Repository.Response.Error(
                    "Failed to retrieve artist's details"
                )
            }
        }
    }

}