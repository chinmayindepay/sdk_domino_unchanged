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

package com.indepay.umps.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.indepay.umps.BuildConfig
import com.indepay.umps.R
import com.indepay.umps.models.AccessToken
import com.indepay.umps.models.AcquiringSource
import com.indepay.umps.models.UserToken

import com.indepay.umps.pspsdk.accountSetup.ManageAccountActivity
import com.indepay.umps.pspsdk.registration.DeregistrationActivity
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import com.indepay.umps.utils.*
import com.indepay.umps.utils.getAccessToken
import com.indepay.umps.utils.getAccessTokenExpireTime
import com.indepay.umps.utils.getStringData
import com.indepay.umps.utils.saveAccessToken
import com.indepay.umps.utils.saveAccessTokenExpireTime
import com.indepay.umps.utils.saveAccessTokenIssueTime
import kotlinx.android.synthetic.main.usr_profile.*
import kotlinx.android.synthetic.main.usr_profile.customer_name
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.jetbrains.anko.*
import java.nio.charset.StandardCharsets

class ProfileActivity : BaseActivity() {


    private val KEY = BuildConfig.MERCHANT_KEY
    private val KI = BuildConfig.MERCHANT_KI
    private val REQUEST_CODE_VALIDATE_ADD_PA = 100
    private val REQUEST_PROFILE = 200
    private val REQUEST_CODE_DEREGISTER_ACCOUNT = 201

    companion object : SdkCommonMembers()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.usr_profile)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = resources.getString(R.string.account)
        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)
        version_name.text = BuildConfig.VERSION_NAME

        //startActivityForResult<UserProfileActivity>(REQUEST_PROFILE)
        var mobile = getStringData(this, RegistrationActivity.MOBILE_NUMBER)
        txt_mobile.text = mobile
        txt_cust_mbl.text = getStringData(this, RegistrationActivity.MOBILE_NUMBER)
        llBankAccounts.setOnClickListener {
            //No scope to use database
            fetchAppToken { token ->
                startActivity(intentFor<ManageAccountActivity>().newTask().singleTop())
            }
            true
        }

        txt_unregister.setOnClickListener {
            startActivityForResult<DeregistrationActivity>(REQUEST_CODE_DEREGISTER_ACCOUNT)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_VALIDATE_ADD_PA ->
                if (resultCode == Activity.RESULT_CANCELED) {
                    //hello_text.text = data?.getStringExtra(RegistrationActivity.SUCCESS_MSG)
                    /*toast("Try again after sometime!")*/
                }
            REQUEST_PROFILE ->
                if (resultCode == Activity.RESULT_OK) {
                    if (null != data) {
                        /*if (null != data.getStringExtra(RegistrationActivity.) && null != data.getStringExtra(UserProfileActivity.ACC_NO)) {
                            txt_bank.text = getString(R.string.bank_acc_name_number, data.getStringExtra(UserProfileActivity.DEFAULT_BANK_NAME), data.getStringExtra(UserProfileActivity.ACC_NO))
                        }*/
                        if (null != data.getStringExtra(RegistrationActivity.MOBILE_NUMBER)) {
                            txt_cust_mbl.text = data.getStringExtra(RegistrationActivity.MOBILE_NUMBER)
                        }
                        if (null != data.getStringExtra(RegistrationActivity.USER_NAME)) {
                            customer_name.text = data.getStringExtra(RegistrationActivity.USER_NAME)
                        }
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    toast(data?.getStringExtra("result").toString())
                }
            REQUEST_CODE_DEREGISTER_ACCOUNT ->
                if (resultCode == Activity.RESULT_OK) {
                    getSharedPreferences(getString(R.string.base_shared_pref), Context.MODE_PRIVATE).edit().clear().apply()
                    longToast("Deregistration Successful!").show()
                    finish()
                }

        }
    }

    private fun fetchAppToken(actionCallback: (token: String) -> Unit) = uiScope.launch {
        if ((getAccessTokenExpireTime(this@ProfileActivity) - System.currentTimeMillis()) < DateUtils.MINUTE_IN_MILLIS) {
//            showDialog()
            llProgressBar.visibility = View.VISIBLE


            val result = withContext(bgDispatcher) {
                loadAppTokenRequestData()
            }
            llProgressBar.visibility = View.VISIBLE

          //  hideDialog()
            result?.let { respStr ->
                val tokenRespStr = String(PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(respStr.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(KEY)), StandardCharsets.UTF_8)
                val accessToken = Gson().fromJson(tokenRespStr, AccessToken::class.java)
                accessToken?.let { tkn ->
                    saveAccessToken(this@ProfileActivity, tkn.token)
                    saveLoginToken(this@ProfileActivity, tkn.token)
                    saveAccessTokenIssueTime(this@ProfileActivity, tkn.issuedAtMillis)
                    saveAccessTokenExpireTime(this@ProfileActivity, tkn.validTillMillis)
                    actionCallback(tkn.token)
                }
            }
        } else {
            actionCallback(getAccessToken(this@ProfileActivity))

        }
    }

    private suspend fun loadAppTokenRequestData(): String? {
        return try {

            val userToken = UserToken(
                    acquiringSource = AcquiringSource(
                            appName = BuildConfig.APP_NAME
                    )
            )
            val reqJson = Gson().toJson(userToken)
            val requestString = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(reqJson.toByteArray(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(KEY)), StandardCharsets.UTF_8)

            val sslData = getBaseAppSslConfig(this)
            val apiService = BaseAppApiService.create(sslData)
            val response = apiService.fetchAppTokenAsync(
                    ki = KI,
                    request = RequestBody.create(("text/plain").toMediaType(), requestString)
            )
            val result = response.await()
            if (result.isSuccessful) {
                result.body()
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        }
    }


}