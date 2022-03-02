package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class FetchOtpRetrievalResponsePayload  (
        @SerializedName("sessionKey") val sessionKey: String? = "",
        @SerializedName("bankKi") val bankKi: String? = "",
        @SerializedName("publicKey") val publicKey : String? = "",
        @SerializedName("referenceId") val referenceId: String? = "",
        @SerializedName("otpExpiry") val otpExpiry: String? = "",
        @SerializedName("otpChallengeCode") val otpChallengeCode: String? = "",
        @SerializedName("action") val action: String? = "",
        @SerializedName("otpPhoneNumber") val otpPhoneNumber: String? = ""

        )