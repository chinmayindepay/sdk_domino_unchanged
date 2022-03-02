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

package com.indepay.umps.pspsdk.utils

import android.support.annotation.Keep

@Keep
open class SdkCommonMembers {
    val MESSENGER_INTENT_KEY = "messenger_intent_key"
    val INIT_NOTIFICATION_CALL = "init_notification_call"
    val ERROR_CODE = "error_code"
    val ERROR_REASON = "error_reason"
    val STATUS = "status"
    @Keep val USER_TOKEN = "user_token"
    @Keep val COMING_FROM = "coming_from"
    val RESULT = "result"

    val CLIENT_ID = "client_id"
    val SDK_EXTRA_1 = "sdk_extra_1"
    val SDK_EXTRA_2 = "sdk_extra_2"
    val SDK_EXTRA_3 = "sdk_extra_3"
    val SDK_EXTRA_4 = "sdk_extra_4"
    val SDK_EXTRA_5 = "sdk_extra_5"
}