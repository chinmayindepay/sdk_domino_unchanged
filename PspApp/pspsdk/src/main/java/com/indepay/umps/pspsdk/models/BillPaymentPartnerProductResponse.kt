package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

class BillPaymentPartnerProductResponse (

    @SerializedName("responseCode") val responseCode: String?,
    @SerializedName("success") val success: Boolean = false,
  //  @SerializedName("message") val message: String,
    @SerializedName("data") val data: ArrayList<PartnerProductData> = ArrayList()

)