package com.example.platys.tagcontext

import androidx.lifecycle.ViewModel
import com.example.platys.di.ViewModelBuilder
import com.example.platys.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ContextTagModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun contextTagFragment(): ContextTagFragment

    @Binds
    @IntoMap
    @ViewModelKey(ContextTagViewModel::class)
    internal abstract fun bindViewModel(viewModel: ContextTagViewModel): ViewModel
}