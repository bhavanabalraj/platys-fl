package com.example.platys.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.platys.PlatysApplication
import com.example.platys.data.*
import com.example.platys.dummy.DummyPlatysRepoImpl
import com.example.platys.utils.PlatysCacheData
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module(includes = [ApplicationModuleBinds::class])
object ApplicationModule {

    @Provides
    @JvmStatic
    @Singleton
    fun provideApplication() : Application {
        return PlatysApplication()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePlatysDataSource(
        database: PlatysDatabase,
        ioDispatcher: CoroutineDispatcher
    ) : PlatysDataSource {
        return PlatysDataSourceImpl(database.platysDao(), ioDispatcher)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun providePlatysDatabase(context: Context) : PlatysDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PlatysDatabase::class.java,
        "Platys.db"
        ).build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @JvmStatic
    @Singleton
    @Provides
    fun providePlatysCacheData() = PlatysCacheData()
}

@Module
abstract class ApplicationModuleBinds {

    @Singleton
    @Binds
    abstract fun bindRepository(repo: DummyPlatysRepoImpl): PlatysRepository
}