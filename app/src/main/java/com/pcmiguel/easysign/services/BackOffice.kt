package com.pcmiguel.easysign.services

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.BuildConfig
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackOffice(
    private val backendInstance: Backend
) {

    private var retrofit: Retrofit? = null
    private var retrofitAI: Retrofit? = null

    private var apiInterface: ApiInterface
    private var apiAIInterface: ApiAIInterface
    private val preferences: SharedPreferences = backendInstance.preferences

    init {
        apiInterface = getClient().create(ApiInterface::class.java)
        apiAIInterface = getClientAI().create(ApiAIInterface::class.java)
    }

    private fun getClient(): Retrofit {

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .addInterceptor { chain ->

                val token = BuildConfig.API_KEY

                val originalRequest = chain.request()
                val request: Request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)

            }
            .build()

        if (retrofit == null)
            retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

        return retrofit!!

    }

    private fun getClientAI(): Retrofit {

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .addInterceptor { chain ->

                val token = BuildConfig.API_AI_KEY

                val originalRequest = chain.request()
                val request: Request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)

            }
            .build()

        if (retrofitAI == null)
            retrofitAI = Retrofit.Builder()
                .baseUrl(BuildConfig.SERVICE_URL_AI)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

        return retrofitAI!!

    }

    fun chatAI(listener: Listener<Any>?, json: JsonObject) {

        apiAIInterface.chatAi(json).enqueue(object : retrofit2.Callback<ApiAIInterface.ChatAI> {
            override fun onResponse(
                call: Call<ApiAIInterface.ChatAI>,
                response: Response<ApiAIInterface.ChatAI>
            ) {
                if (response.isSuccessful) {
                    listener?.onResponse(response.body())
                }
                else {
                    serverError(call, response, listener)
                }
            }

            override fun onFailure(call: Call<ApiAIInterface.ChatAI>, t: Throwable) {
                clientError(t, null)
            }

        })

    }

    private fun serverError(call: Call<*>, response: retrofit2.Response<*>, listener: Listener<Any>?) {
        try {
            val errorMessage = if (response.code() == 403) "403" else response.errorBody()!!.string()

            Log.d("Error ", call.request().url.toString() + "\n\t" + errorMessage)

            listener?.onResponse(errorMessage)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun clientError(t: Throwable, listener: Listener<Any>?) {
        t.printStackTrace()

        listener?.onResponse("error")
    }


}