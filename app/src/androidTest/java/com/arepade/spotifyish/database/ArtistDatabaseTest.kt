package com.arepade.spotifyish.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arepade.spotifyish.database.model.Artist
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class ArtistDatabaseTest {
    private lateinit var artistDao: ArtistsDao
    private lateinit var db: ArtistDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ArtistDatabase::class.java
        ).build()
        artistDao = db.artistsDao
        addItem()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }


    @Test
    @Throws(Exception::class)
    fun insertAndGetArtist() {
        val first = Artist("aerdt-3ju43-fderrf-35gff-3huyb", 5.0, "Logic", "", false, 2)
        artistDao.insert(first)

        val second = artistDao.getArtist(first.mbId)


        assertThat(first, equalTo(second))

    }


    @Test
    @Throws(Exception::class)
    fun deleteArtist() {
        artistDao.deleteArtist(TEST_ARTIST.mbId)

        val artist = artistDao.getArtist(TEST_ARTIST.mbId)

        assertThat(artist, equalTo(null))

    }


    @Test
    @Throws(Exception::class)
    fun clearDb() {
        artistDao.clear()

        assertThat(artistDao.getArtists().size, equalTo(0))
    }


    private fun addItem() {
        val artist = TEST_ARTIST
        artistDao.insert(artist)
    }

    companion object {
        val TEST_ARTIST = Artist("aaaaa-bbbbb-ccccc-dddddd-eeeeee", 5.0, "Eminem", "", false, 1)
    }

}