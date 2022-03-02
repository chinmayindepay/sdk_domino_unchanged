package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

class BillCheckResponseData (

        @SerializedName("inquiryId") val inquiryId: String?,
        @SerializedName("accountNumber") val accountNumber: String?,
        @SerializedName("customerName") val customerName: String?,
        @SerializedName("productName") val productName: String?,
        @SerializedName("productCode") val productCode: String?,
        @SerializedName("category") val category: String?,
        @SerializedName("amount") val amount: Long?,
        @SerializedName("totalAdmin") val totalAdmin: Long?,
        @SerializedName("processingFee") val processingFee: Long?,
        @SerializedName("denom") val denom: String?,
        @SerializedName("validity") val validity: String?,
        @SerializedName("customerDetail") val customerDetail: ArrayList<String> = ArrayList(),
        @SerializedName("billDetails") val billDetails: ArrayList<String> = ArrayList(),
        @SerializedName("productDetails") val productDetails: ArrayList<String> = ArrayList(),
        @SerializedName("extraFields") val extraFields: ArrayList<String> = ArrayList()

)
