package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName

 data class AccountInfo (
        @SerializedName("bic") val bic: String? = null,
        @SerializedName("accountNo") val accountNo: String? = ""
)