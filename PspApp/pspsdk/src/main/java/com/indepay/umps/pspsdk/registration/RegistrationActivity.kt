package com.indepay.umps.pspsdk.registration


import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.Keep
import android.support.annotation.RequiresApi
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.accountSetup.EcosystemBanksActivity
import com.indepay.umps.pspsdk.accountSetup.UserProfileActivity
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.utils.*
import com.indepay.umps.spl.registration.DeviceRegistrationActivity
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.activity_registration.llProgressBar
import org.jetbrains.anko.*


class RegistrationActivity : SdkBaseActivity() {

    private val REQ_CODE_REGISTRATION = 10100
    private val REQ_CODE_PROFILE_FETCH = 10200
    private val REQ_CODE_ECOSYSTEM_BANK_FETCH = 10300

    private lateinit var regnTxnId: String
    private var selectedSimId = 1
    private var selectedSimId2 = 2
    private lateinit var textedtmobile:Editable
    private lateinit var  sendsmsmobilenumber:String

    private lateinit var amount:String
    private lateinit var order_id:String
    private lateinit var remarks:String
    private lateinit var email:String
    private lateinit var merchName:String

    @Keep
    companion object : SdkCommonMembers() {
        @Keep
        const val APP_ID = "app_id"
        @Keep
        const val USER_NAME = "user_name"
        @Keep
        const val USER_MOBILE = "user_mobile"
        @Keep
        const val USER_PA = "user_pa"
        @Keep
        const val MERCHANT_KI = "merchant_ki"
        @Keep
        const val  _LOCALE = "locale"

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


    override fun onCreate(savedInstanceState: Bundle?) {
        callDialog = false
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        button_proceed.isEnabled = true
        if(!intent.getStringExtra(RegistrationActivity._LOCALE).isNullOrBlank()) {
            saveLocale(this, intent.getStringExtra(RegistrationActivity._LOCALE).toString())
        }
        saveAppName(this, intent.getStringExtra(RegistrationActivity.APP_ID).toString())
        saveUserName(this, intent.getStringExtra(RegistrationActivity.USER_NAME).toString())
        saveMobileNo(this, intent.getStringExtra(RegistrationActivity.USER_MOBILE).toString())
        sendsmsmobilenumber = et_mobile_number_verification.setText(getMobileNo(this)).toString()


        amount = ""+intent.getStringExtra(UserProfileActivity.AMOUNT)
        order_id = ""+intent.getStringExtra(UserProfileActivity.ORDER_ID)
        email = ""+intent.getStringExtra(UserProfileActivity.EMAIL)
        remarks = ""+intent.getStringExtra(UserProfileActivity.REMARKS)
        merchName = ""+intent.getStringExtra(UserProfileActivity.MERCHANT_NAME)


        if (getUserToken(this).isNullOrBlank()){
                fetchAppToken { token ->

                }
        }
        checkPermission()
            if (checkPermission()) {
                getSimInfo()
                callVerifyBySim()

            }

//        et_mobile_number_verification.text = textedtmobile
    }

    override fun onResume() {
        super.onResume()
        callDialog = false
        /*
        button_proceed.isEnabled = true
            if(getUserToken(this).isNullOrBlank()){
                fetchAppToken {token ->
                    saveLocale(this,intent.getStringExtra(RegistrationActivity._LOCALE).toString())
                    saveAppName(this, intent.getStringExtra(RegistrationActivity.APP_ID).toString())
                    saveUserName(this, intent.getStringExtra(RegistrationActivity.USER_NAME).toString())
                    saveMobileNo(this,intent.getStringExtra(RegistrationActivity.USER_MOBILE).toString())
                }
        }else {
            if (checkPermission()) {
                getSimInfo()
                callVerifyBySim()

            }
        } */
    }


    @SuppressLint("MissingPermission")
    private fun getSimInfo() {
        if (Build.VERSION.SDK_INT >= 22) {
            val simCardList: ArrayList<Int> = ArrayList()
          //  val subscriptionManager = SubscriptionManager.from(this)
            val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val manager=subscriptionManager.activeSubscriptionInfoList
//            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            val telNumber = tm.line1Number

            if(manager!=null) {
                val subscriptionInfoList: List<SubscriptionInfo> = manager
                for (i in subscriptionInfoList.indices) {
                    val subscriptionId = subscriptionInfoList.get(i).subscriptionId
                    simCardList.add(subscriptionId)
                   }

            }else{
                finish()
            }
//            if (Build.VERSION.SDK_INT >= 24) {
//                selectedSimId = SubscriptionManager.getDefaultSmsSubscriptionId()
//                if (simCardList.size > 0) {
//                    if (selectedSimId == 1) {
//                        rbSimOne.isChecked = true
//
//                    } else if (selectedSimId == 2) {
//                        rbSimTwo.isChecked = true
//                    } else {
//                        selectedSimId = simCardList[0]
//                        rbSimOne.isChecked = true
//                    }
//                }
//            } else {
//                if (simCardList.size > 0) {
//                    selectedSimId = simCardList[0]
//                }
//            }
//            getActiveStateOfSim()
//            if (simCardList.size > 1) {
//                rgSimSelection.visibility = View.VISIBLE
//
//            } else {
//                rgSimSelection.visibility = View.GONE
//            }
//            rgSimSelection.setOnCheckedChangeListener { group, checkedId ->
//                when (checkedId) {
//                    R.id.rbSimOne -> {
//                        selectedSimId = simCardList[0]
//                    }
//                    R.id.rbSimTwo -> {
//                        selectedSimId = simCardList[1]
//                    }
//                }
//            }
            rgSimSelection.visibility = View.GONE
        }


    }

    private fun getActiveStateOfSim() {

    }



    private fun callVerifyBySim() {
        if (!isValidData()) {
            sendError("A002")
        } else {
            saveAppName(this, intent.getStringExtra(APP_ID).toString())
            saveUserName(this, intent.getStringExtra(USER_NAME).toString())
            if(!(intent.getStringExtra(RegistrationActivity._LOCALE).isNullOrBlank())) {
                saveLocale(this, intent.getStringExtra(RegistrationActivity._LOCALE).toString())
            }
            saveMobileNo(this, intent.getStringExtra(USER_MOBILE).toString())

            if (TextUtils.isEmpty(getPspId(this))) {
                button_proceed.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        showAlert()
                    }
                }

            } else {
                alert(resources.getString(R.string.already_registered)) {
                    okButton {
                        fetchCustomerDetails()
                        //finish()

                    }
                    isCancelable = false
                }.show()
            }

        }
    }

    private fun fetchCustomerDetails() {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

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
                                    acquiringSource = AcquiringSource(appName = getAppName(this)
                                    )
                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessProfileDataFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }


    private fun onSuccessProfileDataFetch(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE

        if (result is CustomerProfileResponse) {
            if (result.mappedBankAccounts.isNullOrEmpty()) {
                saveBooleanData(this, IS_REGISTERED, true)
                startActivityForResult(intentFor<EcosystemBanksActivity>().singleTop().clearTop(),REQ_CODE_ECOSYSTEM_BANK_FETCH)
            }/* else {
                startActivity(intentFor<ManageAccountActivity>(ManageAccountActivity.USER_TOKEN to intent.getStringExtra(ManageAccountActivity.USER_TOKEN)).newTask().singleTop())
            }*/
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun showAlert() {
        alert(resources.getString(R.string.standard_charges), resources.getString(R.string.sms_send)) {
            okButton {
                button_proceed.isEnabled = false
                initRegistrationApi()

            }
            isCancelable = false
        }.show()

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun sendSms(regnTxnId: String) {
        Log.d("SelectedSIM","selectedSim::"+selectedSimId)
        Log.d("SelectedSIM","selectedSim::"+selectedSimId2)

//        if(et_mobile_number_verification.text.length>9) {
//            sendsmsmobilenumber = et_mobile_number_verification.setText(getMobileNo(this)).toString()
//        }
//        else{
            sendsmsmobilenumber = et_mobile_number_verification.text.toString()
//        }
        Log.d("SelectedSIM","Sudhir sendsmsmobilenumber::"+sendsmsmobilenumber)


        SmsManager.getSmsManagerForSubscriptionId(selectedSimId).sendTextMessage(BuildConfig.INDEPAY_MOBILE , null, "IndePay Verification"+"  "+ regnTxnId +"#"+ sendsmsmobilenumber, null, null); //use your phone number, message and pending intents
//        SmsManager.getSmsManagerForSubscriptionId(selectedSimId2).sendTextMessage(BuildConfig.INDEPAY_MOBILE, null, "IndePay Verification"+"  "+ regnTxnId +"#"+ sendsmsmobilenumber, null, null); //use your phone number, message and pending intents

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun initRegistrationApi() {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        callApi(
                accessToken = getUserToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.initRegistrationAsync(
                            RegistrationInitiateRequest(
                                    userName = getUserName(this),
                                    requestedLocale = getCurrentLocale(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    )
                            )
                    )
                },
                successCallback = { commonResponse -> onRegistrationInitSuccess(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun checkPermission(): Boolean {
        val permissionCheck = PermissionCheck(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck.checkSmsPermission())
                return true
        } else
            return true
        return false
    }

    private fun isValidData(): Boolean {
        intent?.getStringExtra(APP_ID)?.let {
            if (it.isNullOrBlank() || it.isNullOrEmpty()) {
                return false
            }
        }
        intent?.getStringExtra(USER_NAME)?.let {
            if (it.isNullOrEmpty() || it.isNullOrBlank()) {
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun onRegistrationInitSuccess(commonResponse: CommonResponse) {
        if (commonResponse is RegistrationInitiateResponse) {
            val txnId = commonResponse.transactionId
            val sessionKey = commonResponse.sessionKey
            if (sessionKey != null && txnId != null) {
                regnTxnId = txnId
                callSplForRegistration(txnId, sessionKey)
                sendSms(regnTxnId)
            }
        }
    }


    private fun callSplForRegistration(transactionId: String?, sessionKey: String?) {
//        hideDialog()
        Log.e("callSplForRegistration","entrypoint entered")
        llProgressBar.visibility = View.GONE

        Log.e("callSplForRegistration","after progress bar")

        startActivityForResult(intentFor<DeviceRegistrationActivity>(
                DeviceRegistrationActivity.APP_ID to getAppName(this),
                DeviceRegistrationActivity.PSP_ORG_ID to BuildConfig.PSP_ORG_ID,
                DeviceRegistrationActivity.SESSION_KEY to sessionKey,
                DeviceRegistrationActivity.MOBILE_NUMBER to getMobileNo(this),
                DeviceRegistrationActivity.REGN_TXN_ID to transactionId), REQ_CODE_REGISTRATION)
        Log.e("callSplForRegistration","post start")
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQ_CODE_REGISTRATION && resultCode == Activity.RESULT_OK -> {
//                showDialog()
                llProgressBar.visibility = View.VISIBLE

                callTxnTrackerApi(
                        trackerApi = { sdkApiService ->
                            sdkApiService.trackRegistrationAsync(
                                    createTxnTrackerRequest(regnTxnId)
                            )
                        },
                        successCallback = { result ->
//                            hideDialog()
                            llProgressBar.visibility = View.GONE

                            if (result is TrackerResponse) {
                                val mobileNo = result.mobileNumber
                                val pspId = result.pspId
                                if (pspId != null && mobileNo != null) {
                                    saveBooleanData(this, IS_REGISTERED, true)
                                    savePspId(this, pspId)
                                    saveMobileNo(this, mobileNo)
                                    saveAccessTokenExpireTime(this, 0) //Force expire token
                                    startActivityForResult(intentFor<UserProfileActivity>(
                                        UserProfileActivity.AMOUNT to amount,
                                        UserProfileActivity.ORDER_ID to order_id,
                                        UserProfileActivity.EMAIL to email,
                                        UserProfileActivity.MERCHANT_NAME to merchName,
                                        UserProfileActivity.REMARKS to remarks).singleTop().clearTop(), REQ_CODE_PROFILE_FETCH)
                                }
                            }
                        }
                )
            }
            requestCode == REQ_CODE_PROFILE_FETCH -> {
                if (getBooleanData(this, IS_REGISTERED)) {
                    val resultIntent = Intent()
                    resultIntent.putExtra(STATUS, resources.getString(R.string.success))
                    resultIntent.putExtra(USER_PA, getPaymentAddress(this))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    alert(resources.getString(R.string.registration_failed), resources.getString(R.string.registration_failed_title)) {
                        okButton {
                            val returnIntent = Intent()
                            if (data != null) {
                                returnIntent.putExtra(STATUS, resources.getString(R.string.failed))
                                returnIntent.putExtra(ERROR_CODE, data.getStringExtra(ERROR_CODE))
                                returnIntent.putExtra(ERROR_REASON, data.getStringExtra(ERROR_REASON))
                            }
                            setResult(Activity.RESULT_CANCELED, returnIntent)
                            finish()
                        }
                        isCancelable = false
                    }.show()
                }
            }

            requestCode == REQ_CODE_ECOSYSTEM_BANK_FETCH && resultCode == Activity.RESULT_OK -> {
                if (getBooleanData(this, IS_REGISTERED)) {
                    val resultIntent = Intent()
                    resultIntent.putExtra(STATUS, resources.getString(R.string.success))
                    resultIntent.putExtra(USER_PA, getPaymentAddress(this))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                } else {
                    alert(resources.getString(R.string.registration_failed), resources.getString(R.string.registration_failed_title)) {
                        okButton {
                            val returnIntent = Intent()
                            if (data != null) {
                                returnIntent.putExtra(STATUS, resources.getString(R.string.failed))
                                returnIntent.putExtra(ERROR_CODE, data.getStringExtra(ERROR_CODE))
                                returnIntent.putExtra(ERROR_REASON, data.getStringExtra(ERROR_REASON))
                            }
                            setResult(Activity.RESULT_CANCELED, returnIntent)
                            finish()
                        }
                        isCancelable = false
                    }.show()
                }
            }


            /*
            * REQ_CODE_ECOSYSTEM_BANK_FETCH
            * handle it
            * */
            else -> alert(resources.getString(R.string.something_wrong), resources.getString(R.string.registration_failed_title)) {
                okButton {
                    finish()
                }
                isCancelable = false
            }.show()
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            11 -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        getSimInfo()
                        callVerifyBySim()

                    } else {
                        finish()
                    }
                }
            }
        }
    }
}