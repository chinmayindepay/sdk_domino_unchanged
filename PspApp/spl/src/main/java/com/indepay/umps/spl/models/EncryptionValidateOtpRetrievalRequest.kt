package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName
import com.indepay.umps.spl.models.CommonRequest
import com.indepay.umps.spl.models.ValidateOtpRetrievalPayload

data class EncryptionValidateOtpRetrievalRequest (
        @SerializedName("commonRequest") val commonRequest: CommonRequest? = CommonRequest(),
        @SerializedName("validateOtpRetrievalPayload") val validateOtpRetrievalPayload: ValidateOtpRetrievalPayload?,
        @SerializedName("validateOtpApiPayloadEnc") val validateOtpApiPayloadEnc: String? = ""
)