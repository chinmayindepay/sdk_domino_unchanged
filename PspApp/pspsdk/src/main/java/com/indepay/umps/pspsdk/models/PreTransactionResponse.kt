package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class PreTransactionResponse (
        @SerializedName("feeTaxRefId") val feeTaxRefId: String? = ""

) : CommonResponse()

