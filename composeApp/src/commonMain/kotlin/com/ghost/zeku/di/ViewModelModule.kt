package com.ghost.zeku.di

import com.ghost.zeku.presentation.viewmodel.detail.MediaDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewmodelModule = module {
    viewModel {
        MediaDetailViewModel(repository = get())
    }
}