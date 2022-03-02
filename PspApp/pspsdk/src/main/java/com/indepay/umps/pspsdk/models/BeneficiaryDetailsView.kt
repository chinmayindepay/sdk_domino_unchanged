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
 *  * 06-09-2019     Mayank D   Added file header
 *  *
 *  *****************************************************************************
 */

package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class BeneficiaryDetailsView(
        @SerializedName("beneId") var beneId:Long=0,
        @SerializedName("beneAccountNo") var beneAccountNo: String? = null,
        @SerializedName("beneBic") var beneBic: String? = null,
        @SerializedName("beneName") var beneName: String? = null,
        @SerializedName("benePaymentAddr") var benePaymentAddr: String? = null,
        @SerializedName("beneType") var beneType: String? = null,
        @SerializedName("crtnTs") var crtnTs: String? = null,
        @SerializedName("benePspId") var benePspId: String? = null,
        //UserToken  data
        @SerializedName("custId") var custId: String? = null,
        @SerializedName("custPSPId") var custPSPId: String? =null,
        @SerializedName("accessToken") var accessToken: String? =null,
        @SerializedName("transactionId") var transactionId: String? =null,
        @SerializedName("acquiringSource") var acquiringSource: AcquiringSource? = AcquiringSource()
)