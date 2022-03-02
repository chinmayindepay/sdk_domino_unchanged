package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class CreditCardId (
        @SerializedName("mcPaymentCardId") val mcPaymentCardId: String? = null,
        @SerializedName("customerId") val customerId: String? = null
)