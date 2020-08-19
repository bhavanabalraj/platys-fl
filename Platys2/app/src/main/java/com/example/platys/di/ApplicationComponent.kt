package com.example.platys.di

import android.content.Context
import com.example.platys.PlatysApplication
import com.example.platys.login.LoginModule
import com.example.platys.register.RegisterModule
import com.example.platys.splash.SplashModule
import com.example.platys.tagcontext.ContextTagModule
import com.example.platys.tagcontext.TagContextWindowModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        SplashModule::class,
        LoginModule::class,
        RegisterModule::class,
        ContextTagModule::class,
        TagContextWindowModule::class
    ])
interface ApplicationComponent: AndroidInjector<PlatysApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }
}