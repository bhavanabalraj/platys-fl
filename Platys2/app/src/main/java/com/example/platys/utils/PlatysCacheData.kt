package com.example.platys.utils

import com.google.firebase.auth.FirebaseUser

class PlatysCacheData {
    var userDetails: FirebaseUser? = null
    var contextTagList: HashMap<String, ContextTagModel> = HashMap()
    var countMap = HashMap<String, Int>()
    var activityContextList: List<String>? = null
    var peopleContextList: List<String>? = null
    var placeContextList: List<String>? = null
    var emotionContextList: List<String>? = null
    val activityConstant = "Activity"
    val placeConstant = "Place"
    val peopleConstant = "People"
    val emotionConstant = "Emotion"
}