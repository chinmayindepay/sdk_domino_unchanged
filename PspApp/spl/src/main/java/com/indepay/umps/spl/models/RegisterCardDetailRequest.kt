package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class RegisterCardDetailRequest (
        @SerializedName("commonRequest") val commonRequest: CommonRequest? = CommonRequest(),
        @SerializedName("registerAccountDetailsPayload") val registerAccountDetailsPayload: RegisterAccountDetailsPayload? = null,
        @SerializedName("registerCardDetailPayloadEnc") val registerCardDetailPayloadEnc: String? = ""
)