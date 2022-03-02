package com.indepay.umps.pspsdk.setting

import android.os.Bundle
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.accountSetup.ManageAccountActivity
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.transaction.history.TxnHistoryActivity
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.setting_activity.AccountManage
import kotlinx.android.synthetic.main.setting_activity.TransactionHistory
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.singleTop
import org.jetbrains.anko.startActivityForResult

class RTPActivity : SdkBaseActivity() {

    private val REQUEST_CODE_REGISTRATION = 10100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)
        if (!getBooleanData(this, IS_REGISTERED)) {
            if(getUserToken(this).isNullOrBlank()) {
                fetchAppToken { token ->
                    saveLocale(this, intent.getStringExtra(RegistrationActivity._LOCALE).toString())
                    saveAppName(this, intent.getStringExtra(RegistrationActivity.APP_ID).toString())
                    saveUserName(this, intent.getStringExtra(RegistrationActivity.USER_NAME).toString())
                    saveMobileNo(this, intent.getStringExtra(RegistrationActivity.USER_MOBILE).toString())

//                    showRegistrationDialog(token)
                    startActivityForResult<RegistrationActivity>(REQUEST_CODE_REGISTRATION,
                            RegistrationActivity.USER_NAME to getStringData(this, RegistrationActivity.USER_NAME),
                            RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                            RegistrationActivity.USER_MOBILE to getMobileNo(this),
                            RegistrationActivity.USER_TOKEN to token)
                    finish()

                }
            }else{
                fetchAppToken { token ->
                    saveLocale(this, intent.getStringExtra(RegistrationActivity._LOCALE).toString())
                    saveAppName(this, intent.getStringExtra(RegistrationActivity.APP_ID).toString())
                    saveUserName(this, intent.getStringExtra(RegistrationActivity.USER_NAME).toString())
                    saveMobileNo(this, intent.getStringExtra(RegistrationActivity.USER_MOBILE).toString())

//                showRegistrationDialog(getUserToken(this))
                    startActivityForResult<RegistrationActivity>(REQUEST_CODE_REGISTRATION,
                            RegistrationActivity.USER_NAME to getStringData(this, RegistrationActivity.USER_NAME),
                            RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                            RegistrationActivity.USER_MOBILE to getMobileNo(this),
                            RegistrationActivity.USER_TOKEN to token)
                    finish()
                }
            }
        }else {
            setData()
        }
    }

    override fun onResume() {
        super.onResume()

            if (getBooleanData(this, IS_REGISTERED)) {
                setData()
            }
    }

    private fun setData() {

        AccountManage.setOnClickListener {
            startActivity(intentFor<ManageAccountActivity>(RegistrationActivity.USER_NAME to getUserName(this@RTPActivity), RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                    RegistrationActivity.USER_MOBILE to getMobileNo(this@RTPActivity)).newTask().singleTop())
        }

        TransactionHistory.setOnClickListener {
            startActivity(intentFor<TxnHistoryActivity>(TxnHistoryActivity.PENDING_TXN_ONLY to false,ManageAccountActivity.USER_TOKEN to getUserToken(this)).newTask().singleTop())
           // startActivity(intentFor<TxnHistoryActivity>(ManageAccountActivity.USER_TOKEN to intent.getStringExtra(ManageAccountActivity.USER_TOKEN)).newTask().singleTop())

        }
    }

}