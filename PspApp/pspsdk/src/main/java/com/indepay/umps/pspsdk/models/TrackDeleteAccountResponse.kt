package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class TrackDeleteAccountResponse (
        @SerializedName("deleteResponse") val deleteResponse: ArrayList<DeleteResponse>,
        @SerializedName("pending") val pending: Boolean? = false

) : CommonResponse()
