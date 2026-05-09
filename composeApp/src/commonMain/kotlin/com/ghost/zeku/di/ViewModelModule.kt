package com.ghost.zeku.di

import com.ghost.zeku.presentation.viewmodel.category.CategoryViewModel
import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailViewModel
import com.ghost.zeku.presentation.viewmodel.home.HomeViewModel
import com.ghost.zeku.presentation.viewmodel.library.LibraryCategoryViewModel
import com.ghost.zeku.presentation.viewmodel.library.LibraryViewModel
import com.ghost.zeku.presentation.viewmodel.main.MainViewModel
import com.ghost.zeku.presentation.viewmodel.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewmodelModule = module {
    viewModel {
        MainViewModel(get(), get(), get())
    }
    viewModel {
        MediaDetailViewModel(repository = get())
    }
    viewModel {
        HomeViewModel(repository = get())
    }
    viewModel {
        CategoryViewModel(repository = get(), userSettings = get())
    }
    viewModel {
        SearchViewModel(get(), userSettings = get())
    }
    viewModel {
        LibraryCategoryViewModel(
            categoryRepository = get()
        )
    }
    viewModel {
        LibraryViewModel(
            userSettings = get(),
            libraryRepository = get(),
            categoryRepository = get(),
        )
    }

}