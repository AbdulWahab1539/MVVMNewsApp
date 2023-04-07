package com.androiddevs.mvvmnewsapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androiddevs.mvvmnewsapp.repo.NewsRepo

@Suppress("UNCHECKED_CAST")
class NewsViewModelFactory(private val newsRepo: NewsRepo) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepo = newsRepo) as T
    }
}