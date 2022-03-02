package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class MerchantCollectRequest (
        @SerializedName("merchantId") val merchantId: String,
        @SerializedName("subMerchantName") val subMerchantName: String,
        @SerializedName("accessToken") val accessToken: String,
        @SerializedName("type") val type: String,
        //@SerializedName("initiatorPA") val initiatorPA: String,
        @SerializedName("payer") val payer: Payer,
        @SerializedName("payees") val payees: ArrayList<Payee>,
        @SerializedName("remarks") val remarks: String? = null,
        @SerializedName("refId") val refId: String? = null,
        @SerializedName("refURL") val refURL: String? = null,
        @SerializedName("custRefId") val custRefId: String? = null,
        @SerializedName("timeTillExpireMins") val timeTillExpireMins: Long? = null
)