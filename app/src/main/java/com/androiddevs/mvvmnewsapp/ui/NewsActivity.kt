package com.androiddevs.mvvmnewsapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.db.ArticleDatabase
import com.androiddevs.mvvmnewsapp.repo.NewsRepo
import kotlinx.android.synthetic.main.activity_news.*

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        val repository = NewsRepo(ArticleDatabase(this))

        val viewModelProviderFactory = NewsViewModelFactory(
            newsRepo = repository, application = application
        )

        viewModel = ViewModelProvider(
            this, viewModelProviderFactory
        ).get(NewsViewModel::class.java)


        bottomNavigationView.setupWithNavController(
            newsNavHostFragment.findNavController()
        )
    }
}
