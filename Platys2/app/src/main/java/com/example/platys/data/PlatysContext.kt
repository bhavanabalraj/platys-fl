package com.example.platys.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ContextInfo")
data class PlatysContext @JvmOverloads constructor(
    @PrimaryKey @ColumnInfo(name = "fake") var fake: String =""
)