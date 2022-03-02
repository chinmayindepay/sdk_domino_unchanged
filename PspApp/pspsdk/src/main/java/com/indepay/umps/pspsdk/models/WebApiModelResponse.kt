package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class WebApiModelResponse (

        @SerializedName("error") val error: Boolean = false,
        @SerializedName("data") val data: WebApiModelResponseSub? = null


)
