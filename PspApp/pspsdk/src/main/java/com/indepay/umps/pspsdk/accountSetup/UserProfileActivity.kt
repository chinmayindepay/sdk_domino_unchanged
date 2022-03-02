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

package com.indepay.umps.pspsdk.accountSetup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Keep
import android.util.Log
import android.view.View
import com.google.gson.GsonBuilder
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.activity_user_profile.txt_account_type
import kotlinx.android.synthetic.main.activity_user_profile.txt_bank_name
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import java.io.IOException

class UserProfileActivity : SdkBaseActivity() {

    private val REQ_CODE_ECOSYSTEM_BANK_FETCH = 10300

    private val client = OkHttpClient()
    var customerId:String = ""

    private lateinit var amount:String
    private lateinit var order_id:String
    private lateinit var remarks:String
    private lateinit var email:String
    private lateinit var merchName:String


    @Keep
    companion object : SdkCommonMembers(){
        @Keep
        const val AMOUNT = "amount"
        @Keep
        const val ORDER_ID = "order_id"
        @Keep
        const val EMAIL = "email"
        @Keep
        const val MERCHANT_NAME = "merchant_name"
        @Keep
        const val REMARKS = "remarks"

    }


    /**
     * This method is called by SDK itself, to fetch the Registration details and to
     *              Fetch Bank List.
     * @param CustomerProfileRequest
     * @return CustomerProfileResponse
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)


        amount = intent.getStringExtra(UserProfileActivity.AMOUNT).toString()
        order_id = intent.getStringExtra(UserProfileActivity.ORDER_ID).toString()
        email = intent.getStringExtra(UserProfileActivity.EMAIL).toString()
        remarks = intent.getStringExtra(UserProfileActivity.REMARKS).toString()
        merchName = intent.getStringExtra(UserProfileActivity.MERCHANT_NAME).toString()

        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.fetchCustomerDetailsAsync(
                            CustomerProfileRequest(
                                    accessToken = getAccessToken(this),
                                    custPSPId = getPspId(this),
                                    requestedLocale = getCurrentLocale(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    )
                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessProfileDataFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

        btn_profile_proceed.setOnClickListener {
            if (getBooleanData(this, IS_REGISTERED)) {
//                registerTara("https://dev.tara.app:9001/v1/tara/auth")
                finish()
            } else {
                startActivity(intentFor<ManageAccountActivity>().singleTop().clearTop())
            }
        }
    }


    private fun onSuccessProfileDataFetch(result: CommonResponse) {
        if (result is CustomerProfileResponse) {
            /*result.pa?.let {
                savePaymentAddress(this, it)
            }*/
            txt_mobile_no.text = getMobileNo(this)
            //result.paId?.let { savePaId(this, it) }
            if (result.mappedBankAccounts.isNullOrEmpty()) {
//                startActivityForResult(intentFor<EcosystemBanksActivity>().singleTop().clearTop(), REQ_CODE_ECOSYSTEM_BANK_FETCH)
                startActivity(intentFor<EcosystemBanksActivity>(EcosystemBanksActivity.AMOUNT to amount,
                    EcosystemBanksActivity.ORDER_ID to order_id,EcosystemBanksActivity.EMAIL to email,EcosystemBanksActivity.MERCHANT_NAME to merchName,
                    EcosystemBanksActivity.REMARKS to remarks).singleTop().clearTop())

            } else {

                result.mappedBankAccounts.forEach {
                    if (it.isDefault) {
                        txt_bank_name.text = it.bankName
                     //   txt_account_no.text = it.maskedAccountNumber
                        val lstValues: List<String> = it.maskedAccountNumber!!.split("#").map { it -> it.trim() }
                       txt_account_no.text = lstValues.get(0)

                        txt_account_type.text = it.accountType
                        txt_mobile_no.text = getMobileNo(this)
                        //savePaAccountId(this, it.paAccountId.orEmpty())
                        saveAccountTokenId(this, it.accountTokenId.orZero())

                    }
                    saveBooleanData(this, IS_REGISTERED, true)
                }
                setResult(Activity.RESULT_OK)
                element_container.visibility = View.VISIBLE
            }
        }
    }
    fun registerTara(url: String) {

        val json = "{\"mobileNumber\":\"+91${getMobileNo(this) }\",\"password\":\"null\",\"customerProfile\":{\"firstName\":\"null\",\"email\":\"null\",\"customerType\":\"Consumer\",\"registrationStatus\":\"rtp\"}}"

        Log.d("Sudhir","AddCreditCard json body::"+json)

        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Sudhir","registerTara failure::"+e)
            }
            override fun onResponse(call: Call, response: Response) //= println(response.body()?.string())
            {
                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
//                val Model= gson.fromJson(jsonData,WebApiModelResponse::class.java)
                Log.d("Sudhir", "Response Status::" + response.code)
                if (response.code == 500 || response.code == 200) {
                    getCustomerId(
                        "https://dev.tara.app:9001/v1/tara/crm/customer?mobile_number=91" + getMobileNo(
                            this@UserProfileActivity
                        )
                    )
                }
            }
        })
    }
    fun getCustomerId(url: String) {
        Log.d("Sudhir","getCustomerId url::"+url)
        val request = Request.Builder()
                .url(url)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response)  //= println(response.body()?.string())
            {
                //  println(response.body()?.string())
                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
                if (jsonData != null && jsonData != "") {
                    val Model = gson.fromJson(jsonData, CustomerIdResponse::class.java)
                    Log.d("Sudhir", "Customer Id ::" + Model.id)
                    customerId = Model.id!!
                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQ_CODE_ECOSYSTEM_BANK_FETCH && resultCode == Activity.RESULT_OK -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
            else -> {
                if (data != null) {
                    sendError(data.getStringExtra(ERROR_CODE).toString())
                }
                setResult(Activity.RESULT_CANCELED)
            }
        }
    }

    override fun onBackPressed() {
        val profileIntent = Intent()
        setResult(Activity.RESULT_OK, profileIntent)
        finish()
        super.onBackPressed()
    }
}
