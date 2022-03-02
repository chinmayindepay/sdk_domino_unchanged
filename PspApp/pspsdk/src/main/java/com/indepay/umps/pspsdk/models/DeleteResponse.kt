package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class DeleteResponse (
        @SerializedName("accountTokenId") val accountTokenId: String? = null,
        @SerializedName("msg") val msg: String? =null
)