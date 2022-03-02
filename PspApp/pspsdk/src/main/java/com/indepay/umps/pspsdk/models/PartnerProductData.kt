package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PartnerProductData  (

        @SerializedName("name") val name: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("code") val code: String?,
        @SerializedName("logo") val logo: String?,
        @SerializedName("amount") val amount: String?,
        @SerializedName("adminFee") val adminFee: String?,
        @SerializedName("biller") val biller: String?,
        @SerializedName("category") val category: String?,
        @SerializedName("active") val active: Boolean = false,
        @SerializedName("type") val type: String?


):Serializable