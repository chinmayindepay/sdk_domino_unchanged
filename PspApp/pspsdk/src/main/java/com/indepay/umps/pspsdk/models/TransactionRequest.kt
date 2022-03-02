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

data class TransactionRequest(
        @SerializedName("custPSPId") val custPSPId: String?,
        @SerializedName("accessToken") val accessToken: String?,
        @SerializedName("acquiringSource") val acquiringSource: AcquiringSource?,
        @SerializedName("requestedLocale") val requestedLocale: String?,
        @SerializedName("type") val type: String?,
        @SerializedName("payees") val payees: ArrayList<Payee>?,
        @SerializedName("payer") val payer: Payer?,
        //@SerializedName("initiatorPaAccountId") val initiatorPaAccountId: String?,
        @SerializedName("initiatorAccountId") val initiatorAccountId: String?,
        @SerializedName("remarks") val remarks: String? = null,
        @SerializedName("merchantTxnId") val merchantTxnId: String? = null,
        @SerializedName("feeTaxRefId") val feeTaxRefId: String? = null,
        @SerializedName("timeTillExpireMins") val timeTillExpireMins: String? = null
)