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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.Keep
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import android.text.format.DateUtils
import android.util.Log
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.transaction.history.TxnHistoryActivity
import kotlinx.coroutines.*
import retrofit2.HttpException

private const val ACTION_FETCH_NOTIFICATIONS = "com.indepay.umps.pspsdk.utils.action.NOTI"
private val uiScope = CoroutineScope(Dispatchers.Main)
private val bgDispatcher: CoroutineDispatcher = Dispatchers.IO
private lateinit var notificationManager: NotificationManager
private val TAG = "NotificationService"
private val CHANNEL_ID = "INDEPAY_CHANNEL"
private val NOTIFICATION_ID = 0
private val REQ_CODE_NOTIFICATION = 0

@Keep
class NotificationService : JobIntentService() {


    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            ACTION_FETCH_NOTIFICATIONS -> {
                val userToken = intent.getStringExtra(USER_TOKEN)
                if (userToken != null) {
                    handleActionFoo(userToken)
                }
            }
        }
    }

    private fun handleActionFoo(userToken: String) {
        callFetchNotificationIdsApi(userToken)
    }


    private fun callFetchNotificationIdsApi(userToken: String) = uiScope.launch {
        val result = withContext(bgDispatcher) {
            loadNotificationIdData()
        }

        if (result is Result.Success) {
            if (!(result.data.isEmpty())) {
                callFetchPrivateTokenApi(
                        accessToken = userToken,
                        custPSPId = getPspId(this@NotificationService),
                        appName = getAppName(this@NotificationService),
                        actionCallback = { callFetchNotificationsByIdApi(result.data) })
            }
        }
    }

    private suspend fun loadNotificationIdData(): Result<ArrayList<String>>? {
        return try {
            val apiService = SdkNotificationApiService.create(getUserName(this))
            val response = apiService.getNotificationMsgIdAsync(getPspId(this))
            val result = response.await()
            Result.Success(result)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    private fun callFetchNotificationsByIdApi(notificationIds: ArrayList<String>) = uiScope.launch {

        val result = withContext(bgDispatcher) {
            loadNotificationsData(notificationIds)
        }

        if (result is Result.Success) {
            if (!result.data.isEmpty()) {
                result.data.forEach { notificationFetchResponse: NotificationFetchResponse ->
                    addNotification(message = notificationFetchResponse.msgTxt.orEmpty(), txnId = notificationFetchResponse.txnId.orEmpty())
                }
            }
        } else if (result is Result.Error) {
            result.exception.message?.let { Log.e(TAG, it) }
        }
    }

    private suspend fun loadNotificationsData(notificationIds: ArrayList<String>): Result<ArrayList<NotificationFetchResponse>>? {
        return try {
            val request = NotificationFetchRequest(
                    custPSPId = getPspId(this),
                    accessToken = getAccessToken(this),
                    acquiringSource = AcquiringSource(
                            appName = getAppName(this)
                    ),
                    requestedLocale = getCurrentLocale(this),
                    msgIdList = notificationIds
            )
            val notiApiService = SdkApiService.create(getPspSslConfig(this), getUserName(this))
            val response = notiApiService.fetchNotificationByMsgIdsAsync(request)
            val result = response.await()
            Result.Success(result)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    private fun callFetchPrivateTokenApi(accessToken: String, custPSPId: String?, appName: String,
                                         actionCallback: () -> Job) = uiScope.launch {

        if ((getAccessTokenExpireTime(this@NotificationService) - System.currentTimeMillis()) < DateUtils.MINUTE_IN_MILLIS) {
            val result = withContext(bgDispatcher) {
                loadRefreshTokenData(accessToken = accessToken, custPSPId = custPSPId, appName = appName)
            }

            if (result is Result.Success) {
                saveAccessToken(this@NotificationService, result.data.token.orEmpty())
                saveAccessTokenIssueTime(this@NotificationService, result.data.issuedAtMillis.orZero())
                saveAccessTokenExpireTime(this@NotificationService, result.data.validTillMillis.orZero())
                actionCallback()
            } else if (result is Result.Error) {
                result.exception.printStackTrace()
            }
        } else {
            actionCallback()
        }
    }

    private suspend fun loadRefreshTokenData(accessToken: String, custPSPId: String?, appName: String): Result<AccessToken> {
        return try {
            val request = TrackingRequest(
                    custPSPId = custPSPId,
                    accessToken = accessToken,
                    acquiringSource = AcquiringSource(
                            appName = appName
                    )
            )

            val sslData = getPspSslConfig(this)
            val apiService = SdkApiService.create(sslData, getUserName(this))
            val response = apiService.fetchAccessTokenAsync(request)
            val result = response.await()
            Result.Success(result)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    companion object {
        @Keep
        const val USER_TOKEN = "user_token"
        private const val JOB_ID = 1000

        @Keep
        @JvmStatic
        fun enqueueWork(context: Context, work: Intent) {
            work.apply {
                action = ACTION_FETCH_NOTIFICATIONS
            }
            enqueueWork(context, NotificationService::class.java, JOB_ID, work)
        }
    }

    private fun addNotification(message: String, txnId: String) {

        val builder = NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("Collect Request")
                .setContentText(message)
                .setAutoCancel(true)
                .setChannelId(CHANNEL_ID)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, TxnHistoryActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        //notificationIntent.putExtra(TxnCollectApproveActivity.ORIG_TXN_ID, txnId)
        notificationIntent.putExtra(TxnHistoryActivity.PENDING_TXN_ONLY, true)
        val contentIntent = PendingIntent.getActivity(this, REQ_CODE_NOTIFICATION, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(contentIntent)

        createNotificationChannel()
        notificationManager.notify(NOTIFICATION_ID, builder.build())

    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, "SPN Channel", importance)
            channel.description = "SPN Collect Request"
            notificationManager.createNotificationChannel(channel)
        }

    }
}
