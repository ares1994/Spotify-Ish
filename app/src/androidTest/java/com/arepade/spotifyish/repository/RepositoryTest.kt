package com.arepade.spotifyish.repository

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.arepade.spotifyish.database.ArtistDatabase
import com.arepade.spotifyish.database.ArtistDatabaseTest.Companion.TEST_ARTIST
import com.arepade.spotifyish.database.ArtistsDao
import com.arepade.spotifyish.database.model.Artist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RepositoryTest {

    private lateinit var db: ArtistDatabase
    private lateinit var repo: FakeRepository

    private val artist = Artist(
        "aeee-rdd45-ggggg-rvb45-ggtre", 4.0, "Eminem", "", true, 2
    )


    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ArtistDatabase::class.java
        ).build()

        repo = FakeRepository(db)

        repo.insertArtist(
            TEST_ARTIST
        )

    }


    @ExperimentalCoroutinesApi
    @Test
    fun insertArtistAndValidateFlow() = runBlocking {

        repo.bookmarkedArtists.test {
            val value1 = awaitItem()
            assert(value1.size == 1)

            repo.insertArtist(artist)

            val value2 = awaitItem()
            assert(value2.size == 2)
        }
    }


    @Test
    fun insertAndValidate() {
        assert(repo.getArtists().find { it.mbId == artist.mbId } == null)

        repo.insertArtist(artist)

        assert(repo.getArtists().find { it.mbId == artist.mbId } != null)


    }


    @Test
    fun deleteAndValidate() {

        assert(repo.getArtists().find { it.mbId == TEST_ARTIST.mbId } != null)

        repo.deleteArtist(TEST_ARTIST)

        assert(repo.getArtists().find { it.mbId == TEST_ARTIST.mbId } == null)
    }


    @Test
    fun getArtists(): Unit = runBlocking {

        when (val res = repo.searchArtists(15, "Justin Beiber", null)) {

            is Repository.Response.Error -> {
                assert(res.error == "Failed to retrieve artists")
            }
            is Repository.Response.Result -> {
                res.result?.search?.artists?.nodes?.isNotEmpty()?.let { assert(it) }
            }
        }

    }


    @Test
    fun getArtistDetails(): Unit = runBlocking {


        when (val res = repo.getArtistDetails("54")) {

            is Repository.Response.Error -> {
                assert(res.error == "Failed to retrieve artists")
            }
            is Repository.Response.Result -> {
                res.result?.lookup?.artist?.releases?.nodes?.isNotEmpty()?.let { assert(it) }
            }
        }
    }


    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

}