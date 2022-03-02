package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class RefreshOtpPayload (
        @SerializedName("action") val action: String?,
        @SerializedName("referenceId") val referenceId: String?,
        @SerializedName("bic") val bic: String? ,
        @SerializedName("deviceInfo") val deviceInfo: DeviceInfo
)
