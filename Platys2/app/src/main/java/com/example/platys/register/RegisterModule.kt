package com.example.platys.register

import androidx.lifecycle.ViewModel
import com.example.platys.di.ViewModelBuilder
import com.example.platys.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class RegisterModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun registerFragment(): RegisterFragment

    @Binds
    @IntoMap
    @ViewModelKey(RegisterViewModel::class)
    abstract fun bindViewModel(registerViewModel: RegisterViewModel): ViewModel
}