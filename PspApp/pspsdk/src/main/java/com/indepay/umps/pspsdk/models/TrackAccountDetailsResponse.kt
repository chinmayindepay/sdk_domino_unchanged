package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class TrackAccountDetailsResponse (

        @SerializedName("listOfMappedAccount") val listOfMappedAccount: ArrayList<BankAccount>? = null
) : CommonResponse()
