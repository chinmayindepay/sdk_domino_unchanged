package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class MerchantCollectResponse (
        @SerializedName("transactionId") val transactionId: String? = null,
        @SerializedName("success") val success: Boolean = false,
        @SerializedName("errorCode") val errorCode: String? = null,
        @SerializedName("errorReason") val errorReason: String? = null,
        @SerializedName("status") val status: String? = null,
        @SerializedName("listPayeeStatus") val listPayeeStatus: String? = null,
        @SerializedName( "merchantTxnId") val merchantTxnId: String? = null
)