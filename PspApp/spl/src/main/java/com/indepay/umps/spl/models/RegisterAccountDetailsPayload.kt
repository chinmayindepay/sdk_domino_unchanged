package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName
import java.util.*

 data class RegisterAccountDetailsPayload(
        @SerializedName("startDateTime") val startDateTime: Long ,
        @SerializedName("deviceInfo") val deviceInfo: DeviceInfo? = null,
        @SerializedName("accountInfo") val accountInfo: AccountInfo? = null,
        @SerializedName("card") val card: RegisterAccountDetails? = null,
        @SerializedName("bic") val bic: String? = null
)

