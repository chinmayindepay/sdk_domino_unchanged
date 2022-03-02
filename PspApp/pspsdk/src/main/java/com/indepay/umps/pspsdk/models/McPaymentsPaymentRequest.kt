package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class McPaymentsPaymentRequest (
        @SerializedName("register_id") val register_id: String? = null,
        @SerializedName("callback_url") val callback_url: String? = null,
        @SerializedName("return_url") val return_url: String? = null,
        @SerializedName("token") val token: String? = null,
        @SerializedName("amount") val amount: String? = null,
        @SerializedName("description") val description: String? = null

)
