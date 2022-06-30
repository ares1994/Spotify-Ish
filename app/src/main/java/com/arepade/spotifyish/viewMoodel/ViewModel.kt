package com.arepade.spotifyish.viewMoodel

import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import androidx.paging.*
import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.database.model.Artist
import com.arepade.spotifyish.paging.SpotifyIshPagingSource
import com.arepade.spotifyish.repository.Repository
import com.arepade.spotifyish.utils.REQUEST_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repo: Repository,
) : ViewModel() {

    val bookmarkedArtists = repo.bookmarkedArtists.asFlow()

    private val _searchedData = MutableLiveData<Set<Artist>>()
    val searchedData: Flow<List<Artist>>
        get() =
            _searchedData.asFlow().combine(bookmarkedArtists) { a, b ->
                a.map {
                    Artist(it.mbId, it.rating, it.name, it.image,
                        b
                            .find { artist -> artist.mbId == it.mbId } != null
                    )
                }
            }

    private val _artistDetails = MutableLiveData<LookupQuery.Data?>()
    val artistDetails: Flow<LookupQuery.Data?> get() = _artistDetails.asFlow()

    private val _showSpotifyLoader = MutableLiveData<Boolean>()
    val showSpotifyLoader: LiveData<Boolean> get() = _showSpotifyLoader

    private val _showBottomLoader = MutableLiveData<Boolean>()
    val showBottomLoader: LiveData<Boolean> get() = _showBottomLoader

    private var after: String? = null
    private var hasMore: Boolean = true
    private var currentQuery: String = ""
    private var onError: ((message: String, onEnd: Boolean) -> Unit)? = null


    private fun spotifyLoaderState(state: Boolean) {
        _showSpotifyLoader.postValue(state)
    }

    private fun bottomLoaderState(state: Boolean) {
        _showBottomLoader.postValue(state)
    }


    fun setOnError(value: (message: String, onEnd: Boolean) -> Unit) {
        onError = value
    }


    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    fun fetchArtists(query: String): LiveData<PagingData<Artist>> {
        return Pager(
            config = PagingConfig(15),
        ) {
            SpotifyIshPagingSource(repo, query)
        }.liveData.cachedIn(viewModelScope)
    }


    fun fetchArtists(query: String? = null) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (query != null && currentQuery != query) {
                    currentQuery = query
                    _searchedData.postValue(emptySet())
                    hasMore = true
                    after = null

                    spotifyLoaderState(true)

                } else {
                    bottomLoaderState(true)
                }




                if (!hasMore) {
                    onError?.invoke("All artists loaded", true)
                    bottomLoaderState(false)
                    return@withContext
                }


                when (val res = repo.searchArtists(REQUEST_SIZE, currentQuery, after)) {
                    is Repository.Response.Error -> {

                        onError?.invoke("${res.error}. Retry?", false)
                        bottomLoaderState(false)
                        spotifyLoaderState(false)
                    }
                    is Repository.Response.Result -> {


                        try {
                            after = res.result?.search?.artists?.pageInfo?.endCursor
                            res.result?.search?.artists?.pageInfo?.hasNextPage?.let { hasMore = it }


                            val set = mutableSetOf<Artist>()

                            _searchedData.value?.let { data -> set.addAll(data) }

                            res.result?.search?.artists?.nodes?.let { artistData ->

                                set.addAll(artistData.filterNotNull().map { i ->
                                    Artist(
                                        i.mbid.toString(),
                                        i.rating?.value,
                                        i.name,
                                        try {
                                            i.fanArt?.thumbnails?.get(0)?.url as String?
                                        } catch (e: Exception) {
                                            null
                                        }
                                    )
                                })
                            }



                            _searchedData.postValue(set)



                            bottomLoaderState(false)
                            spotifyLoaderState(false)


                        } catch (e: Exception) {
                            e.message?.let { error -> onError?.invoke("$error. Retry?", false) }
                            bottomLoaderState(false)
                            spotifyLoaderState(false)
                        }
                    }
                }


            }
        }
    }

    fun fetchArtistDetails(mbID: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                spotifyLoaderState(true)


                when (val res = repo.getArtistDetails(mbID)) {
                    is Repository.Response.Result -> {
                        _artistDetails.postValue(res.result)
                    }

                    is Repository.Response.Error -> {
                        onError?.invoke("${res.error}. Retry?", false)
                    }
                }

                spotifyLoaderState(false)

            }
        }


    }


    fun insertArtist(artist: Artist) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.insertArtist(artist)
            }
        }
    }

    fun deleteArtist(artist: Artist) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repo.deleteArtist(artist)
            }
        }
    }


}