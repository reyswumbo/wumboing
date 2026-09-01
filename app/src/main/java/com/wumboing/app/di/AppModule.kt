package com.wumboing.app.di

import android.content.Context
import com.wumboing.app.data.local.LocalStore
import com.wumboing.app.data.repository.ComicRepository
import com.wumboing.app.data.source.HttpFetcher
import com.wumboing.app.data.source.aw.AwSource
import com.wumboing.app.data.source.wz.WzSource
import com.wumboing.app.ui.detail.DetailViewModel
import com.wumboing.app.ui.home.HomeViewModel
import com.wumboing.app.ui.library.LibraryViewModel
import com.wumboing.app.ui.reader.ReaderViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<OkHttpClient> { HttpFetcher.createClient() }
    single { HttpFetcher(get()) }

    single { WzSource(get()) }
    single { AwSource(get()) }

    single {
        ComicRepository(mapOf("wz" to get<WzSource>(), "aw" to get<AwSource>()))
    }

    single { LocalStore(androidContext()) }

    viewModel { HomeViewModel(get()) }
    viewModel { DetailViewModel(get(), get()) }
    viewModel { LibraryViewModel(get()) }
    viewModel { ReaderViewModel(get(), get()) }
}
