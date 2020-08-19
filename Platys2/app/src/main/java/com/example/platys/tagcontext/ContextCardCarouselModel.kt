package com.example.platys.tagcontext

data class ContextCardCarouselModel(
    var backgroundColor: Int,
    var caption: String,
    var message: String,
    var imageId: Int,
    var suggestions: List<String>
)