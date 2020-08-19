package com.example.platys.login

import androidx.lifecycle.ViewModel
import com.example.platys.di.ViewModelBuilder
import com.example.platys.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class LoginModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun loginFragment(): LoginFragment

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindViewModel(viewModel: LoginViewModel): ViewModel
}