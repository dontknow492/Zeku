package com.ghost.zeku.di

import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailViewModel
import com.ghost.zeku.presentation.viewmodel.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewmodelModule = module {
    viewModel {
        MediaDetailViewModel(repository = get())
    }
    viewModel {
        HomeViewModel(repository = get())
    }

}