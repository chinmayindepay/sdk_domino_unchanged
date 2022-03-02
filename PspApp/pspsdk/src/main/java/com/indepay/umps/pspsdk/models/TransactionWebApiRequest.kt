package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class TransactionWebApiRequest(

        @SerializedName("amount") val amount: String,
        @SerializedName("description") val description: String
)