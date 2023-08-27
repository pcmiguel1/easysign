package com.pcmiguel.easysign.services

import android.content.SharedPreferences
import com.pcmiguel.easysign.BuildConfig
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class BackOffice(
    private val backendInstance: Backend
) {

    private var retrofit: Retrofit? = null
    private var apiInterface: ApiInterface
    private val preferences: SharedPreferences = backendInstance.preferences

    init {
        apiInterface = getClient().create(ApiInterface::class.java)
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


}