package com.pcmiguel.easysign.services

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiAIInterface {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    fun chatAi(@Body jsonObject: JsonObject) : Call<ChatAI>

    class ChatAI {

        @SerializedName("id")
        var id: String? = null

        @SerializedName("choices")
        var choices: List<Choices>? = null

    }

    class Choices {

        @SerializedName("index")
        var index: Int? = null

        @SerializedName("message")
        var message: Message? = null

        @SerializedName("finish_reason")
        var finishReason: String? = null

    }

    class Message {

        @SerializedName("role")
        var role: String? = null

        @SerializedName("content")
        var content: String? = null

    }

}