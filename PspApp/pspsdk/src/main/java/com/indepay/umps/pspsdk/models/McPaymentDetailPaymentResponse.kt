package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class McPaymentDetailPaymentResponse (
        @SerializedName("id") val id: String? = null,
        @SerializedName("register_id") val register_id: String? = null,
        @SerializedName("amount") val amount: String? = null,
        @SerializedName("description") val description: String? = null,
        @SerializedName("status") val status: String? = null

)