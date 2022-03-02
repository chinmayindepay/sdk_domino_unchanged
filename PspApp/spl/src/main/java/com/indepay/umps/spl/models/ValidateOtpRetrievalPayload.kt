package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class ValidateOtpRetrievalPayload (
        @SerializedName("bic") val bic: String?,
        @SerializedName("referenceId") val referenceId: String?,
        @SerializedName("otp") val otp: String?,
        @SerializedName("action") val action: String?,
        @SerializedName("deviceInfo") val deviceInfo: DeviceInfo
)