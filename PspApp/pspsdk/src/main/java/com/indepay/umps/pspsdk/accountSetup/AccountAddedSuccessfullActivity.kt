package com.indepay.umps.pspsdk.accountSetup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Keep
import android.util.Log
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import kotlinx.android.synthetic.main.account_added_successfully.*
import kotlinx.android.synthetic.main.otp_layout.*

class AccountAddedSuccessfullActivity : SdkBaseActivity() {

    private var validateMessage: Boolean = false
    private var screencheckforAddAccount: Boolean = false

    @Keep
    companion object : SdkCommonMembers() {
        const val VALIDATE_MESSAGE = "VALIDATE_MESSAGE"
        const val SCREEN_FROM_CHECK = "SCREEN_FROM_CHECK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_added_successfully)

        screencheckforAddAccount = intent.getBooleanExtra(SCREEN_FROM_CHECK,false)

        if(screencheckforAddAccount ==false) {
            validateMessage = intent.getBooleanExtra(VALIDATE_MESSAGE, false)
        }else{

        }
Log.d("Sudhir","boolean::"+validateMessage)

        btn_gotit.setOnClickListener{

            Log.d("Sudhir","Got it Clicked")
            val intent = Intent()
            intent.putExtra(VALIDATE_MESSAGE, validateMessage)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

          }
    }