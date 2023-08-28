package com.pawcare.pawcare.services

import android.util.Log
import retrofit2.Call
import retrofit2.Response

internal abstract class Callback<T> : retrofit2.Callback<T> {

    abstract override fun onResponse(call: Call<T>, response: Response<T>)

    override fun onFailure(call: Call<T>, t: Throwable) {
        Log.d(call.request().url.toString(), t.message + " ")
    }

}