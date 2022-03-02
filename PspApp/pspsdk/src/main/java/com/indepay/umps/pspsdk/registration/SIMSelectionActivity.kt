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

package com.indepay.umps.pspsdk.registration

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.accountSetup.BankAccountListActivity
import com.indepay.umps.pspsdk.adapter.SIMAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.SmsDeliveredReceiver
import com.indepay.umps.pspsdk.models.SmsSentReceiver
import com.indepay.umps.pspsdk.utils.NetworkConnectivityReceiver
import kotlinx.android.synthetic.main.activity_send_sms.*
import kotlinx.android.synthetic.main.alert_dialog_custom.*
import kotlinx.android.synthetic.main.alert_dialog_custom.view.*
import kotlinx.android.synthetic.main.listview.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import kotlin.coroutines.CoroutineContext


class SIMSelectionActivity : SdkBaseActivity() {

    private val uiContext: CoroutineContext = Dispatchers.Main
    private val bgContext: CoroutineContext = Dispatchers.Default
    private var networkConnectivityReceiver : NetworkConnectivityReceiver? = null

    companion object {
        const val DEMO_KEY = "demo_key"
        const val ERROR_MSG = "error_msg"
        const val SUCCESS_MSG = "success_msg"
        const val ERROR_CODE = "error_code"
        const val ERROR_REASON = "error_reason"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_sms)

        val intentFilter = IntentFilter()
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        intentFilter.priority = 100
        networkConnectivityReceiver = NetworkConnectivityReceiver()
        registerReceiver(networkConnectivityReceiver, intentFilter)

        simSelectiondialog()

    }

    private fun isValidateData(intent: Intent?): Boolean {
        //TODO: Implement actual input validation logic
        intent?.getStringExtra(DEMO_KEY)?.let {
            return true
        }

        return false
    }

    private fun returnResult(resultCode: Int = Activity.RESULT_OK, messageMap: Map<String, String>) {
        val returnIntent = Intent()
        messageMap.forEach { (key, value) ->
            returnIntent.putExtra(key, value)
        }
        setResult(resultCode, returnIntent)
        finish()
    }

    private fun mockApiCall() = GlobalScope.launch(uiContext) {
        showLoading()

        //val result = dataProvider.loadData()
        //getNetworkOperatorsName(applicationContext)

        hideLoading()

        val resultMap = HashMap<String, String>()
        resultMap[SUCCESS_MSG] = "Success!"
        returnResult(Activity.RESULT_OK, resultMap)
    }

    private fun showLoading() {
//        button_proceed.visibility = View.GONE
//        progressBar.visibility = View.VISIBLE
        llLayout.visibility = View.VISIBLE
    }

    private fun hideLoading() {
//        progressBar.visibility = View.GONE
        llLayout.visibility = View.GONE
        //   button_proceed.visibility = View.VISIBLE
    }

    @SuppressLint("MissingPermission")
    private fun simSelectiondialog() {
        showLoading()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {

            val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList

            if (subscriptionInfoList == null) {

                alert(resources.getString(R.string.no_sim), resources.getString(R.string.sim_error)) {
                    okButton {
                        finish()
                    }
                }.show()

            } else {
                val adapter = SIMAdapter(this, subscriptionInfoList)
                list_item.adapter = adapter

                list_item.setOnItemClickListener { parent, view, position, id ->

                    val subId = subscriptionInfoList[position].subscriptionId// change index to 1 if you want to get Subscrption Id for slot 1.
                    //sendSms("", subId.toString())

                    val builder = Dialog(this)
                    val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val dialog: View = inflater.inflate(R.layout.alert_dialog_custom, registration_alert)
                    builder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.registration_alert.background = resources.getDrawable(R.drawable.background_shape)
                    builder.setContentView(dialog, ViewGroup.LayoutParams(500, ViewGroup.LayoutParams.WRAP_CONTENT))
                    builder.setCancelable(false)
                    //val registrationDialog : Dialog
                    builder.show()
                    builder.txttitle.text = resources.getString(R.string.register_success)
                    builder.txtMessage.text = resources.getText(R.string.registration_success)
                    builder.buttonOk.setOnClickListener {
                        finish()
                        startActivity(Intent(this, BankAccountListActivity::class.java))
                    }
                }

            }

        } else {
            //TODO: Handle older versions
            SmsManager.getDefault()
        }
        hideLoading()
    }

    private fun sendSms(message: String, subId: String) {
        val phoneNumber = "0000000000"
        val sentPendingIntents = ArrayList<PendingIntent>()
        val deliveredPendingIntents = ArrayList<PendingIntent>()
        val sentPI = PendingIntent.getBroadcast(this, 0,
                Intent(this, SmsSentReceiver::class.java), 0)
        val deliveredPI = PendingIntent.getBroadcast(this, 0,
                Intent(this, SmsDeliveredReceiver::class.java), 0)
        try {
            val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SmsManager.getSmsManagerForSubscriptionId(Integer.parseInt(subId))
            } else {
                TODO("VERSION.SDK_INT < LOLLIPOP_MR1")
            }
            val mSMSMessage = sms.divideMessage(message)
            for (i in 0 until mSMSMessage.size) {
                sentPendingIntents.add(i, sentPI)
                deliveredPendingIntents.add(i, deliveredPI)
            }

            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
                    sentPendingIntents, deliveredPendingIntents)

        } catch (e: Exception) {

            e.printStackTrace()
            Toast.makeText(baseContext, resources.getString(R.string.sms_send_fail), Toast.LENGTH_SHORT).show()
        }

    }

    /*private fun sendError(error_code: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ERROR_CODE, error_code)
        returnIntent.putExtra(ERROR_REASON, getErrorMsg(error_code))
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }*/

}
