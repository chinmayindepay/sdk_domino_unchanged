package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class WebApiModelResponseSub (

        @SerializedName("seamless_url") val seamless_url: String? = null,
        @SerializedName("expired_date") val expired_date: String? = null
)