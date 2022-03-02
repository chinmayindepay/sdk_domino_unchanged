package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

 data class ConfirmAccountDetailsPayload (
      //  @SerializedName("startDateTime") val startDateTime: Long,
        @SerializedName("deviceInfo") val deviceInfo: DeviceInfo? = null,
        @SerializedName("accepted") val accepted: Boolean? = true,
        @SerializedName("bic") val bic: String? = null,
        @SerializedName("authorizePINCred") val authorizePINCred: AuthorizePINCred? = AuthorizePINCred()
)
