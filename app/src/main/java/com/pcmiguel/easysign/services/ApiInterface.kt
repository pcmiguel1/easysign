package com.pcmiguel.easysign.services

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("account/create")
    fun createAccount(@Body jsonObject: JsonObject) : Call<AccountResponse>

    @Headers("Content-Type: application/json")
    @GET("signature_request/files_as_data_uri/{signature_request_id}")
    fun downloadFilesDataUri(
        @Path("signature_request_id") signature_request_id: String
    ) : Call<JsonObject>

    @Headers("Content-Type: application/json")
    @GET("template/files_as_data_uri/{template_id}")
    fun getTemplateFilesDataUri(
        @Path("template_id") template_id: String
    ) : Call<JsonObject>

    @Headers("Content-Type: application/json")
    @GET("embedded/sign_url/{signature_id}")
    fun getEmbeddedSignURL(
        @Path("signature_id") signature_id: String
    ) : Call<EmbeddedResponse>

    @Headers("Content-Type: application/json")
    @GET("account")
    fun getAccount(
        @Query("email_address") email_address : String
    ) : Call<AccountResponse>

    @Headers("Content-Type: application/json")
    @POST("signature_request/create_embedded")
    fun createEmbeddedSignatureRequest(@Body jsonObject: JsonObject) : Call<CreateEmbeddedSignatureRequest>

    @Headers("Content-Type: application/json")
    @POST("signature_request/create_embedded_with_template")
    fun createEmbeddedSignatureRequestWithTemplate(@Body jsonObject: JsonObject) : Call<CreateEmbeddedSignatureRequest>

    @Headers("Content-Type: application/json")
    @GET("signature_request/list")
    fun listSignatureRequests(
        @Query("account_id") account_id : String,
        @Query("page") page : Int,
        @Query("page_size") page_size : Int,
        @Query("query") query : String,
    ) : Call<SignatureRequests>


    @Headers("Content-Type: application/json")
    @GET("template/list")
    fun listTemplates(
        @Query("account_id") account_id : String,
        @Query("page") page : Int,
        @Query("page_size") page_size : Int,
        @Query("query") query : String,
    ) : Call<TemplateRequests>

    @Headers("Content-Type: application/json")
    @POST("template/delete/{template_id}")
    fun deleteTemplate(
        @Path("template_id") template_id: String
    ) : Call<Void>

    @Headers("Content-Type: application/json")
    @POST("signature_request/cancel/{signature_request_id}")
    fun cancelSignatureRequest(
        @Path("signature_request_id") signature_request_id: String
    ) : Call<Void>

    @Headers("Content-Type: application/json")
    @POST("template/create_embedded_draft")
    fun createEmbeddedTemplateDraft(@Body jsonObject: JsonObject) : Call<CreateEmbeddedDraftRequest>

    @Headers("Content-Type: application/json")
    @POST("embedded/edit_url/{template_id}")
    fun getEmbeddedTemplateEditUrl(
        @Body jsonObject: JsonObject,
        @Path("template_id") template_id: String
    ) : Call<EmbeddedResponse>

    class Embedded {

        @SerializedName("edit_url")
        var editUrl: String? = null

        @SerializedName("sign_url")
        var signUrl: String? = null

        @SerializedName("expires_at")
        var expiresAt: String? = null

    }

    class CreateEmbeddedDraftRequest {

        @SerializedName("template")
        var template: TemplateRequest? = null

    }

    class TemplateRequest {

        @SerializedName("template_id")
        var templateId: String? = null

        @SerializedName("edit_url")
        var editUrl: String? = null

        @SerializedName("expires_at")
        var expiresAt: Long? = null

    }

    class EmbeddedResponse {

        @SerializedName("embedded")
        var embedded: Embedded? = null

    }

    class AccountResponse {

        @SerializedName("account")
        var account: Account? = null

    }

    class Account {

        @SerializedName("account_id")
        var accountId: String? = null

        @SerializedName("email_address")
        var emailAddress: String? = null

    }

    class CreateEmbeddedSignatureRequest {

        @SerializedName("signature_request")
        var signatureRequest: SignatureRequest? = null

    }

    class TemplateRequests {

        @SerializedName("list_info")
        var listInfo: ListInfo? = null

        @SerializedName("templates")
        var templates: List<Templates>? = null

    }

    class SignatureRequests {

        @SerializedName("list_info")
        var listInfo: ListInfo? = null

        @SerializedName("signature_requests")
        var signatureRequests: List<SignatureRequest>? = null

    }

    class ListInfo {

        @SerializedName("page")
        var page: Int = 0

        @SerializedName("num_pages")
        var numPages: Int = 0

        @SerializedName("num_results")
        var numResults: Int = 0

        @SerializedName("page_size")
        var pageSize: Int = 0

    }

    class Templates() : Parcelable {

        @SerializedName("template_id")
        var templateId: String? = null

        @SerializedName("reusable_form_id")
        var reusableFormId: String? = null

        @SerializedName("title")
        var title: String? = null

        @SerializedName("message")
        var message: String? = null

        @SerializedName("updated_at")
        var updatedAt: Long? = null

        @SerializedName("is_creator")
        var isCreator: Boolean = false

        @SerializedName("is_embedded")
        var isEmbedded: Boolean = false

        @SerializedName("can_edit")
        var canEdit: Boolean = false

        @SerializedName("signer_roles")
        var signerRoles: List<SignerRole>? = null

        constructor(parcel: Parcel) : this() {
            templateId = parcel.readString()
            reusableFormId = parcel.readString()
            title = parcel.readString()
            message = parcel.readString()
            updatedAt = parcel.readValue(Long::class.java.classLoader) as? Long
            isCreator = parcel.readByte() != 0.toByte()
            isEmbedded = parcel.readByte() != 0.toByte()
            canEdit = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(templateId)
            parcel.writeString(reusableFormId)
            parcel.writeString(title)
            parcel.writeString(message)
            parcel.writeValue(updatedAt)
            parcel.writeByte(if (isCreator) 1 else 0)
            parcel.writeByte(if (isEmbedded) 1 else 0)
            parcel.writeByte(if (canEdit) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Templates> {
            override fun createFromParcel(parcel: Parcel): Templates {
                return Templates(parcel)
            }

            override fun newArray(size: Int): Array<Templates?> {
                return arrayOfNulls(size)
            }
        }

    }

    class SignerRole {

        @SerializedName("name")
        var role: String? = null

        var nameSigner: String? = null

        var emailSigner: String? = null

    }

    class SignatureRequest() : Parcelable {

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
        var createdAt: Long? = null

        @SerializedName("is_complete")
        var isComplete: Boolean = false

        @SerializedName("is_declined")
        var isDeclined: Boolean = false

        @SerializedName("has_error")
        var hasError: Boolean = false

        @SerializedName("signing_url")
        var signingUrl: String? = null

        @SerializedName("signing_redirect_url")
        var signingRedirectUrl: String? = null

        @SerializedName("details_url")
        var detailsUrl: String? = null

        @SerializedName("requester_email_address")
        var requesterEmailAddress: String? = null

        @SerializedName("signatures")
        var signatures: List<Signature>? = null

        @SerializedName("cc_email_addresses")
        var ccEmailAddresses: List<String>? = null

        constructor(parcel: Parcel) : this() {
            signatureRequestId = parcel.readString()
            title = parcel.readString()
            originalTitle = parcel.readString()
            subject = parcel.readString()
            message = parcel.readString()
            createdAt = parcel.readValue(Long::class.java.classLoader) as? Long
            isComplete = parcel.readByte() != 0.toByte()
            isDeclined = parcel.readByte() != 0.toByte()
            hasError = parcel.readByte() != 0.toByte()
            signingUrl = parcel.readString()
            signingRedirectUrl = parcel.readString()
            detailsUrl = parcel.readString()
            requesterEmailAddress = parcel.readString()
            ccEmailAddresses = parcel.createStringArrayList()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(signatureRequestId)
            parcel.writeString(title)
            parcel.writeString(originalTitle)
            parcel.writeString(subject)
            parcel.writeString(message)
            parcel.writeValue(createdAt)
            parcel.writeByte(if (isComplete) 1 else 0)
            parcel.writeByte(if (isDeclined) 1 else 0)
            parcel.writeByte(if (hasError) 1 else 0)
            parcel.writeString(signingUrl)
            parcel.writeString(signingRedirectUrl)
            parcel.writeString(detailsUrl)
            parcel.writeString(requesterEmailAddress)
            parcel.writeStringList(ccEmailAddresses)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SignatureRequest> {
            override fun createFromParcel(parcel: Parcel): SignatureRequest {
                return SignatureRequest(parcel)
            }

            override fun newArray(size: Int): Array<SignatureRequest?> {
                return arrayOfNulls(size)
            }
        }

    }

    class Signature {

        @SerializedName("signature_id")
        var signatureId: String? = null

        @SerializedName("signer_email_address")
        var signerEmailAddress: String? = null

        @SerializedName("signer_name")
        var signerName: String? = null

        @SerializedName("signer_role")
        var signerRole: String? = null

        @SerializedName("order")
        var order: Int = 0

        @SerializedName("status_code")
        var statusCode: String? = null

        @SerializedName("signed_at")
        var signedAt: Long? = null

    }


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

        @SerializedName("signing_url")
        var signingUrl: String? = null

        @SerializedName("signing_redirect_url")
        var signingRedirectUrl: String? = null

        @SerializedName("details_url")
        var detailsUrl: String? = null

        @SerializedName("requester_email_address")
        var requesterEmailAddress: String? = null

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
            signingUrl = parcel.readString()
            signingRedirectUrl = parcel.readString()
            detailsUrl = parcel.readString()
            requesterEmailAddress = parcel.readString()
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
            parcel.writeString(signingUrl)
            parcel.writeString(signingRedirectUrl)
            parcel.writeString(detailsUrl)
            parcel.writeString(requesterEmailAddress)
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