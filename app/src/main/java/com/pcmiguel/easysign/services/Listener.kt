package com.pawcare.pawcare.services

interface Listener<T> {

    fun onResponse(response: T?)

}