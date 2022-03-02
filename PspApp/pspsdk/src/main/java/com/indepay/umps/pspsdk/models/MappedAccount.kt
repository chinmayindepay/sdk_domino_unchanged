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

data class MappedAccount(
        //@SerializedName("id") val id: String? = null,
        //@SerializedName("paId") val paId: String? = null,
        //@SerializedName("paAccountId") val paAccountId: String? = null,
        @SerializedName("custPSPId") val custPSPId: String? = null,
        @SerializedName("accessToken") val accessToken: String? = null,
        @SerializedName("acquiringSource") val acquiringSource: AcquiringSource? = null,
        @SerializedName("merchantId") val merchantId: String? = null,
        @SerializedName("requestedLocale") val requestedLocale: String? = null,
        @SerializedName("id") val id: String? = null,
        @SerializedName("accountToken") val accountToken: String? = null,
        @SerializedName("accountTokenId") val accountTokenId: Long? = null,
        @SerializedName("maskedAccountNumber") val maskedAccountNumber: String? = null,
        @SerializedName("customerPspId") val customerPspId: String? = null,
        @SerializedName("bic") val bic: String? = null,
        @SerializedName("accountType") val accountType: String? = null,
        @SerializedName("isDefault") var isDefault: Boolean = false,
        @SerializedName("mpinExists") val isMpinExists: Boolean = false,
        @SerializedName("bankBic") val bankBic: String? = null,
        @SerializedName("bankName") val bankName: String? = null,
        @SerializedName("isSelected") var isSelected: Boolean = false,
        @SerializedName("addnew") var addnew: String? = null

) : CommonResponse()