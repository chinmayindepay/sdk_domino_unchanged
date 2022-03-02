package com.indepay.umps.pspsdk.models

/*
* ******************************************************************************
*  * Copyright, INDEPAY 2019 All rights reserved.
*  *
*  * The copyright in this work is vested in INDEPAY and the
*  * information contained herein is confidential.  This
*  * work (either in whole or in part) must not be modified,
*  * reproduced, disclosed or disseminated to others or used
*  * for purposes other than that for which it is supplied,
*  * without the prior written permission of INDEPAY.  If this
*  * work or any part hereof is furnished to a third party by
*  * virtue of a contract with that party, use of this work by
*  * such party shall be governed by the express contractual
*  * terms between the INDEPAY which is a party to that contract
*  * and the said party.
*  *
*  * Revision History
*  * Date           Who        Description
*  * 09-12-2020     Sudhir   Added file header
*  *
*  *****************************************************************************
*/
import com.google.gson.annotations.SerializedName

data class AccountDetailsRequestNewapi(

            @SerializedName("custPSPId") val custPSPId: String? =null,
            @SerializedName("accessToken") val accessToken: String? =null,
            @SerializedName("transactionId") val transactionId: String? =null,
            @SerializedName("acquiringSource") val acquiringSource: AcquiringSource? = null,
            @SerializedName("merchantId") val merchantId: String? =null,
            @SerializedName("bic") val bic: String? = null,
            @SerializedName("cardLast6Digits") val cardLast6Digits: String? = null,
            @SerializedName("requestedLocale") val requestedLocale: String? = null,
            @SerializedName("email") val email: String? = null
            //@SerializedName("paId") val paId:String? = null
    )
