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
 *  * 12-12-2019     Mayank D   Fixed issue- RR-99
 *  *****************************************************************************
 */

package com.indepay.umps.pspsdk.transaction.payment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_txn_payment.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.startActivityForResult

class TxnPaymentActivity : SdkBaseActivity() {

    private val REQ_CODE_TRANSACTION = 1005
    private lateinit var txnId: String

    companion object : SdkCommonMembers() {
        const val TXN_TYPE_PAY = "PAY"
        //const val TXN_TYPE_COLLECT = "COLLECT"
        const val TRANSACTION_TYPE = "transaction_type"
        //const val PAYMENT_ADDRESS = "payment_address"
        const val MERCHANT_TXN_ID = "merchant_txn_id"
        const val BENE_ID = "bene_id"
        const val TARGET_SELF_ACCOUNT_ID = "target_self_account_id"
        const val INITIATOR_ACCOUNT_ID = "intitiator_account_id"
        const val PAYEE_NAME = "payee_name"
        const val ACCOUNT_NO = "account_no"
        const val MOBILE_NO = "mobile_no"
        const val APP_ID = "APP_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_txn_payment)

        var remarks = edt_remarks.text.toString()
        if (remarks.isNullOrBlank()) {
            remarks = "-"
        }



        button_proceed.setOnClickListener {

            if (intent.getStringExtra(TRANSACTION_TYPE).equals(TXN_TYPE_PAY, ignoreCase = true)) {
            startActivityForResult<PaymentAccountActivity>(REQ_CODE_TRANSACTION,
                    PaymentAccountActivity.APP_ID to getAppName(this),
                    PaymentAccountActivity.MOBILE_NO to intent.getStringExtra(MOBILE_NO),
                    PaymentAccountActivity.TRANSACTION_TYPE to intent.getStringExtra(TRANSACTION_TYPE),
                    PaymentAccountActivity.PAYEE_NAME to intent.getStringExtra(PAYEE_NAME),
                    PaymentAccountActivity.BENE_ID to intent.getStringExtra(BENE_ID),
                    PaymentAccountActivity.AMOUNT to edt_txn_amount.text.toString(),
                    PaymentAccountActivity.NOTE to remarks,
                    PaymentAccountActivity.COMING_FROM to 1

                            ,
                    PaymentAccountActivity.EMAIL to "email",
                    PaymentAccountActivity.ORDER_ID to "987897",
                    PaymentAccountActivity.REMARKS to "remarks",PaymentAccountActivity.MERCHANT_NAME to "merchName"            )

        }
            else
            {
                initiateCollectRequest()
            }
        }

    }

    private fun initiateCollectRequest() {
        Log.e("TxnTypeBefore", intent.getStringExtra(TRANSACTION_TYPE).toString())



        var remarks = edt_remarks.text.toString()
        if (remarks.isNullOrBlank()) {
            remarks = "-"
        }

        var payees: ArrayList<Payee>? = null
        var payer: Payer? = null
        payer = Payer(
                beneId = intent.getStringExtra(BENE_ID),
                //pa = intent.getStringExtra(PAYMENT_ADDRESS)
                //mobile = mobile, //intent.getStringExtra(MOBILE_NO),
                mobile = intent.getStringExtra(MOBILE_NO),
                appId = intent.getStringExtra(APP_ID)!!

        )
        val payee = Payee(
                amount = edt_txn_amount.text.toString()
                //amount = amount

        )
        payees = ArrayList()
        payees.add(payee)

    callApi(
    accessToken = retrieveAccessToken(this),
    custPSPId = getPspId(this),
    appName = getAppName(this),
    apiToCall = { sdkApiService ->
        sdkApiService.initTransactionAsync(
                TransactionRequest(
                        custPSPId = getPspId(this),
                        requestedLocale = getCurrentLocale(this),
                        acquiringSource = AcquiringSource(
                                appName = getAppName(this)
                        ),
                        accessToken = getAccessToken(this),
                        merchantTxnId = intent.getStringExtra(MERCHANT_TXN_ID),
                        type = intent.getStringExtra(TRANSACTION_TYPE), //intent.getStringExtra(TRANSACTION_TYPE),
                        remarks = remarks,
                        payees = payees,
                        payer = payer,
                        //initiatorPaAccountId = intent.getStringExtra(INITIATOR_PA_ACCOUNT_ID)
                        //initiatorAccountId = intent.getLongExtra(PaymentAccountActivity.INITIATOR_ACCOUNT_ID, 0).toString()
                        //initiatorAccountId = selectedAccount?.accountTokenId.toString()
                        initiatorAccountId = intent.getLongExtra(INITIATOR_ACCOUNT_ID, 0).toString()
                )
        )
    },
    successCallback = { commonResponse ->
        if (commonResponse is TransactionResponse) {
            txnId = commonResponse.transactionId.orEmpty()
            //if (intent.getStringExtra(TRANSACTION_TYPE).equals(TXN_TYPE_PAY, ignoreCase = true)) {
            if (intent.getStringExtra(TRANSACTION_TYPE).equals(TXN_TYPE_PAY, ignoreCase = true)) {
                //callSplForTransaction(commonResponse.bic.orEmpty())
                //callAccountSelection(commonResponse.bic.orEmpty())
                finish()
            } else {
                alert(resources.getString(R.string.request_sent), resources.getString(R.string.success)) {
                    okButton { finish() }
                    isCancelable = true
                }.show()
            }

        }
    },
    errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
    )
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQ_CODE_TRANSACTION && resultCode == Activity.RESULT_OK -> {
               //finish()
            }

            else -> alert(resources.getString(R.string.something_wrong), resources.getString(R.string.request_failed)) {
                okButton { finish() }
                isCancelable = false
            }.show()

        }

    }
}
