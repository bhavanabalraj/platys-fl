package com.example.platys.splash

import androidx.lifecycle.ViewModel
import com.example.platys.di.ViewModelBuilder
import com.example.platys.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class SplashModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun splashFragment(): SplashFragment

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    internal abstract fun bindViewModel(viewModel: SplashViewModel): ViewModel
}