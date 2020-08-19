package com.example.platys.data

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlatysDataSourceImpl internal constructor(
    private val platysDao: PlatysDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PlatysDataSource {

//    override suspend fun createUser(email: String, password: String): Result<FirebaseUser?> {
//
//    }
}