package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class DataEcoSystem (

    @SerializedName("param") val param: String? = null,
    @SerializedName("required") val required: Boolean = false

)