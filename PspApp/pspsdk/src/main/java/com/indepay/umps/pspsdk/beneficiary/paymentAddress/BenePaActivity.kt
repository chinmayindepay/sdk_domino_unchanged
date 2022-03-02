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

package com.indepay.umps.pspsdk.beneficiary.paymentAddress

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnBeneficiaryListInteractionListener
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.transaction.payment.TxnPaymentActivity
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_bene_pa.*
import org.jetbrains.anko.*

class BenePaActivity : SdkBaseActivity(), OnBeneficiaryListInteractionListener {

    private val benePaList = ArrayList<Beneficiary>()
    private val BENE_TYPE_PA = "PA"

    companion object : SdkCommonMembers()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bene_pa)

        val layoutManager = android.support.v7.widget.LinearLayoutManager(this)
        val itemDecoration = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)
        rv_pa_bene_list.layoutManager = layoutManager
        rv_pa_bene_list.addItemDecoration(itemDecoration)
        rv_pa_bene_list.adapter = BenePaAdapter(benePaList, this)

        button_proceed.setOnClickListener {
            showPaValidationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                arrayApiToCall = { sdkApiService ->
                    sdkApiService.getBeneficiariesAsync(
                            appName = getAppName(this),
                            accessToken = getAccessToken(this),
                            custPSPId = getPspId(this)
                    )
                },
                successArrayCallback = { commonResponse -> onSuccessBeneListFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun onSuccessBeneListFetch(response: ArrayList<out CommonResponse>) {
        val beneficiaryList = response as ArrayList<Beneficiary>
        if (beneficiaryList.isNotEmpty()) {
            benePaList.clear()
            benePaList.addAll(beneficiaryList)
            rv_pa_bene_list.adapter.notifyDataSetChanged()
        } else {
            alert(message = "Please add a new beneficiary.", title = "No added beneficiary found!") {
                okButton { showPaValidationDialog() }
                cancelButton { finish() }
                isCancelable = false
            }.show()
        }
    }

    private fun showPaValidationDialog() {
        val factory = LayoutInflater.from(this)
        val validatePaDialog = factory.inflate(R.layout.dialog_validate_pa, null)
        alert {
            title = "Validate Payment Address"
            customView = validatePaDialog
            val paymentAddress = validatePaDialog.find<EditText>(R.id.edt_payment_address).text
            positiveButton("Submit") {
                if (paymentAddress.isNullOrBlank()) {
                    toast("Please enter a valid payment address")
                } else {
                    validatePaymentAddress(paymentAddress.toString())
                }
            }
        }.show()
    }

    private fun validatePaymentAddress(paymentAddress: String) {
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.validatePaymentAddressAsync(
                            ValidatePaRequest(
                                    custPSPId = getPspId(this),
                                    requestedLocale = getCurrentLocale(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    paymentAddress = paymentAddress
                            )
                    )
                },
                successCallback = {
                    if (it is ValidatePaResponse) {
                        addNewBeneficiary(it.name, paymentAddress)
                    }
                },
                errorCallback = {
                    alert("Please enter a valid payment address.", "Payment Address Invalid") {
                        okButton { }
                        isCancelable = false
                    }.show()
                }
        )
    }

    private fun addNewBeneficiary(beneName: String?, payemntAddress: String) {
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.addBeneficiaryAsync(
                            AddBeneficiaryRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    requestedLocale = getCurrentLocale(this),
                                    beneName = beneName,
                                    benePaymentAddr = payemntAddress,
                                    beneType = BENE_TYPE_PA
                            )
                    )
                },
                successCallback = {
                    if (it is AddBeneficiaryResponse) {
                        alert("Name: ${it.beneName} \n PaymentAddress: ${it.benePaymentAddr}",
                                "Successfully added beneficiary") {
                            okButton { }
                            isCancelable = false
                        }.show()
                    }
                },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    override fun onBeneficiaryListInteraction(beneficiary: Beneficiary) {
        startActivity<TxnPaymentActivity>(
                TxnPaymentActivity.ACCOUNT_NO to beneficiary.beneAccountNo,
                TxnPaymentActivity.TRANSACTION_TYPE to TxnPaymentActivity.TXN_TYPE_PAY,
                TxnPaymentActivity.BENE_ID to beneficiary.beneId,
                TxnPaymentActivity.PAYEE_NAME to beneficiary.beneName
                //TxnPaymentActivity.MOBILE_NO to
                //TxnPaymentActivity.INITIATOR_PA_ACCOUNT_ID to getPaAccountId(this)
                //TxnPaymentActivity.INITIATOR_ACCOUNT_ID to beneficiary.
        )
    }
}
