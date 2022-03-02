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


data class CustomerPaModel(
        @SerializedName("id") var id: Long? = null,
        @SerializedName("paId") var paId: Long? = null,
        @SerializedName("paAccountId") var paAccountId: Long? = null,
        @SerializedName("accountNumber") var accountNumber: String? = null,
        @SerializedName("bic") var bic: String? = null,
        @SerializedName("accountType") var accountType: String? = null,
        @SerializedName("isDefault") var isDefault: Boolean? = null,
        @SerializedName("isMpinExists") var isMpinExists: Boolean? = null,
        @SerializedName("customerPspId") var customerPspId: String? = null,
        @SerializedName("pa") var pa: String? =null,
        @SerializedName("custId") val custId: String? = "",
        @SerializedName("custPSPId") val custPSPId: String? ="",
        @SerializedName("accessToken") val accessToken: String? ="",
        @SerializedName("transactionId") val transactionId: String? ="",
        @SerializedName("acquiringSource") val acquiringSource: AcquiringSource? = AcquiringSource()
)