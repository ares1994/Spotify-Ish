package com.arepade.spotifyish.paging


import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arepade.spotifyish.SearchArtistsQuery
import com.arepade.spotifyish.database.Artist
import com.arepade.spotifyish.repository.SpotifyIshRepository
import com.arepade.spotifyish.utils.REQUEST_SIZE
import java.lang.Exception


class SpotifyIshPagingSource constructor(
    private val repository: SpotifyIshRepository,
    private val query: String
) :
    PagingSource<String, Artist>() {
    override fun getRefreshKey(state: PagingState<String, Artist>): String? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey
                ?: state.closestPageToPosition(it)?.nextKey
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Artist> {


        val key = params.key


        return when (val res = repository.searchArtists(REQUEST_SIZE, query, key)) {
            is SpotifyIshRepository.SpotifyIshResponse.Result -> {
                try {
                    val artists =  res.result?.search?.artists?.nodes?.filterNotNull()!!.map {
                        Artist(it.mbid.toString(),
                            it.rating?.value,
                            it.name,
                            try {
                                it.fanArt?.thumbnails?.get(0)?.url as String?
                            } catch (e: Exception) {
                                null
                            },
                            false)
                    }

                    LoadResult.Page(
                       artists,
                        res.result.search.artists.pageInfo.startCursor,
                        res.result.search.artists.pageInfo.endCursor
                    )
                }catch (e:Exception){
                    LoadResult.Error(e)
                }
            }
            is SpotifyIshRepository.SpotifyIshResponse.Error -> {
                LoadResult.Error(Throwable(res.error))
            }
        }


    }


    companion object {
        const val TAG = "SpotifyIshPagingSource"
    }
}