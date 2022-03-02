package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class AddNewBeneRequest(
        @SerializedName("custPSPId") val custPSPId: String?,
        @SerializedName("accessToken") val accessToken: String?,
        @SerializedName("acquiringSource") val acquiringSource: AcquiringSource?,
        @SerializedName("requestedLocale") val requestedLocale: String?,
        @SerializedName("beneAccountNo") val beneAccountNo: String? = null,
        @SerializedName("beneBic") val beneBic: String? = null,
        @SerializedName("beneName") val beneName: String? = null,
        @SerializedName("beneMobile") val beneMobile: String? = null,
        @SerializedName("beneAppName") val beneAppName: String? = null,
        @SerializedName("beneType") val beneType: String?
)

