package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class EncryptionRefreshOtpRequest (

    @SerializedName("commonRequest") val commonRequest: CommonRequest? = CommonRequest(),
    @SerializedName("refreshOtpPayload") val refreshOtpPayload: RefreshOtpPayload?,
    @SerializedName("refreshOtpApiPayloadEnc") val refreshOtpApiPayloadEnc: String? = ""

    )
