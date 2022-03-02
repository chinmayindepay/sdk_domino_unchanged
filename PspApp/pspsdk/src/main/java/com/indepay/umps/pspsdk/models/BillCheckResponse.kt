package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

class BillCheckResponse (

        @SerializedName("responseCode") val responseCode: String?,
        @SerializedName("success") val success: Boolean = false,
        //  @SerializedName("message") val message: String,
        @SerializedName("data") val data: BillCheckResponseData

)

//{
//    "responseCode": 300,
//    "success": true,
//    "message": {
//    "ID": "Inkuiri berhasil",
//    "EN": "Inquiry is successful"
//},
