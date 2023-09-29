package com.pcmiguel.easysign.services

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Callback
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.BuildConfig
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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

                /*val originalRequest = chain.request()
                val request: Request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)*/

                val username = token
                val password = ""
                val credentials = "$username:$password"
                val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

                val originalRequest = chain.request()
                val request: Request = originalRequest.newBuilder()
                    .header("Authorization", basicAuth)
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

    fun createEmbeddedSignatureRequest(listener: Listener<Any>?, json: JsonObject) {

        apiInterface.createEmbeddedSignatureRequest(json).enqueue(object : retrofit2.Callback<ApiInterface.CreateEmbeddedSignatureRequest> {
            override fun onResponse(call: Call<ApiInterface.CreateEmbeddedSignatureRequest>, response: Response<ApiInterface.CreateEmbeddedSignatureRequest>) {

                if (response.isSuccessful) {
                    listener?.onResponse(response.body()!!)
                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

            override fun onFailure(call: Call<ApiInterface.CreateEmbeddedSignatureRequest>, t: Throwable) {
                clientError(t, null)
            }

        })

    }

    fun createEmbeddedSignatureRequestWithTemplate(listener: Listener<Any>?, json: JsonObject) {

        apiInterface.createEmbeddedSignatureRequestWithTemplate(json).enqueue(object : retrofit2.Callback<ApiInterface.CreateEmbeddedSignatureRequest> {
            override fun onResponse(call: Call<ApiInterface.CreateEmbeddedSignatureRequest>, response: Response<ApiInterface.CreateEmbeddedSignatureRequest>) {

                if (response.isSuccessful) {
                    listener?.onResponse(response.body()!!)
                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

            override fun onFailure(call: Call<ApiInterface.CreateEmbeddedSignatureRequest>, t: Throwable) {
                clientError(t, null)
            }

        })

    }

    fun createEmbeddedTemplateDraft(listener: Listener<Any>?, json: JsonObject) {

        apiInterface.createEmbeddedTemplateDraft(json).enqueue(object : retrofit2.Callback<ApiInterface.CreateEmbeddedDraftRequest> {
            override fun onResponse(call: Call<ApiInterface.CreateEmbeddedDraftRequest>, response: Response<ApiInterface.CreateEmbeddedDraftRequest>) {

                if (response.isSuccessful) {
                    listener?.onResponse(response.body()!!)
                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

            override fun onFailure(call: Call<ApiInterface.CreateEmbeddedDraftRequest>, t: Throwable) {
                clientError(t, null)
            }

        })

    }

    fun deleteTemplate(listener : Listener<Any>?, templateId : String) {

        apiInterface.deleteTemplate(templateId).enqueue(object : Callback<Void>() {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

                if (response.isSuccessful)  {
                    listener?.onResponse(null)
                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

        })

    }

    fun cancelSignatureRequest(listener : Listener<Any>?, signatureRequestId : String) {

        apiInterface.cancelSignatureRequest(signatureRequestId).enqueue(object : Callback<Void>() {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

                if (response.isSuccessful)  {
                    listener?.onResponse(null)
                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

        })

    }

    fun downloadFilesDataUri(listener: Listener<Any>?, signatureRequestId : String) {

        apiInterface.downloadFilesDataUri(signatureRequestId).enqueue(object : Callback<JsonObject>() {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                if (response.isSuccessful) {

                    try {

                        listener?.onResponse(response.body()!!.asJsonObject)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

        })

    }

    fun getTemplateFilesDataUri(listener: Listener<Any>?, templateId : String) {

        apiInterface.getTemplateFilesDataUri(templateId).enqueue(object : Callback<JsonObject>() {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                if (response.isSuccessful) {

                    try {

                        listener?.onResponse(response.body()!!.asJsonObject)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

        })

    }

    fun getEmbeddedSignURL(listener : Listener<Any>?, signatureId: String) {

        apiInterface.getEmbeddedSignURL(signatureId).enqueue(object : retrofit2.Callback<ApiInterface.EmbeddedResponse> {
            override fun onResponse(
                call: Call<ApiInterface.EmbeddedResponse>,
                response: Response<ApiInterface.EmbeddedResponse>
            ) {

                if (response.isSuccessful) {
                    listener?.onResponse(response.body()!!)
                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }
            }

            override fun onFailure(call: Call<ApiInterface.EmbeddedResponse>, t: Throwable) {
                clientError(t, null)
            }
        })

    }

    fun getEmbeddedTemplateEditUrl(listener : Listener<Any>?, json: JsonObject, templateId: String) {

        apiInterface.getEmbeddedTemplateEditUrl(json, templateId).enqueue(object : retrofit2.Callback<ApiInterface.EmbeddedResponse> {
            override fun onResponse(
                call: Call<ApiInterface.EmbeddedResponse>,
                response: Response<ApiInterface.EmbeddedResponse>
            ) {

                if (response.isSuccessful) {
                    listener?.onResponse(response.body()!!)
                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }
            }

            override fun onFailure(call: Call<ApiInterface.EmbeddedResponse>, t: Throwable) {
                clientError(t, null)
            }
        })

    }

    fun getAccount(listener: Listener<Any>?, email: String) {

        apiInterface.getAccount(email).enqueue(object : retrofit2.Callback<ApiInterface.AccountResponse> {
            override fun onResponse(
                call: Call<ApiInterface.AccountResponse>,
                response: Response<ApiInterface.AccountResponse>
            ) {
                if (response.isSuccessful) {

                    listener?.onResponse(response.body()!!)

                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }
            }

            override fun onFailure(call: Call<ApiInterface.AccountResponse>, t: Throwable) {
                clientError(t, null)
            }
        })

    }

    fun createAccount(listener: Listener<Any>?, json: JsonObject) {

        apiInterface.createAccount(json).enqueue(object : retrofit2.Callback<ApiInterface.AccountResponse> {
            override fun onResponse(
                call: Call<ApiInterface.AccountResponse>,
                response: Response<ApiInterface.AccountResponse>
            ) {

                if (response.isSuccessful) {

                    listener?.onResponse(response.body()!!)

                }
                else {
                    val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
                    if (jsonObj.has("error")) {
                        val errorObject = jsonObj.getJSONObject("error")
                        val errorMsg = errorObject.getString("error_msg")
                        listener?.onResponse(errorMsg)
                    }
                }

            }

            override fun onFailure(call: Call<ApiInterface.AccountResponse>, t: Throwable) {
                clientError(t, null)
            }

        })

    }

    fun listSignatureRequests(listener: Listener<Any>?, page: Int, pageSize: Int, query: String) {

        apiInterface.listSignatureRequests(
            App.instance.preferences.getString("AccountId", "")!!,
            page,
            pageSize,
            query
        ).enqueue(object : retrofit2.Callback<ApiInterface.SignatureRequests> {
            override fun onResponse(
                call: Call<ApiInterface.SignatureRequests>,
                response: Response<ApiInterface.SignatureRequests>
            ) {

                if (response.isSuccessful) {

                    listener?.onResponse(response.body())

                }
                else {
                    serverError(call, response, listener)
                }

            }

            override fun onFailure(call: Call<ApiInterface.SignatureRequests>, t: Throwable) {
                clientError(t, null)
            }

        })

    }

    fun listTemplates(listener: Listener<Any>?, page: Int, pageSize: Int, query: String) {

        apiInterface.listTemplates(
            App.instance.preferences.getString("AccountId", "")!!,
            page,
            pageSize,
            query
        ).enqueue(object : retrofit2.Callback<ApiInterface.TemplateRequests> {
            override fun onResponse(
                call: Call<ApiInterface.TemplateRequests>,
                response: Response<ApiInterface.TemplateRequests>
            ) {

                if (response.isSuccessful) {

                    listener?.onResponse(response.body())

                }
                else {
                    serverError(call, response, listener)
                }

            }

            override fun onFailure(call: Call<ApiInterface.TemplateRequests>, t: Throwable) {
                clientError(t, null)
            }

        })

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