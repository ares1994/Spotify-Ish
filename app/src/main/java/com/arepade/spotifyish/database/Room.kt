package com.arepade.spotifyish.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.arepade.spotifyish.database.model.Artist


@Dao
interface ArtistsDao {
    @Query("select * from artist where mbId = :mbId")
    fun getArtist(mbId: String): Artist

    @Query("select * from artist")
    fun getAllArtists(): LiveData<List<Artist>>

    @Query("select * from artist")
    fun getArtists(): List<Artist>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg artists: Artist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artist: Artist)

    @Query("DELETE FROM artist")
    fun clear()

    @Query("DELETE FROM artist where mbId = :mbId")
    fun deleteArtist(mbId: String)

}



@Database(entities = [Artist::class], version = 1, exportSchema = false)
abstract class ArtistDatabase : RoomDatabase() {
    abstract val artistsDao: ArtistsDao
}


private lateinit var INSTANCE: ArtistDatabase

fun getDatabase(context: Context): ArtistDatabase {
    synchronized(ArtistDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                ArtistDatabase::class.java,
                "artist"
            ).build()
        }
    }
    return INSTANCE
}