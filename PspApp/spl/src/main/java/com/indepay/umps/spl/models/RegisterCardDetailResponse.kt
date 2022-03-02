package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class RegisterCardDetailResponse (

        @SerializedName("commonResponse") val commonResponse: CommonResponse? = null,
        @SerializedName("registerCardDetailResponsePayloadEnc") val registerCardDetailResponsePayloadEnc: String? = ""

)