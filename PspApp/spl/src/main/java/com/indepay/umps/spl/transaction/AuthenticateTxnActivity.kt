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
 *  * 16-10-2019     Mayank D   Changed text message for Jira Ticket RR-99
 *
 *  *****************************************************************************
 */

package com.indepay.umps.spl.transaction

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Keep
import android.text.TextUtils
import android.view.View
import com.google.gson.Gson
import com.indepay.umps.spl.R
import com.indepay.umps.spl.activity.PinPadActivity
import com.indepay.umps.spl.activity.SplBaseActivity
import com.indepay.umps.spl.models.*
import com.indepay.umps.spl.utils.*
import com.indepay.umps.spl.utils.SplMessageUtils.createEncryptionKeyRetrievalRequest
import kotlinx.android.synthetic.main.activity_authnticate_transactions.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.startActivityForResult
import retrofit2.HttpException
import java.net.ConnectException
import java.nio.charset.StandardCharsets

@Keep
class AuthenticateTxnActivity : SplBaseActivity() {

    private lateinit var dataIntent: Intent
    private lateinit var apiService: SplApiService
    private lateinit var encRetResp: EncryptionKeyRetrievalResponse
    private lateinit var appId: String
    private lateinit var transactionType: TransactionType

    @Keep
    companion object : SplCommonMembers() {
        const val TXN_ID = "txn_id"
        const val BIC = "bic"
        const val APP_ID = "app_id"
        const val MOBILE_NO = "mobile_no"
        const val PSP_ID = "psp_id"
        const val TXN_TYPE = "TXN_TYPE"

        const val AMOUNT = "amount"
        const val PAYEE_NAME = "payee_name"
        const val BANK_NAME = "bank_name"
        const val ACCOUNT_NO = "account"
        const val NOTE = "note"
        const val _LOCALE ="locale"
    }

    /**
     * This method is a part of SPL, called by SDK, to authenticate the transaction details.
     *              and Calling for Pin Pad activity.
     * @param
     * @return
     */


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authnticate_transactions)

        dataIntent = intent
        appId = dataIntent.getStringExtra(APP_ID).toString()
        //saveLocale(this, dataIntent.getStringExtra(_LOCALE).toString())

        try {
            transactionType = TransactionType.fromValue(dataIntent.getStringExtra(TXN_TYPE).toString())
        } catch (e: Exception) {
            finish()
        }

        apiService = SplApiService.create(getSSLConfig(this))

        if (!TextUtils.isEmpty(dataIntent.getStringExtra(PSP_ID)) && getPspId(this) == dataIntent.getStringExtra(PSP_ID)) {
            authenticate_txn_progress.visibility = View.VISIBLE
             retrieveKeys()
        } else {
//            setResult(Activity.RESULT_CANCELED,//Failure intent data)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

    }

    //Retrieve keys call
    private fun retrieveKeys() = uiScope.launch {
        val result = withContext(bgDispatcher) {
            retrieveKeysRequest()
        }

        if (result is Result.Success) {
            encRetResp = result.data
            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {

                startActivityForResult<PinPadActivity>(100,
                        PinPadActivity.IS_REQUIRED_CONFIRMATION to false,
                        PinPadActivity.ACCOUNT_NO to intent.getStringExtra(ACCOUNT_NO),
                        PinPadActivity.AMOUNT to intent.getStringExtra(AMOUNT),
                        PinPadActivity.BANK_NAME to intent.getStringExtra(BANK_NAME),
                        PinPadActivity.PAYEE_NAME to intent.getStringExtra(PAYEE_NAME),
                        PinPadActivity.NOTE to intent.getStringExtra(NOTE),
                        PinPadActivity.TXN_ID to intent.getStringExtra(TXN_ID),
                        PinPadActivity.TXN_TYPE to intent.getStringExtra(TXN_TYPE)
                )

            } else {
                alert(resources.getString(R.string.spl_continue_trans), resources.getString(R.string.spl_device_failed)) {
                    okButton {
                        finish()
                    }
                }.show().setCancelable(false)
            }
        }else if(result is Result.Error){
            if(result.exception.cause is ConnectException){
                showError(resources.getString(R.string.spl_no_internet_connec),resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            }else {
                showError(resources.getString(R.string.spl_server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private fun sendError(error_code: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ERROR_CODE, error_code)
        returnIntent.putExtra(ERROR_REASON, getErrorMessage(error_code))
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    private suspend fun retrieveKeysRequest(): Result<EncryptionKeyRetrievalResponse> {

        return try {
            val encKeyRetReq: EncryptionKeyRetrievalRequest = createEncryptionKeyRetrievalRequest(
                    txnId = dataIntent.getStringExtra(TXN_ID).toString(),
                    bic = dataIntent.getStringExtra(BIC).toString(),
                    context = this,
                    mobileNumber = dataIntent.getStringExtra(MOBILE_NO).toString(),
                    resetCredentialCall = false,
                    symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                    activity = this,
                    appId = appId,
                    txnType = transactionType
            )
            val response = apiService.createRetrieveKeysRequest(encKeyRetReq)
            val result = response.await()
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK == resultCode && null != data) {
            var txtPin = data.getStringExtra(PinPadActivity.MPIN)
            if (null != encRetResp) {
                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
                pubKeyArr?.let {
                    val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(encRetResp.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                    symmetricKey?.let {
                        val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(encRetResp.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
                        responseData.let {
                            val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
                            responseObj?.let {
                                val ki = responseObj.bankKi
                                val credFormat = responseObj.resetCredentialFormat
                                val bankKey = responseObj.publicKey
                                val sessionKey = responseObj.sessionKey
                                if (null != bankKey && null != ki && null != sessionKey) {
                                    createCredSubmissionRequest(bankKey, ki, sessionKey, txtPin.toString(), transactionType)
                                }

                            }

                        }
                    }
                }
            } else {
                sendError("A251")
            }
        }else if(Activity.RESULT_CANCELED == resultCode){
            setResult(Activity.RESULT_CANCELED)
            finish()
        }else{
            sendError("Something is wrong")
        }

    }

    private fun createCredSubmissionRequest(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType) = uiScope.launch {
        showDialog()
        val result = withContext(bgDispatcher) {
            createCredSubmissionReq(bankKey, ki, sessionKey, mpin, txnType)
        }
        hideDialog()
        if (result is Result.Success) {
            //Return to sdk
            if(null != result.data) {
                setResult(Activity.RESULT_OK)
                finish()
            }else{
                sendError("A251")
            }

        }else if(result is Result.Error){
            if(result.exception.cause is ConnectException){
                showError(resources.getString(R.string.spl_no_internet_connec),resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            }else {
                showError(resources.getString(R.string.spl_server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private suspend fun createCredSubmissionReq(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType): Result<CredentialSubmissionResponse> {
        return try {
            val request: CredentialSubmissionRequest = SplMessageUtils.createCredentialSubmissionRequest(
                    txnId = dataIntent.getStringExtra(TXN_ID).toString(),
                    pspId = dataIntent.getStringExtra(PSP_ID).toString(),
                    mobileNumber = dataIntent.getStringExtra(MOBILE_NO).toString(),
                    activity = this,
                    bankKey = bankKey,
                    ki = ki,
                    mpin = mpin,
                    sessionKey = sessionKey,
                    transactionType = txnType,
                    appId = appId
            )

            val response = apiService.createCredentialSubmissionRequest(request)
            val result = response.await()
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }
}

