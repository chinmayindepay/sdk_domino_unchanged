package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName


data class AppDetailsResponse(

    @SerializedName("appDetails")val appDetailsList: ArrayList<AppDetail>
): CommonResponse()


data class AppDetail(
    val active: Boolean=false,
    val appId: String?=null,
    val logo: String?=null,
    val name: String?=null
)



data class BeneficiaryListResponse(
    val beneDetails: ArrayList<BeneDetail>?=null
):CommonResponse()

data class BeneDetail(
    val beneAccountNo: Any,
    val beneAppName: String,
    val beneBic: Any,
    val beneId: Int,
    val beneMobile: String,
    val beneName: String,
    val beneType: String,
    val crtnTs: Long,
    val custPSPId: String
)


