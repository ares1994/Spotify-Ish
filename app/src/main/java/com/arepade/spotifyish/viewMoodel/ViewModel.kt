package com.arepade.spotifyish.viewMoodel

import androidx.lifecycle.*
import androidx.lifecycle.ViewModel
import androidx.paging.*
import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.SearchArtistsQuery
import com.arepade.spotifyish.database.Artist
import com.arepade.spotifyish.paging.SpotifyIshPagingSource
import com.arepade.spotifyish.repository.SpotifyIshRepository
import com.arepade.spotifyish.utils.REQUEST_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    private val repo: SpotifyIshRepository,
) : ViewModel() {

    private val _searchedList = MutableLiveData<Set<Artist>>()
    val searchedList: LiveData<Set<Artist>> get() = _searchedList

    private val _artistDetails = MutableLiveData<LookupQuery.Data?>()
    val artistDetails: LiveData<LookupQuery.Data?> get() = _artistDetails

    private val _showSpotifyLoader = MutableLiveData<Boolean>()
    val showSpotifyLoader: LiveData<Boolean> get() = _showSpotifyLoader

    private val _showBottomLoader = MutableLiveData<Boolean>()
    val showBottomLoader: LiveData<Boolean> get() = _showBottomLoader

    private var after: String? = null
    private var hasMore: Boolean = true
    private var currentQuery: String = ""
    var onError: ((message: String, onEnd: Boolean) -> Unit)? = null

    @ExperimentalCoroutinesApi
    @ExperimentalPagingApi
    fun fetchArtists(query: String): LiveData<PagingData<Artist>> {
        return Pager(
            config = PagingConfig(30),
        ) {
            SpotifyIshPagingSource(repo, query)
        }.liveData.cachedIn(viewModelScope)
    }

    private fun spotifyLoaderState(state: Boolean) {
        _showSpotifyLoader.postValue(state)
    }

    private fun bottomLoaderState(state: Boolean) {
        _showBottomLoader.postValue(state)
    }


    fun fetchArtists(query: String? = null) {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (query != null && currentQuery != query) {
                    currentQuery = query
                    _searchedList.postValue(emptySet())
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
                    is SpotifyIshRepository.SpotifyIshResponse.Error -> {

                        onError?.invoke("${res.error}. Retry?", false)
                        bottomLoaderState(false)
                        spotifyLoaderState(false)
                    }
                    is SpotifyIshRepository.SpotifyIshResponse.Result -> {


                        try {
                            after = res.result?.search?.artists?.pageInfo?.endCursor
                            res.result?.search?.artists?.pageInfo?.hasNextPage?.let { hasMore = it }


                            val set = mutableSetOf<Artist>()

                            _searchedList.value?.let { data -> set.addAll(data) }

                            res.result?.search?.artists?.nodes?.let { artistData ->

                                set.addAll(artistData.filterNotNull().map { i ->
                                    Artist(i.mbid.toString(),
                                        i.rating?.value,
                                        i.name,
                                        try {
                                            i.fanArt?.thumbnails?.get(0)?.url as String?
                                        } catch (e: Exception) {
                                            null
                                        },
                                        repo.getArtists()
                                            .find { artist -> artist.mbId == i.mbid } != null)
                                })
                            }



                            _searchedList.postValue(set)



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


    fun fetchArtistDetails(mbID: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                spotifyLoaderState(true)


                when (val res = repo.getArtistDetails(mbID)) {
                    is SpotifyIshRepository.SpotifyIshResponse.Result -> {
                        _artistDetails.postValue(res.result)
                    }

                    is SpotifyIshRepository.SpotifyIshResponse.Error -> {
                        onError?.invoke(res.error, false)
                    }
                }

                spotifyLoaderState(false)

            }
        }


    }


}