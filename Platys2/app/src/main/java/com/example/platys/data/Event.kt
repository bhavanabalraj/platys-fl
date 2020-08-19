package com.example.platys.data

import androidx.lifecycle.Observer

open class Event<out T>(private val content: T) {
    var handled = false
        private set

    fun handle(): T? {
        return if(handled) {
            null
        } else {
            handled = true
            content
        }
    }

    fun peekContent(): T = content
}

class EventObserver<T>(private val onEventUnHandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(t: Event<T>?) {
        t?.handle()?.let {
            onEventUnHandledContent(it)
        }
    }
}