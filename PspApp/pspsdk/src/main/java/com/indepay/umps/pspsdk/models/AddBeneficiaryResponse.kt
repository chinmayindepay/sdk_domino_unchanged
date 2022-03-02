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

data class AddBeneficiaryResponse(
        @SerializedName("beneAccountNo") val beneAccountNo: String?,
        @SerializedName("beneBic") val beneBic: String?,
        @SerializedName("beneName") val beneName: String?,
        @SerializedName("benePaymentAddr") val benePaymentAddr: String?,
        @SerializedName("beneType") val beneType: String?,
        @SerializedName("beneMobile") val beneMobile: String?,
        @SerializedName("beneAppId") val beneAppId: String
) : CommonResponse()