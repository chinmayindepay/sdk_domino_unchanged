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

package com.indepay.umps.pspsdk.accountSetup

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.BankAccountListAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnBankAccountListInteractionListner
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_bank_account_list.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import org.jetbrains.anko.okButton

class BankAccountListActivity : SdkBaseActivity(), OnBankAccountListInteractionListner {

    private lateinit var txnId: String

    companion object {
        const val BANK_BIC = "bank_bic"
        const val BANK_NAME = "bank_name"
        const val DEBIT_CARD_NO = "debit_card_no"
    }

    /**
     * This method is called by SDK itself, from EcosystemBanksActivity to
     *              Fetch List of Mapped Bank account.
     * @param AccountDetailsRequest
     * @return AccountDetailsResponse
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_account_list)

        val layoutManager = android.support.v7.widget.LinearLayoutManager(this@BankAccountListActivity)
        val itemDecoration = android.support.v7.widget.DividerItemDecoration(this@BankAccountListActivity, layoutManager.orientation)
        rv_bank_acc_list.layoutManager = layoutManager
        rv_bank_acc_list.addItemDecoration(itemDecoration)
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.initiateAccDetailAsync(
                            AccountDetailsRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    bic = intent.getStringExtra(BANK_BIC),
                                    cardLast6Digits = intent.getStringExtra(DEBIT_CARD_NO)
                                    //paId = getPaId(this)
                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessAccDetailsFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }

    private fun onSuccessAccDetailsFetch(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE

        if (result is AccountDetailsResponse) {
            result.transactionId?.let {
                txnId = result.transactionId
                result.listOfMappedAccount?.let {
                    if (it.isNotEmpty()) {
                        val bankAccList = result.listOfMappedAccount
                        val picasso = getPicassoInstance(this@BankAccountListActivity, getPspSslConfig(this@BankAccountListActivity))
                        val banksAdapter = BankAccountListAdapter(
                                bankAcList = bankAccList,
                                listener = this@BankAccountListActivity,
                                picassoInstance = picasso,
                                accessToken = getAccessToken(this@BankAccountListActivity),
                                appName = getAppName(this@BankAccountListActivity),
                                custPspId = getPspId(this@BankAccountListActivity))
                        rv_bank_acc_list.adapter = banksAdapter
                    } else {
                        alert(message = resources.getString(R.string.no_acc_with_mobile_number), title = intent.getStringExtra(BANK_NAME)) {
                            okButton { finish() }
                            isCancelable = false
                        }.show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (null != dialog) {
            dialog.dismiss()
        }
        super.onDestroy()
    }

    override fun onBankAccountListInteraction(bankAccount: BankAccount) {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.mapAccountToProfileAsync(
                            MapAccountToProfileRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    transactionId = txnId,
                                    requestedLocale = getCurrentLocale(this),
                                    //paId = getPaId(this),
                                    fetchAccountDetailsAccountSeq = bankAccount.mappedAccountSeq
                            )
                    )
                },
                successCallback = { response ->
                    llProgressBar.visibility = View.GONE

//                    hideDialog()
                    if (response is MapAccountToProfileResponse) {
                        alert(resources.getString(R.string.acc_succ_mapped) + getMobileNo(this) + "\n " +
                                resources.getString(R.string.set_acc_as_def), resources.getString(R.string.success)) {
                            saveBooleanData(this@BankAccountListActivity, IS_REGISTERED, true)
                            setResult(Activity.RESULT_OK)
                            okButton {
                                response.accountTokenId?.let { acId ->
                                    callSetDefaultAccountApi(
                                            bic = bankAccount.bic,
                                            accountNo = bankAccount.accountNumber.orEmpty(),
                                            //paAccountId = response.paAccountId.toString())
                                            accountTokenId = acId)
                                }
                            }
                            cancelButton {
                                finish()
                            }
                            isCancelable = false
                        }.show()
                    }
                },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun callSetDefaultAccountApi(bic: String, accountNo: String, accountTokenId: Long) {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.setDefaultAccountAsync(
                            SetDefaultAccountRequest(
                                    bic = bic,
                                    //paAccountId = paAccountId,
                                    //paId = getPaId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    accountTokenId = accountTokenId,
                                    custPSPId = getPspId(this),
                                    requestedLocale = getCurrentLocale(this)
                            )
                    )
                },
                successCallback = {
//                    hideDialog()
                    llProgressBar.visibility = View.GONE

                    //savePaAccountId(this, paAccountId)
                    saveAccountTokenId(this, accountTokenId)
                    alert(resources.getString(R.string.acc)+"$accountNo"+resources.getString(R.string.def_acc), resources.getString(R.string.success)) {
                        okButton {
                            finish() }
                        isCancelable = false
                    }.show()
                },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }

}
