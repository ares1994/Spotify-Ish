package com.arepade.spotifyish.database.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Artist(


    @ColumnInfo
    val mbId: String,

    @ColumnInfo
    val rating: Double? = null,

    @ColumnInfo
    val name: String? = null,

    @ColumnInfo
    val image: String? = null,

    @ColumnInfo
    var isBookmarked: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val no: Int = 0
    ) : Parcelable