package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class WebApiModelRequest(

        @SerializedName("register_id") val register_id: String,
        @SerializedName("callback_url") val callback_url: String,
        @SerializedName("return_url") val return_url: String,
        @SerializedName("is_transaction") val is_transaction: Boolean = false,
        @SerializedName("transaction") val transaction: TransactionWebApiRequest
)
