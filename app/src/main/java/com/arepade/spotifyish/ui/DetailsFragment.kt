package com.arepade.spotifyish.ui

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arepade.spotifyish.MainActivity
import com.arepade.spotifyish.R
import com.arepade.spotifyish.adapters.ReleaseAdapter
import com.arepade.spotifyish.databinding.FragmentDetailsBinding
import com.arepade.spotifyish.databinding.FragmentHomeBinding
import com.arepade.spotifyish.utils.ReleaseDiffUtil
import com.arepade.spotifyish.utils.getProgressDialog
import com.arepade.spotifyish.viewMoodel.ViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.lang.Exception

@AndroidEntryPoint
class DetailsFragment : Fragment(R.layout.fragment_details) {

    private val args: DetailsFragmentArgs by navArgs()
    private val viewModel by viewModels<ViewModel>()

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private var loadingDialog: Dialog? = null

    private val releaseAdapter = ReleaseAdapter(ReleaseDiffUtil)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDetailsBinding.bind(view)
        loadingDialog = getProgressDialog(requireContext())

        setHasOptionsMenu(true)

        initializeObservers()
        initializeViews()
        defineErrorFunction()


        viewModel.fetchArtistDetails(args.artist.mbId)

    }


    private fun defineErrorFunction() {
        viewModel.setOnError { message, onEnd ->
            val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)

            if (!onEnd) {
                snackBar.apply {

                    setAction("OK") {
                        viewModel.fetchArtistDetails(args.artist.mbId)
                        dismiss()
                    }
                    setActionTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.spotify_green
                        )
                    )
                }
            }

            snackBar.show()


        }
    }


    private fun initializeViews() {
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.details)

        binding.recyclerView.apply {
            adapter = releaseAdapter
        }

        args.artist.apply {
            binding.nameTextView.text = "Name: $name"

            binding.ratingBar.rating = rating?.toFloat() ?: 0F

            binding.boardView.setBackgroundColor(Color.parseColor(args.color))


            if (!image.isNullOrEmpty()) {
                try {
                    Glide
                        .with(binding.artistImageView)
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
        }


    }


    private fun initializeObservers() {
        lifecycleScope.launchWhenResumed {

            viewModel.artistDetails.collectLatest {
                val releases = it?.lookup?.artist?.releases?.nodes

                if (!releases.isNullOrEmpty()) {
                    binding.headerTextView.text = getString(R.string.some_of, args.artist.name)
                }
                releaseAdapter.submitList(releases)
            }
        }


        viewModel.showSpotifyLoader.observe(viewLifecycleOwner) {
            if (it) {
                loadingDialog?.show()
            } else {
                loadingDialog?.dismiss()
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()

        loadingDialog = null
        _binding = null
    }
}