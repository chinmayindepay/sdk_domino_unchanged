package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class TrackDeleteAccountRequest (

        @SerializedName("custPSPId") val custPSPId: String? =null,
        @SerializedName("accessToken") val accessToken: String? =null,
        @SerializedName("transactionId") val transactionId: String? =null,
        @SerializedName("acquiringSource") val acquiringSource: AcquiringSource? = null,
        @SerializedName("merchantId") val merchantId: String? =null,
        @SerializedName("requestedLocale") val requestedLocale: String? = null
)