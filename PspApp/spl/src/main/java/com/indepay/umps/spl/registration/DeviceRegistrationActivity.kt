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

package com.indepay.umps.spl.registration

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.Keep
import android.util.Log
import com.google.gson.Gson
import com.indepay.umps.spl.R
import com.indepay.umps.spl.activity.SplBaseActivity
import com.indepay.umps.spl.models.SplRegistrationResponse
import com.indepay.umps.spl.models.UserRegistrationResponsePayload
import com.indepay.umps.spl.models.UserRegistrationTxnSms
import com.indepay.umps.spl.utils.*
import com.indepay.umps.spl.utils.Base64
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import retrofit2.HttpException
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.*

@Keep
class DeviceRegistrationActivity : SplBaseActivity() {

    private lateinit var splId: String
    private val REQ_CODE_READ_PHONE_STATE = 100800

    @Keep
    companion object : SplCommonMembers() {
        const val APP_ID = "app_id"
        const val PSP_ORG_ID = "psp_org_id"
        const val SESSION_KEY = "session_key"
        const val REGN_TXN_ID = "regn_txn_id"
        const val MOBILE_NUMBER = "mobile_mumber"
    }

    /**
     * This method is a part of SPL, called by SDK, to ask to allow the phone permissions, while device registration.
     *
     * @param
     * @return
     */

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("oncreate_device_reg","started")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_device_registration)
        Log.e("oncreate_device_reg","in device_reg")
        if (intent != null) {
            if (!(intent.getStringExtra(SESSION_KEY).isNullOrEmpty())) {
                if (!(intent.getStringExtra(REGN_TXN_ID).isNullOrEmpty())) {
                    if (!(intent.getStringExtra(APP_ID).isNullOrEmpty())) {
                        if (!(intent.getStringExtra(PSP_ORG_ID).isNullOrEmpty())) {
                            requestPermission()
                        } else {
                            sendError("A007")
                        }
                    } else {
                        sendError("A006")
                    }
                } else {
                    sendError("A005")
                }
            } else {
                sendError("A004")
            }
        } else {
            sendError("A003")
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQ_CODE_READ_PHONE_STATE)
        } else {
            registerUser()
        }
    }

    //TODO: For development purpose, remove on production
    private suspend fun loadSmsApiData(txnId: String?) {

        var mobileNo = intent.getStringExtra(MOBILE_NUMBER)

        try {
            val request = UserRegistrationTxnSms(
                    mobileNumber = mobileNo,
                    txnId = txnId
            )

            val sslData = getSSLConfig(this)
            val apiService = SplApiService.create(sslData)
            val response = apiService.mockSmsRequestAsync(request)
            val result = response.await()
            if (result.isSuccessful) {
                Log.i("Registration", "Sms sending suscess!")
            } else {
                Log.e("Registration", "Sms sending failed!")
            }
        } catch (e: Throwable) {
            Log.e("Registration", "Sms sending failed!")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQ_CODE_READ_PHONE_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerUser()
            } else {
                alert(resources.getString(R.string.spl_permission_required)) {
                    okButton {
                        sendError("A102")
                    }
                    isCancelable = false
                }.show()
            }
        }
    }

    private fun registerUser() = uiScope.launch {
            showDialog()
            Log.e("registerUser","entered registered user")

        withContext(bgDispatcher) {
                loadSmsApiData(intent.getStringExtra(REGN_TXN_ID))
            }
        Log.e("registerUser","after sms")

            val result = withContext(bgDispatcher) {
                loadRegistrationData()

            }

        Log.e("registerUser","after registration")

            hideDialog()
            if (result is Result.Success && null != result.data.commonResponse && result.data.commonResponse.success == true) {
                val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(result.data.commonResponse.symmetricKey?.toByteArray(StandardCharsets.UTF_8),
                        PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getDecoder().decode(intent.getStringExtra(SESSION_KEY))))
                val userRegdRespByte = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.data.userRegistrationResponsePayloadEnc?.toByteArray(StandardCharsets.UTF_8), symmetricKey)
                val userRegdResp = Gson().fromJson(userRegdRespByte.toString(StandardCharsets.UTF_8), UserRegistrationResponsePayload::class.java)
                val splKey = userRegdResp.splKey
                val pspIdentifier = userRegdResp.pspIdentifier
              //  Log.d("symmetric key","Sudhir keyyyy::"+symmetricKey)
             //   Log.d("symmetric key","Sudhir userRegdRespByte::"+userRegdRespByte)


                sendSuccess(splKey, pspIdentifier)
            } else if (result is Result.Error) {
                if (result.exception.cause is ConnectException) {
                    showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                    sendError("A152")
                } else {
                    showError(resources.getString(R.string.spl_server_error), result.toString())
                    sendError("A153")
                }
            }
            Log.e("registerUser","exited registered user")

    }

    private fun sendError(error_code: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ERROR_CODE, error_code)
        returnIntent.putExtra(ERROR_REASON, getErrorMessage(error_code))
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    private fun sendSuccess(splKey: String?, pspIdentifier: String?) {
        if (splKey != null && pspIdentifier != null) {
            saveSplData(this, splKey, pspIdentifier, splId)
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            if (splKey == null) {
                sendError("A009")
            } else if (pspIdentifier == null) {
                sendError("A008")
            }
        }
    }


    private suspend fun loadRegistrationData(): Result<SplRegistrationResponse> {
        Log.e("loadRegistrationData","In")
        splId = UUID.randomUUID().toString()
        return try {
            val request = SplMessageUtils.createSplRegistrationRequest(
                    sessionKey = intent.getStringExtra(SESSION_KEY).toString(),
                    txnId = intent.getStringExtra(REGN_TXN_ID).toString(),
                    splIdentifier = splId,
                    appId = intent.getStringExtra(APP_ID).toString(),
                    deviceId = getDeviceId(this@DeviceRegistrationActivity),
                    imei1 = getImei1(this@DeviceRegistrationActivity,this),
                    imei2 = getImei2(this@DeviceRegistrationActivity,this),
                    pspOrgId = intent.getStringExtra(PSP_ORG_ID).toString(),
                    activity = this
            )
            val sslData = getSSLConfig(this)
            val apiService = SplApiService.create(sslData)
            val response = apiService.registerUserAsync(request)
            val result = response.await()
            Result.Success(result)

        } catch (e: HttpException) {
            Log.e("loadRegistrationData",e.message())
            Result.Error(e)
        }

//         catch (e: Throwable) {
//            Log.e ("loadRegistrationData", "some error")
//            Result.Error(e)
//        }

        Log.e("loadRegistrationData","Out")

    }

    override fun onDestroy() {
        dialog.let { it.dismiss() }
        super.onDestroy()
    }
}
