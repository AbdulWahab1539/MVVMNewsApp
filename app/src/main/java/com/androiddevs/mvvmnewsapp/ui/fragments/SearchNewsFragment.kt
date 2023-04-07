package com.androiddevs.mvvmnewsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.ui.NewsActivity
import com.androiddevs.mvvmnewsapp.ui.NewsViewModel
import com.androiddevs.mvvmnewsapp.ui.util.Constants
import com.androiddevs.mvvmnewsapp.ui.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.androiddevs.mvvmnewsapp.ui.util.Resource
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var searchNewsAdapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = (activity as NewsActivity).viewModel
        setUpRecyclerView()

        searchNewsAdapter.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }

        var job: Job? = null
        etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    hideProgressbar()
                    it.data?.let { newsResponse ->
                        searchNewsAdapter.differ
                            .submitList(newsResponse.articles)
                    }
                }


                is Resource.Error -> {
                    hideProgressbar()
                    it.message?.let { message ->
                        Log.e(Constants.TAG, "onViewCreated: $message")
                    }
                }

                is Resource.Loading -> {
                    showProgressbar()
                }

            }
        })
    }


    private fun showProgressbar() {
        paginationProgressBar.visibility = View.VISIBLE
    }


    private fun hideProgressbar() {
        paginationProgressBar.visibility = View.GONE
    }

    private fun setUpRecyclerView() {
        searchNewsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = searchNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

}
