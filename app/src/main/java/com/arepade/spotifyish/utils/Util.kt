package com.arepade.spotifyish.utils

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import androidx.recyclerview.widget.DiffUtil
import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.R
import com.arepade.spotifyish.database.Artist
import kotlinx.android.synthetic.main.spotify_dialog.*

val handler = Handler()

val COLORS = listOf("#07d2c3", "#9e7be3", "#fb5da5", "#ff9a34", "#6891ff")

const val REQUEST_SIZE = 15
const val ARTIST_WORKS_REQUEST_SIZE = 6


val SearchArtistQueryDiffUtil = object : DiffUtil.ItemCallback<Artist>() {
    override fun areItemsTheSame(
        oldItem: Artist,
        newItem: Artist
    ): Boolean {
        return oldItem.mbId == newItem.mbId
    }

    override fun areContentsTheSame(
        oldItem: Artist,
        newItem: Artist
    ): Boolean {
        return oldItem == newItem
    }
}


val ReleaseDiffUtil = object : DiffUtil.ItemCallback<LookupQuery.Node?>() {
    override fun areItemsTheSame(oldItem: LookupQuery.Node, newItem: LookupQuery.Node): Boolean {
        return "${oldItem.coverArtArchive} ${oldItem.title}" == "${newItem.coverArtArchive} ${newItem.title}"
    }

    override fun areContentsTheSame(oldItem: LookupQuery.Node, newItem: LookupQuery.Node): Boolean {
        return oldItem == newItem
    }
}


fun getProgressDialog(context: Context): Dialog {

    return Dialog(context).apply {
        setContentView(R.layout.spotify_dialog)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val animator = ObjectAnimator.ofPropertyValuesHolder(
            logoImageView,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        )
        animator.duration = 750

        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.REVERSE

        animator.start()

        setCancelable(false)
    }
}


