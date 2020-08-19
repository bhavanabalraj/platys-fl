package com.example.platys.data

import com.example.platys.tagcontext.ContextType
import com.google.firebase.auth.FirebaseUser

interface PlatysRepository {
    suspend fun getUserDetails(): FirebaseUser?

    suspend fun signInUser(email: String, password: String): Result<FirebaseUser?>

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser?>

    fun getSuggestions(contextType: ContextType): List<String>
}