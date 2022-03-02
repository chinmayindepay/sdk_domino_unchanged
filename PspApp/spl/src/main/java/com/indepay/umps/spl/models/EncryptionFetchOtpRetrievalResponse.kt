package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class EncryptionFetchOtpRetrievalResponse  (
        @SerializedName("commonResponse") val commonResponse: CommonResponse? = CommonResponse(),
        @SerializedName("OtpFetchRetrievalPayload") val OtpFetchRetrievalPayload: OtpFetchRetrievalPayload? ,
        @SerializedName("fetchOtpCodeResponsePayloadEnc") val fetchOtpCodeResponsePayloadEnc: String? = ""
)