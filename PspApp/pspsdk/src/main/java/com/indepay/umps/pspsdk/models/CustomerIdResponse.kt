package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class CustomerIdResponse (
        @SerializedName("id") val id: String? = null,
        @SerializedName("firstName") val firstName: String? = null,
        @SerializedName("lastName") val lastName: String? = null,
        @SerializedName("mobileNumber") val mobileNumber: String? = null,
        @SerializedName("country") val country: String? = null,
        @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
        @SerializedName("isKyc") val isKyc: Boolean = false,
        @SerializedName("email") val email: String? = null,
        @SerializedName("customerType") val customerType: String? = null,
        @SerializedName("firebaseId") val firebaseId: String? = null,
        @SerializedName("address") val address: ArrayList<String>? = null


)
