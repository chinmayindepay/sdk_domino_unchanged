package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName
import com.indepay.umps.spl.models.CommonRequest
import com.indepay.umps.spl.models.OtpFetchRetrievalPayload

data class EncryotionOtpFetchRetrievalRequest  (
        @SerializedName("commonRequest") val commonRequest: CommonRequest? = CommonRequest(),
        @SerializedName("OtpFetchRetrievalPayload") val OtpFetchRetrievalPayload: OtpFetchRetrievalPayload?,
        @SerializedName("fetchOtpCodePayloadEnc") val fetchOtpCodePayloadEnc: String? = ""
)