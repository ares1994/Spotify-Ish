package com.arepade.spotifyish.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arepade.spotifyish.LookupQuery
import com.arepade.spotifyish.R
import com.arepade.spotifyish.databinding.ItemReleaseBinding
import com.bumptech.glide.Glide
import java.lang.Exception

class ReleaseAdapter(
    diffCallback: DiffUtil.ItemCallback<LookupQuery.Node?>,
) :
    ListAdapter<LookupQuery.Node?, ReleaseAdapter.ViewHolder>(
        diffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemReleaseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder constructor(private val binding: ItemReleaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            value: LookupQuery.Node?
        ) {

            binding.titleTextView.text = value?.title

            if (value?.coverArtArchive?.images != null && value.coverArtArchive.images.isNotEmpty()) {
                try {
                    Glide
                        .with(binding.root)
                        .load(value.coverArtArchive.images[0]?.thumbnails?.small as String?)
                        .centerCrop()
                        .placeholder(R.drawable.dummy_record)
                        .into(binding.artImageView)
                } catch (e: Exception) {
                    binding.artImageView.setImageResource(R.drawable.dummy_record)
                }
            } else {
                binding.artImageView.setImageResource(R.drawable.dummy_record)
            }




        }


    }

}