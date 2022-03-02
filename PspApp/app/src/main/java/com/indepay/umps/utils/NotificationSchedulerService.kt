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

package com.indepay.umps.utils

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import com.google.gson.Gson
import com.indepay.umps.BuildConfig
import com.indepay.umps.models.AccessToken
import com.indepay.umps.models.AcquiringSource
import com.indepay.umps.models.UserToken
import com.indepay.umps.pspsdk.utils.NotificationService
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import java.nio.charset.StandardCharsets

class NotificationSchedulerService : JobService() {

    private val TAG = "NotiSchedulerService"
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private val bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun onStopJob(jobParameters: JobParameters?): Boolean {
        Log.e(TAG, "Service stopped")
        jobFinished(jobParameters, true)
        return true
    }

    override fun onStartJob(jobParameters: JobParameters?): Boolean {
        Log.e(TAG, "Service started")
        if (jobParameters != null) {

            fetchAppToken {
                val intent = Intent()
                intent.putExtra(NotificationService.USER_TOKEN, it)
                NotificationService.enqueueWork(this, intent)
            }
            jobFinished(jobParameters, true)
        }
        return true
    }

    private fun fetchAppToken(actionCallback: (token: String) -> Unit) = uiScope.launch {
        if ((getAccessTokenExpireTime(this@NotificationSchedulerService) - System.currentTimeMillis()) < DateUtils.MINUTE_IN_MILLIS) {
            val result = withContext(bgDispatcher) {
                loadAppTokenRequestData()
            }

            result?.let { respStr ->
                val tokenRespStr = String(PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(respStr.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)
                val accessToken = Gson().fromJson(tokenRespStr, AccessToken::class.java)
                accessToken?.let { tkn ->
                    saveAccessToken(this@NotificationSchedulerService, tkn.token)
                    saveAccessTokenIssueTime(this@NotificationSchedulerService, tkn.issuedAtMillis)
                    saveAccessTokenExpireTime(this@NotificationSchedulerService, tkn.validTillMillis)
                    actionCallback(tkn.token)
                }
             //   Log.d("Login","Sudhir access token Notif response::"+accessToken)

            }
        } else {
            actionCallback(getAccessToken(this@NotificationSchedulerService))
         //   Log.d("Login","Sudhir access token response Notifi else::"+ getAccessToken(this@NotificationSchedulerService))

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
                    Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)

            val sslData = getBaseAppSslConfig(this)
            val apiService = BaseAppApiService.create(sslData)
            val response = apiService.fetchAppTokenAsync(
                    ki = BuildConfig.MERCHANT_KI,
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }
}
