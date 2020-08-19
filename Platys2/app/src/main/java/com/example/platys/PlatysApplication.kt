package com.example.platys

import com.example.platys.di.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber

open class PlatysApplication: DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }
}