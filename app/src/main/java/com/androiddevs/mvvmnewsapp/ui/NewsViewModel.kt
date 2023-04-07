package com.androiddevs.mvvmnewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.mvvmnewsapp.models.Article
import com.androiddevs.mvvmnewsapp.models.NewsResponse
import com.androiddevs.mvvmnewsapp.repo.NewsRepo
import com.androiddevs.mvvmnewsapp.ui.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application,
    val newsRepo: NewsRepo
) : AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode = countryCode)
    }


    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeNewsSearchCall(query = searchQuery)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>):
            Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { newsResponse ->
                breakingNewsPage++
                if (breakingNewResponse == null) {
                    breakingNewResponse = newsResponse
                } else {
                    val oldArticles = breakingNewResponse?.articles
                    val newArticles = newsResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(
                    breakingNewResponse ?: newsResponse
                )
            }
        }
        return Resource.Error(response.message())
    }


    private fun handleSearchNewsResponse(response: Response<NewsResponse>):
            Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { newsResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = newsResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = newsResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(
                    searchNewsResponse ?: newsResponse
                )
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepo.upsert(article)
    }

    fun getSavedNews() = newsRepo.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepo.deleteArticle(article)
    }

    private suspend fun safeNewsSearchCall(query: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepo
                    .searchNews(
                        searchQuery = query,
                        pageNumber = searchNewsPage
                    )
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(
                    Resource.Error(message = "No Internet Connection")
                )
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(
                    Resource.Error(message = "Network Failure")
                )
                else -> searchNews.postValue(
                    Resource.Error(message = "Conversion Error")
                )
            }
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepo
                    .getBreakingNews(
                        countryCode,
                        pageNumber = breakingNewsPage
                    )
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(
                    Resource.Error(message = "No Internet Connection")
                )
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(
                    Resource.Error(message = "Network Failure")
                )
                else -> breakingNews.postValue(
                    Resource.Error(message = "Conversion Error")
                )
            }
        }
    }

    fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activityNetwork =
                connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activityNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

}