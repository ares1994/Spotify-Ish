package com.arepade.spotifyish.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arepade.spotifyish.R
import com.arepade.spotifyish.adapters.ArtistAdapter
import com.arepade.spotifyish.databinding.FragmentHomeBinding
import com.arepade.spotifyish.utils.ArtistDiffUtil
import com.arepade.spotifyish.utils.getProgressDialog
import com.arepade.spotifyish.utils.handler
import com.arepade.spotifyish.viewMoodel.ViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var inputManager: InputMethodManager? = null

    private val viewModel by viewModels<ViewModel>()

    private val artistAdapter: ArtistAdapter =
        ArtistAdapter(ArtistDiffUtil)


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var loadingDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setHasOptionsMenu(true)

        inputManager =
            requireActivity().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        loadingDialog = getProgressDialog(requireContext())


        defineFunctions()
        initializeObservers()
        initializeViews()


    }


    private fun defineFunctions() {

        artistAdapter.also {
            it.onEndOfListReached = {
                commenceSearch()
            }

            it.onBookmarkStateChange = { artist, isBookmarked ->
                if (isBookmarked) {
                    viewModel.deleteArtist(artist)
                } else {
                    viewModel.insertArtist(artist)
                }

                artistAdapter.notifyDataSetChanged()
            }

            it.onNavigate = { artist, color ->
                artist?.let {
                    this.findNavController()
                        .navigate(
                            HomeFragmentDirections.actionHomeFragmentToDetailsFragment(
                                it,
                                color
                            )
                        )
                }
            }
        }


        viewModel.onError = { message, onEnd ->
            if (onEnd) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {

                    setAction("OK") {
                        commenceSearch()
                        dismiss()
                    }
                    setActionTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.spotify_green
                        )
                    )


                }.show()
            }


        }
    }


    private fun initializeViews() {
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = artistAdapter

            recycledViewPool.setMaxRecycledViews(0, 0)

        }


    }

    private fun initializeObservers() {

        viewModel.searchedList.observe(viewLifecycleOwner, { data ->
            lifecycleScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    artistAdapter.submitList(viewModel.mapBookmarkState(data.toList()))
                    handler.postDelayed({
                        artistAdapter.notifyDataSetChanged()
                    }, 1000)
                }
            }
        })



        viewModel.showSpotifyLoader.observe(viewLifecycleOwner, {
            if (it) {
                loadingDialog?.show()
            } else {
                loadingDialog?.dismiss()
            }
        })


        viewModel.showBottomLoader.observe(viewLifecycleOwner, {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.home_menu, menu)

        val menuItem = menu.findItem(R.id.app_bar_search)

        val searchView = menuItem.actionView as SearchView

        searchView.queryHint = "Find Artists"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {


                inputManager?.hideSoftInputFromWindow(view?.windowToken, 0)
                menuItem.collapseActionView()


                handler.postDelayed({
                    commenceSearch(query)
                }, 100)

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.bookmark -> {
                this.findNavController()
                    .navigate(HomeFragmentDirections.actionHomeFragmentToBookmarkFragment())
                return true
            }

        }


        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        inputManager = null
        _binding = null
    }


    private fun commenceSearch(query: String? = null) {
        viewModel.fetchArtists(query)
    }

}