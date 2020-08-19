package com.example.platys.dummy;

import com.example.platys.data.PlatysDataSource
import com.example.platys.data.PlatysRepository
import com.example.platys.utils.PlatysCacheData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import javax.inject.Inject
import com.example.platys.data.Result
import com.example.platys.tagcontext.ContextType

class DummyPlatysRepoImpl @Inject constructor(
    private val platysDataSource: PlatysDataSource,
    private val ioDispatcher:CoroutineDispatcher= Dispatchers.IO,
    private val platysCache:PlatysCacheData
) : PlatysRepository {

    override suspend fun getUserDetails(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    override suspend fun signInUser(email:String,password:String): Result<FirebaseUser?> {
        delay(3000)
        return Result.Success(Firebase.auth.currentUser)
    }

    override suspend fun registerUser(email:String,password:String):Result<FirebaseUser?> {
        delay(3000)
        return Result.Success(Firebase.auth.currentUser)
    }

    override fun getSuggestions(contextType: ContextType): List<String> {
        return listOf("abcde", "ghijk", "qwklm", "polascdhrhb")
    }
}
