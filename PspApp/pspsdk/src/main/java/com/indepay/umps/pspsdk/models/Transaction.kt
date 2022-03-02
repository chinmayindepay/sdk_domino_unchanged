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

data class Transaction(
        @SerializedName("transactionId") val transactionId: String? = null,
        @SerializedName("success") val success: Boolean = false,
        @SerializedName("errorCode") val errorCode: String? = null,
        @SerializedName("errorReason") val errorReason: String? = null,
        @SerializedName("status") val status: String? = null,
        @SerializedName("txnType") val txnType: String? = null,
        @SerializedName("selfInitiated") val selfInitiated: Boolean = false,
        @SerializedName("waitingForApproval") val waitingForApproval: Boolean = false,
        @SerializedName("selfAccountNumber") val selfAccountNumber: String? = null,
        @SerializedName("selfBIC") val selfBIC: String? = null,
        @SerializedName("counterpartMerchantId") val counterpartMerchantId: String? = null,
        @SerializedName("counterpartMobile") val counterpartMobile: String? = null,
        @SerializedName("counterpartName") val counterpartName: String? = null,
        @SerializedName("counterpartAppName") val counterpartAppName: String? = null,
        @SerializedName("counterpartAccountNumber") val counterpartAccountNumber: String? = null,
        @SerializedName("counterpartBIC") val counterpartBIC: String? = null,
        @SerializedName("counterpartContactMobile") val counterpartContactMobile: String? = null,
        @SerializedName("counterpartSelf") val counterpartSelf: Boolean = false,
        @SerializedName("amount") val amount: String? = null,
        @SerializedName("minimumAmount") val minimumAmount: String? = null,
        @SerializedName("remarks") val remarks: String? = null,
        @SerializedName("expiringAt") val expiringAt: Long? = null,
        @SerializedName("timestamp") val timestamp: Long? = null,
        @SerializedName("header") var header: Boolean = false,
        @SerializedName("name") var name: String? = null


)