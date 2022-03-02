package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

 data class RegisterAccountDetails (
         @SerializedName("number") val number: String? = "",
        @SerializedName("expiryMonth") val expiryMonth: String? = "",
        @SerializedName("expiryYear") val expiryYear: String? = "",
        @SerializedName("cvv") val cvv: String? = "",
        @SerializedName("fullName") val fullName: String? = ""
)