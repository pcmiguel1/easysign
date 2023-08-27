package com.pcmiguel.easysign.services

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

interface ApiInterface {



    class Requests() : Parcelable {

        @SerializedName("signature_request_id")
        var signatureRequestId: String? = null

        @SerializedName("title")
        var title: String? = null

        @SerializedName("original_title")
        var originalTitle: String? = null

        @SerializedName("subject")
        var subject: String? = null

        @SerializedName("message")
        var message: String? = null

        @SerializedName("created_at")
        var createdAt: String? = null

        @SerializedName("is_complete")
        var isComplete: Boolean = false

        @SerializedName("is_declined")
        var isDeclined: Boolean = false

        @SerializedName("has_error")
        var hasError: Boolean = false

        constructor(parcel: Parcel) : this() {
            signatureRequestId = parcel.readString()
            title = parcel.readString()
            originalTitle = parcel.readString()
            subject = parcel.readString()
            message = parcel.readString()
            createdAt = parcel.readString()
            isComplete = parcel.readByte() != 0.toByte()
            isDeclined = parcel.readByte() != 0.toByte()
            hasError = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(signatureRequestId)
            parcel.writeString(title)
            parcel.writeString(originalTitle)
            parcel.writeString(subject)
            parcel.writeString(message)
            parcel.writeString(createdAt)
            parcel.writeByte(if (isComplete) 1 else 0)
            parcel.writeByte(if (isDeclined) 1 else 0)
            parcel.writeByte(if (hasError) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Requests> {
            override fun createFromParcel(parcel: Parcel): Requests {
                return Requests(parcel)
            }

            override fun newArray(size: Int): Array<Requests?> {
                return arrayOfNulls(size)
            }
        }

    }

}