package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androiddevs.mvvmnewsapp.repo.NewsRepo

@Suppress("UNCHECKED_CAST")
class NewsViewModelFactory(
    val newsRepo: NewsRepo,
    val application: Application
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepo = newsRepo, app = application) as T
    }
}