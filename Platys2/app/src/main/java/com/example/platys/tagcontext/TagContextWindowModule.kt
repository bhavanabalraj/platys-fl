package com.example.platys.tagcontext

import com.example.platys.di.ViewModelBuilder
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TagContextWindowModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun tagContextWindowFragment(): TagContextWindowFragment
}