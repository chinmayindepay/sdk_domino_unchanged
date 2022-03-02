package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class DeleteAccountResponse (
        @SerializedName("transactionIds")  val transactionIds: String? = null

//        @SerializedName("transactionId")  val transactionId: String? = null,
//        @SerializedName("success") val success: Boolean = false,
//        @SerializedName("errorCode") val errorCode: String? = null,
//        @SerializedName("errorReason") val errorReason: String? = null,
//        @SerializedName("status") val status: String? = null

):CommonResponse()