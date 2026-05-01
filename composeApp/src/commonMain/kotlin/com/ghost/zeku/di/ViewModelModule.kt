package com.ghost.zeku.di

import com.ghost.zeku.presentation.viewmodel.category.CategoryViewModel
import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailViewModel
import com.ghost.zeku.presentation.viewmodel.home.HomeViewModel
import com.ghost.zeku.presentation.viewmodel.main.MainViewModel
import com.ghost.zeku.presentation.viewmodel.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewmodelModule = module {
    viewModel {
        MainViewModel(get(), get())
    }
    viewModel {
        MediaDetailViewModel(repository = get())
    }
    viewModel {
        HomeViewModel(repository = get())
    }
    viewModel {
        CategoryViewModel(repository = get())
    }
    viewModel {
        SearchViewModel(get())
    }

}