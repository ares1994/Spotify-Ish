package com.arepade.spotifyish.viewMoodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.asLiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.arepade.spotifyish.database.ArtistDatabase
import com.arepade.spotifyish.database.ArtistDatabaseTest
import com.arepade.spotifyish.database.ArtistsDao
import com.arepade.spotifyish.database.model.Artist
import com.arepade.spotifyish.repository.FakeRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class ViewModelTest {
    private lateinit var db: ArtistDatabase
    private lateinit var repo: FakeRepository
    private lateinit var viewModel: ViewModel


    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ArtistDatabase::class.java
        ).build()

        repo = FakeRepository(db)

        repo.insertArtist(
            ArtistDatabaseTest.TEST_ARTIST
        )

        viewModel = ViewModel(repo)

    }

    @ExperimentalCoroutinesApi
    @Test
    fun searchQueryValidateFlow(): Unit = runBlocking(Dispatchers.Default) {


        viewModel.fetchArtists()

        val actualData = FakeRepository.artistList[0]
        val flowData = viewModel.searchedData.first()[0]

        launch {
            assert(actualData.mbid == flowData.mbId)
            this.cancel()
        }

    }


    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }


}