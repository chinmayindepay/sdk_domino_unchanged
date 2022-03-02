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

data class TransactionTrackingResponse (
        @SerializedName("transactionId") val transactionId: String? ="",
        @SerializedName("success") val success: Boolean? = null,
        @SerializedName("errorCode") val errorCode: String? ="",
        @SerializedName("errorReason") val errorReason: String? ="",
        @SerializedName("status") val status: String? ="",
        @SerializedName("merchantTxnId") val merchantTxnId: String? ="",
        @SerializedName("listPayeeStatus") val listPayeeStatus: ArrayList<PayeeStatus>? = null

)