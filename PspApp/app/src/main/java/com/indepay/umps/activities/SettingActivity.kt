package com.indepay.umps.activities

import android.os.Bundle
import com.indepay.umps.BuildConfig
import com.indepay.umps.R

import com.indepay.umps.pspsdk.accountSetup.ManageAccountActivity
import com.indepay.umps.pspsdk.setting.AddBeneficiaryActivity
import com.indepay.umps.utils.getStringData

import kotlinx.android.synthetic.main.setting_act.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.singleTop


class SettingActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_act)
        setData()
    }

    private fun setData() {

        Account.setOnClickListener {
            startActivity(intentFor<ManageAccountActivity>(com.indepay.umps.pspsdk.registration.RegistrationActivity.USER_NAME to getStringData(this@SettingActivity, RegistrationActivity.USER_NAME),
                    com.indepay.umps.pspsdk.registration.RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                    com.indepay.umps.pspsdk.registration.RegistrationActivity.USER_MOBILE to getStringData(this@SettingActivity, RegistrationActivity.MOBILE_NUMBER )).newTask().singleTop())
        }

        AddBeneficiary.setOnClickListener {
            startActivity(intentFor<AddBeneficiaryActivity>(ManageAccountActivity.USER_TOKEN to intent.getStringExtra(ManageAccountActivity.USER_TOKEN)).newTask().singleTop())

        }
    }

}