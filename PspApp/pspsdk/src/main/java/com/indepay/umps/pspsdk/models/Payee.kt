package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class Payee (
        @SerializedName("amount") val amount: String,
        @SerializedName("minAmount") val minAmount: String? = null,
        @SerializedName("beneId") val beneId: String? = null,
        @SerializedName("mobileNo") val mobile: String? = null,
        @SerializedName("appId") val appId: String? = null,
        @SerializedName("targetSelfAccountId") val targetSelfAccountId: String? = null
        //@SerializedName("pa") val pa: String? = null,
        //@SerializedName("targetSelfPaAccountId") val targetSelfPaAccountId: String? = null
)