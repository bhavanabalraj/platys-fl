package com.example.platys.data

import com.example.platys.tagcontext.ContextType
import com.example.platys.utils.PlatysCacheData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlatysRepositoryImpl @Inject constructor(
    private val platysDataSource: PlatysDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val platysCache: PlatysCacheData
) : PlatysRepository {

    override suspend fun getUserDetails(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    override suspend fun signInUser(email: String, password: String): Result<FirebaseUser?> {
        return withContext(ioDispatcher) {
            suspendCoroutine<Result<FirebaseUser?>> { continuation ->
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful) {
                            platysCache.userDetails = Firebase.auth.currentUser
                            continuation.resume(Result.Success(Firebase.auth.currentUser))
                        } else {
                            continuation.resume(Result.Failure(Exception("Account creation failed.")))
                        }
                    }
            }
        }
    }

    override suspend fun registerUser(email: String, password: String): Result<FirebaseUser?> {
        return withContext(ioDispatcher) {
            suspendCoroutine<Result<FirebaseUser?>> { continuation ->
                Firebase.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{ task ->
                        if(task.isSuccessful) {
                            platysCache.userDetails = Firebase.auth.currentUser
                            continuation.resume(Result.Success(Firebase.auth.currentUser))
                        } else {
                            continuation.resume(Result.Failure(Exception("Account creation failed.")))
                        }
                    }
            }
        }
    }

    override fun getSuggestions(contextType: ContextType): List<String> {
        return listOf()
    }
}