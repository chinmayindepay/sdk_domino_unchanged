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

import android.app.ProgressDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import com.indepay.umps.utils.NotificationSchedulerService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.indeterminateProgressDialog


open class BaseActivity : AppCompatActivity() {

    private var notificationJobId: Int = 0
    private lateinit var dialog: ProgressDialog
    protected val uiScope = CoroutineScope(Dispatchers.Main)
    protected val bgDispatcher: CoroutineDispatcher = Dispatchers.IO


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialog = indeterminateProgressDialog(message = "Please wait...", title = "Connecting Server") {
            isIndeterminate = true
            setCancelable(false)
        }
        if (dialog.isShowing) {
            dialog.cancel()
        }

        //Check if registered and service not started
        if (notificationJobId == 0) {
            val jobInfoBuilder = JobInfo.Builder(1, ComponentName(this, NotificationSchedulerService::class.java))
            jobInfoBuilder.setMinimumLatency(DateUtils.MINUTE_IN_MILLIS)
            jobInfoBuilder.setOverrideDeadline(DateUtils.MINUTE_IN_MILLIS)
            jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            jobInfoBuilder.setRequiresDeviceIdle(false)
            jobInfoBuilder.setRequiresCharging(false)
            jobInfoBuilder.setBackoffCriteria(5 * DateUtils.SECOND_IN_MILLIS, JobInfo.BACKOFF_POLICY_LINEAR)
            val notificationJob = jobInfoBuilder.build()
            notificationJobId = notificationJob.id
            (getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(notificationJob)
        }

    }

    protected fun showDialog() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    protected fun hideDialog() {
        if (dialog.isShowing) {
            dialog.cancel()
        }
    }

    override fun onStart() {
        super.onStart()
        val notificationServiceIntent = Intent(this, NotificationSchedulerService::class.java)
        startService(notificationServiceIntent)
    }

}
