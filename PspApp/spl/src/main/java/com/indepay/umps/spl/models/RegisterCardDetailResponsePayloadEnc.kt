package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class RegisterCardDetailResponsePayloadEnc (
        @SerializedName("sessionKey") val sessionKey: String? = "",
        @SerializedName("registeredName") val registeredName: String? = ""

        )