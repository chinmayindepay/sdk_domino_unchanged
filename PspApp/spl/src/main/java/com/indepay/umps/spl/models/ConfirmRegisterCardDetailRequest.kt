package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

data class ConfirmRegisterCardDetailRequest (
                @SerializedName("commonRequest") val commonRequest: CommonRequest? = CommonRequest(),
                @SerializedName("confirmAccountDetailsPayload") val confirmAccountDetailsPayload: ConfirmAccountDetailsPayload? = null,
                @SerializedName("confirmAccountRegPayloadEnc") val confirmAccountRegPayloadEnc: String? = ""
)
