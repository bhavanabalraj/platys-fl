package com.example.platys.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PlatysContext::class], version = 1, exportSchema = false)
abstract class PlatysDatabase: RoomDatabase() {
    abstract fun platysDao(): PlatysDao
}