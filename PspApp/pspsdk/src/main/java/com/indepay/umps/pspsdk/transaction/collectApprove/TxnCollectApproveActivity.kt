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

package com.indepay.umps.pspsdk.transaction.collectApprove

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Keep
import android.view.View
import com.google.gson.Gson
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_bank_account_list.*
import kotlinx.android.synthetic.main.activity_txn_collect_approve.*
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.activity_txn_collect_approve.llProgressBar as llProgressBar1


@Keep
class TxnCollectApproveActivity : SdkBaseActivity() {

    private val REQ_CODE_APPROVE_COLLECT = 10900
    private val REQ_CODE_REGISTRATION = 10100
    private val REQ_CODE_PROFILE_FETCH = 10200
    private val REQ_CODE_ECOSYSTEM_BANK_FETCH = 10300
    private lateinit var collectDetails: Transaction
    private lateinit var collectTxnId: String
    private lateinit var amount:String
    private lateinit var order_id:String
    private lateinit var remarks:String
    private val REQUEST_CODE_MERCHANT_PAYMENT:Int = 700
    private lateinit var merchName:String

    @Keep
    companion object : SdkCommonMembers() {
        const val MRCH_TXN_ID = "mrch_txn_id"
        const val ORIG_TXN_ID = "orig_txn_id"
        const val AMOUNT = "amount"
        @Keep  const val ORDER_ID = "order_id"
        const val REMARKS = "remarks"
        const val MERCHANT_NAME = "merchName"
        @Keep
        const val WALLET_BAL = "wallet_bal"
        internal const val COLLECT_DETAILS = "collect_details"
    }

    /**
     * This method is called by Base App, to generate the transaction list with their status.
     *              and to Respond and Postpone to the Pending Collect request.
     * @param EcosystemBankRequest
     * @return EcosystemBankResponse
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_txn_collect_approve)
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        callDialog=true
        if(intent.getStringExtra(TxnCollectApproveActivity.COLLECT_DETAILS).isNullOrBlank()) {
            amount = intent.getStringExtra(TxnCollectApproveActivity.AMOUNT).toString()
            order_id = intent.getStringExtra(PaymentAccountActivity.ORDER_ID).toString()
            remarks = intent.getStringExtra(TxnCollectApproveActivity.REMARKS).toString()
            merchName = intent.getStringExtra(TxnCollectApproveActivity.MERCHANT_NAME).toString()
        }else{
            val txnDetails = Gson().fromJson(intent.getStringExtra(TxnCollectApproveActivity.COLLECT_DETAILS), Transaction::class.java)
            amount = txnDetails.amount.toString()
            remarks = txnDetails.remarks.toString()
            merchName = txnDetails.counterpartName.toString()
        }
        if(intent.getStringExtra(RegistrationActivity._LOCALE).isNullOrBlank()){
            getCurrentLocale(this)
        }else {
            saveLocale(this, intent.getStringExtra(RegistrationActivity._LOCALE).toString())
        }
        if(intent.getStringExtra(RegistrationActivity.APP_ID).isNullOrBlank() && getAppName(this).isNullOrBlank()){
            alert("App Name cannot be empty", "App Name missing") {
                okButton {
                    finish()
                }
            }.show()
        }

        if(intent.getStringExtra(RegistrationActivity.USER_NAME).isNullOrBlank() && getUserName(this).isNullOrBlank()){
            alert("User Name cannot be empty", "User Name missing") {
                okButton {
                    finish()
                }
            }.show()
        }

        if(intent.getStringExtra(RegistrationActivity.USER_MOBILE).isNullOrBlank() && getMobileNo(this).isNullOrBlank()){
            alert("Mobile Number cannot be empty", "Mobile Number missing") {
                okButton {
                    finish()
                }
            }.show()
        }
        if(!intent.getStringExtra(RegistrationActivity.APP_ID).isNullOrBlank() && !intent.getStringExtra(RegistrationActivity.USER_NAME).isNullOrBlank()
                && !intent.getStringExtra(RegistrationActivity.USER_MOBILE).isNullOrBlank()) {
            saveAppName(this, intent.getStringExtra(RegistrationActivity.APP_ID).toString())
            saveUserName(this, intent.getStringExtra(RegistrationActivity.USER_NAME).toString())
            saveMobileNo(this, intent.getStringExtra(RegistrationActivity.USER_MOBILE).toString())
        }
        if (!getBooleanData(this, IS_REGISTERED)) {
            if(getUserToken(this).isNullOrBlank()) {
                fetchAppToken { token ->
                    hideDialog()
                        showRegistrationDialog(token)

                }
            }else{
                hideDialog()
                showRegistrationDialog(getUserToken(this))
            }
        }else {
            // showDialog()
            fetchAppToken { token ->
                saveUserToken(this,token)
                callAccountSelection("", "")
//                val payees = ArrayList<Payee>()
//                if (remarks.isNullOrBlank()) {
//                    remarks = resources.getString(R.string.paid_by)+" ${getUserName(this)}"
//                }
//                payees.add(Payee(
//                        amount = amount
//                ))
//                callMerchantApi(
//                        merchantId = BuildConfig.MERCHANT_ID,
//
//                        apiToCall = { sdkApiService ->
//                            sdkApiService.initiateMerchantTransactionAsync(
//                                    MerchantCollectRequest(
//                                            merchantId = BuildConfig.MERCHANT_ID,
//                                            accessToken = getStringData(this, MRCH_TXN_ID),
//                                            type = "COLLECT",
//                                            //initiatorPA = initiatorPa,
//                                            payer = Payer(
//                                                    //pa = getStringData(this, com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER) + PA_HANDLE
//                                                    mobile = getMobileNo(this),
//                                                    appId = BuildConfig.APP_NAME
//                                            ),
//                                            payees = payees,
//                                            remarks = remarks,
//                                            subMerchantName = merchName
//
//                                    ))
//                        },
//                        successCallback = { merchantCollectResponse ->
//                            saveStringData(this@TxnCollectApproveActivity, TxnCollectApproveActivity.MRCH_TXN_ID, merchantCollectResponse.transactionId);
//                            when {
//                                !(getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID).isNullOrBlank()) -> {
//                                    fetchTxnDetails(getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID))
//                                }
//                                intent.hasExtra(ORIG_TXN_ID) -> {
//                                    collect_approval_root.visibility = View.VISIBLE
//                                    fetchTxnDetails(intent.getStringExtra(ORIG_TXN_ID))
//                                }
//                                else -> {
//                                    hideDialog()
//                                    collectDetails = Gson().fromJson(intent.getStringExtra(COLLECT_DETAILS), Transaction::class.java)
//                                    collect_approval_root.visibility = View.VISIBLE
//                                    initializeView()
//                                    //callApproveCollectInitApi()
//                                }
//                            }
//                        },
//                        errorCallback = { it ->
//                            hideDialog()
//                            val errCode = it.errorCode
//                            val errReason = it.errorReason
//
//                            errReason?.let { it1 ->
//                                alert(it1, errCode) {
//                                    okButton {}
//                                }.show()
//                            }
//                        }
//                )
            }
        }

        btn_approve.setOnClickListener {
//            showDialog()
            llProgressBar.visibility = View.VISIBLE

            callApproveCollectInitApi()
        }

        btn_decline.setOnClickListener {
//            showDialog()
            llProgressBar.visibility = View.VISIBLE

            callRejectCollectApi()
        }
    }


    private fun initializeView() {
        txt_requested_by.text = collectDetails.counterpartName
        //txt_payment_address.text = collectDetails.counterpartPA
        txt_amount.text = collectDetails.amount
        if (collectDetails.minimumAmount.isNullOrBlank()) {
            lbl_min_amount.visibility = View.GONE
            txt_min_amount.visibility = View.GONE
        } else {
            lbl_min_amount.visibility = View.VISIBLE
            txt_min_amount.visibility = View.VISIBLE
            txt_min_amount.text = collectDetails.minimumAmount
        }
        txt_expire_at.text = getFormattedDate(this, collectDetails.expiringAt)
        collect_details_container.visibility = View.VISIBLE
    }

    /**
     * This method is called by SDK itself, to Approve the Collect request.
     * @param CollectApproveRequest
     * @return CollectApproveResponse
     */

    private fun callApproveCollectInitApi() {
        val mrchTxnId = getStringData(this, MRCH_TXN_ID)
        val txnId = if (mrchTxnId == null) {
            intent.getStringExtra(ORIG_TXN_ID) ?: collectDetails.transactionId
        } else {
            null
        }
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.collectApprovalRequestAsync(
                            CollectApproveRequest(
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    approvedAmount = collectDetails.amount.orEmpty(),
                                    approved = true,
                                    //paAccountId = getPaAccountId(this),
                                    accountId = getAccountTokenId(this),
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    transactionId = txnId,
                                    merchantTxnId = mrchTxnId,
                                    requestedLocale = getCurrentLocale(this)
                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessCollectApprove(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun onSuccessCollectApprove(response: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE

        if (response is CollectApproveResponse) {
            collectTxnId = response.transactionId.orEmpty()
            val bic = response.bic.orEmpty()
            //callSplForCollectApproval(collectTxnId, bic)
            callAccountSelection(collectTxnId, bic)

        }
    }

    /**
     * This method is called by SDK itself, to Reject the Collect request.
     * @param CollectApproveRequest
     * @return CollectApproveResponse
     */

    private fun callRejectCollectApi() {

        val mrchTxnId = intent.getStringExtra(MRCH_TXN_ID)
        val txnId = if (mrchTxnId == null) {
            intent.getStringExtra(ORIG_TXN_ID) ?: collectDetails.transactionId
        } else {
            null
        }
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.collectRejectionRequestAsync(
                            CollectRejectionRequest(
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    approved = false,
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    transactionId = txnId,
                                    merchantTxnId = mrchTxnId
                            )
                    )
                },
                successCallback = {
//                    hideDialog()
                    llProgressBar.visibility = View.GONE

                    alert(resources.getString(R.string.collect_request_rejected), resources.getString(R.string.request_failed)){
                        okButton { finish() }
                        isCancelable = false
                    }.show()
                },
                errorCallback = { commonResponse ->
                    hideDialog()
                    commonErrorCallback(commonResponse) }
        )
    }

    private  fun callAccountSelection(txnId: String, bic: String) {
        startActivityForResult(intentFor<PaymentAccountActivity>(
                PaymentAccountActivity.TXN_ID to txnId,
                PaymentAccountActivity.BIC to bic,
                PaymentAccountActivity.APP_ID to getAppName(this),
                PaymentAccountActivity.MOBILE_NO to getMobileNo(this),
                PaymentAccountActivity.PSP_ID to getPspId(this),
                PaymentAccountActivity.TXN_TYPE to TransactionType.FINANCIAL_TXN.name,
                PaymentAccountActivity.BANK_NAME to getBankNameFromBic(this, bic),
                PaymentAccountActivity.PAYEE_NAME to "",
                PaymentAccountActivity.ACCOUNT_NO to "",
                PaymentAccountActivity.AMOUNT to amount,
                PaymentAccountActivity.MERCHANT_NAME to merchName,
                PaymentAccountActivity.NOTE to remarks
        ).singleTop().clearTop(),
                REQ_CODE_APPROVE_COLLECT
        )
    }

    /* private fun callSplForCollectApproval(txnId: String, bic: String) {
         startActivityForResult(intentFor<PaymentAccountActivity>(
                 AuthenticateTxnActivity.APP_ID to getAppName(this),
                 AuthenticateTxnActivity.BIC to bic,
                 AuthenticateTxnActivity.TXN_ID to txnId,
                 AuthenticateTxnActivity.MOBILE_NO to getMobileNo(this),
                 AuthenticateTxnActivity.PSP_ID to getPspId(this),
                 AuthenticateTxnActivity.TXN_TYPE to TransactionType.FINANCIAL_TXN.name,
                 AuthenticateTxnActivity.BANK_NAME to getBankNameFromBic(this, bic),
                 AuthenticateTxnActivity.PAYEE_NAME to "",
                 AuthenticateTxnActivity.ACCOUNT_NO to "",
                 AuthenticateTxnActivity.AMOUNT to collectDetails.amount,
                 AuthenticateTxnActivity.NOTE to collectDetails.remarks).singleTop().clearTop(),
                 REQ_CODE_APPROVE_COLLECT
         )
     }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when {
            requestCode == REQ_CODE_REGISTRATION && resultCode == Activity.RESULT_OK -> {
                //fetchMerchantToken(amount = amount, remarks = remarks, merchantId = BuildConfig.MERCHANT_ID, reqCode = REQUEST_CODE_MERCHANT_PAYMENT, merchName = merchName)
                val payees = ArrayList<Payee>()
                if (remarks.isNullOrBlank()) {
                    remarks = resources.getString(R.string.paid_by)+" ${getUserName(this)}"
                }
                payees.add(Payee(
                        amount = amount
                ))
//                showDialog()
                llProgressBar.visibility = View.VISIBLE

                callMerchantApi(
                        merchantId = BuildConfig.MERCHANT_ID,

                        apiToCall = { sdkApiService ->
                            sdkApiService.initiateMerchantTransactionAsync(
                                    MerchantCollectRequest(
                                            merchantId = BuildConfig.MERCHANT_ID,
                                            accessToken = getStringData(this, MRCH_TXN_ID),
                                            type = "COLLECT",
                                            //initiatorPA = initiatorPa,
                                            payer = Payer(
                                                    //pa = getStringData(this, com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER) + PA_HANDLE
                                                    mobile = getMobileNo(this),
                                                    appId = BuildConfig.APP_NAME
                                            ),
                                            payees = payees,
                                            remarks = remarks,
                                            refURL = BuildConfig.REFERENCE_URL,
                                            refId =order_id ,
                                            subMerchantName = merchName

                                    ))
                        },
                        successCallback = { merchantCollectResponse ->
                            saveStringData(this@TxnCollectApproveActivity, TxnCollectApproveActivity.MRCH_TXN_ID, merchantCollectResponse.transactionId);
                            when {
                                !(getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID).isNullOrBlank()) -> {
                                    fetchTxnDetails(getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID))
                                }
                                intent.hasExtra(ORIG_TXN_ID) -> {
                                    collect_approval_root.visibility = View.VISIBLE
                                    fetchTxnDetails(intent.getStringExtra(ORIG_TXN_ID).toString())
                                }
                                else -> {
//                                    hideDialog()
                                    llProgressBar.visibility = View.GONE

                                    collectDetails = Gson().fromJson(intent.getStringExtra(COLLECT_DETAILS), Transaction::class.java)
                                    collect_approval_root.visibility = View.VISIBLE
                                    initializeView()
                                    //callApproveCollectInitApi()
                                }
                            }
                        },
                        errorCallback = { it ->
//                            hideDialog()
                            llProgressBar.visibility = View.GONE

                            val errCode = it.errorCode
                            val errReason = it.errorReason

                            errReason?.let { it1 ->
                                alert(it1, errCode) {
                                    okButton {}
                                }.show()
                            }
                        }
                )
            }
            else->{
//                hideDialog()
                llProgressBar.visibility = View.GONE

                finish()
            }
        }
    }

    private fun fetchTxnDetails(txnId: String) {
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.getTransactionHistoryDetailsAsync(
                            appName = getAppName(this),
                            accessToken = getAccessToken(this),
                            custPSPId = getPspId(this),
                            txnId = txnId,
                            requestedLocale = getCurrentLocale(this),
                            includeWaitingForApprovalOnly = true
                    )
                },
                successCallback = { commonResponse -> onSuccessTxnDetails(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun onSuccessTxnDetails(response: CommonResponse) {
        if (response is TxnHistoryResponse) {
            if (response.transactionList.isEmpty()) {
//                hideDialog()
                llProgressBar.visibility = View.GONE

                alert(resources.getString(R.string.search_again), resources.getString(R.string.trans_not_found)) {
                    okButton {
                        when {
                            !(getStringData(this@TxnCollectApproveActivity, MRCH_TXN_ID).isNullOrBlank()) -> {
                                fetchTxnDetails(getStringData(this@TxnCollectApproveActivity, MRCH_TXN_ID))
                            }
                            intent.hasExtra(ORIG_TXN_ID) -> {
                                fetchTxnDetails(intent.getStringExtra(ORIG_TXN_ID).toString())
                            }
                        }
                    }
                    cancelButton { finish() }
                    isCancelable = false
                }.show()
            } else {
//                hideDialog()
                llProgressBar.visibility = View.GONE
                collectDetails = response.transactionList[0] //It should contain only one item.
                if ( !(getStringData(this@TxnCollectApproveActivity, MRCH_TXN_ID).isNullOrBlank())) {
                    //showDialog()
                    //callApproveCollectInitApi()

                    collectTxnId = collectDetails.transactionId.orEmpty()
                    val bic = collectDetails.selfBIC.orEmpty()

                    callAccountSelection(collectTxnId, bic)
                } else {
                    initializeView()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog.cancel()
        llProgressBar.visibility = View.GONE

    }
}
