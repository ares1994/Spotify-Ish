package com.arepade.spotifyish.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arepade.spotifyish.MainActivity
import com.arepade.spotifyish.R
import com.arepade.spotifyish.adapters.ArtistAdapter
import com.arepade.spotifyish.databinding.FragmentBookmarkBinding
import com.arepade.spotifyish.databinding.FragmentHomeBinding
import com.arepade.spotifyish.utils.ArtistDiffUtil
import com.arepade.spotifyish.viewMoodel.ViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : Fragment(R.layout.fragment_bookmark) {

    private val viewModel by viewModels<ViewModel>()

    private val artistAdapter: ArtistAdapter =
        ArtistAdapter(ArtistDiffUtil)

    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBookmarkBinding.bind(view)

        initializeViews()
        defineFunctions()
        initializeObservers()

    }


    private fun defineFunctions() {
        artistAdapter.also {

            it.onBookmarkStateChange = { artist, isBookmarked ->
                if (isBookmarked) {
                    viewModel.deleteArtist(artist)
                } else {
                    viewModel.insertArtist(artist)
                }
                artistAdapter.notifyDataSetChanged()
            }

            it.onNavigate = { artist, color ->
               artist?.let{
                   this.findNavController().navigate(BookmarkFragmentDirections
                       .actionBookmarkFragmentToDetailsFragment(it,color))
               }
            }
        }
    }


    private fun initializeViews() {

        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.bookmarks)

        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = artistAdapter

            recycledViewPool.setMaxRecycledViews(0, 0)
        }
    }

    private fun initializeObservers() {
       lifecycleScope.launchWhenResumed {
           viewModel.bookmarkedArtists.observe(viewLifecycleOwner,{
               artistAdapter.submitList(it)
           })
       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}