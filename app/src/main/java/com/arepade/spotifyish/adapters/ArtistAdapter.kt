package com.arepade.spotifyish.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arepade.spotifyish.R
import com.arepade.spotifyish.database.model.Artist
import com.arepade.spotifyish.databinding.ItemArtistsBinding
import com.arepade.spotifyish.utils.COLORS
import com.bumptech.glide.Glide

import java.lang.Exception

class ArtistAdapter(
    diffCallback: DiffUtil.ItemCallback<Artist>
) :
    ListAdapter<Artist, ArtistAdapter.ViewHolder>(
        diffCallback
    ) {


    var onEndOfListReached: (() -> Unit)? = null
    var onBookmarkStateChange: ((artist: Artist, isBookmarked: Boolean) -> Unit)? = null
    var onNavigate: ((artist: Artist?, color: String) -> Unit)? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(currentList, position, onEndOfListReached, onBookmarkStateChange, onNavigate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArtistsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    class ViewHolder constructor(private val binding: ItemArtistsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            list: List<Artist?>,
            position: Int,
            onEndOfListReached: (() -> Unit)?,
            onBookmarkStateChange: ((artist: Artist, isBookmarked: Boolean) -> Unit)?,
            onNavigate: ((artist: Artist, color: String) -> Unit)?
        ) {

            val value = list[position]

            if (value!!.isBookmarked) {
                binding.bookmarkImageView.setImageResource(R.drawable.ic_baseline_bookmark_24)
            }


            if (position == list.size - 1) {
                onEndOfListReached?.invoke()
            }




            binding.containerLayout.setBackgroundColor(Color.parseColor(COLORS[position % COLORS.size]))

            binding.nameTextView.text = value.name


            val image = value.image

            if (!image.isNullOrEmpty()) {
                try {
                    Glide
                        .with(binding.root)
                        .load(image)
                        .centerCrop()
                        .placeholder(R.drawable.dummy_image)
                        .into(binding.artistImageView)
                } catch (e: Exception) {
                    binding.artistImageView.setImageResource(R.drawable.dummy_image)
                }
            } else {
                binding.artistImageView.setImageResource(R.drawable.dummy_image)
            }

            binding.ratingBar.rating = value.rating?.toFloat() ?: 0F




            binding.bookmarkImageView.setOnClickListener {


                value.let { artist -> onBookmarkStateChange?.invoke(artist, value.isBookmarked) }

                value.isBookmarked = !value.isBookmarked

            }

            binding.cardView.setOnClickListener {
                onNavigate?.invoke(value, COLORS[position % COLORS.size])
            }
        }


    }


    interface OnActionListener {
        fun onNavigate()
    }

}