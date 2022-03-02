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

import android.widget.Toast
import android.app.Activity
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.telephony.SmsManager


class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, arg1: Intent) {
        when (resultCode) {
            Activity.RESULT_OK -> Toast.makeText(context, "SMS Sent", Toast.LENGTH_SHORT).show()
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT)
                    .show()
            SmsManager.RESULT_ERROR_NO_SERVICE -> Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                    .show()
            SmsManager.RESULT_ERROR_NULL_PDU -> Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show()
            SmsManager.RESULT_ERROR_RADIO_OFF -> Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show()
        }
    }
}