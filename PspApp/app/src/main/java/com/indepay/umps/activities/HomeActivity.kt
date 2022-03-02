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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import com.indepay.umps.BuildConfig
import com.indepay.umps.R
import com.indepay.umps.fragments.BillPaymentFragment
import com.indepay.umps.fragments.WalletFragment
import com.indepay.umps.models.*
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.beneficiary.Contacts.BeneContactsActivity
import com.indepay.umps.pspsdk.billPayment.BillPaymentActivity
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.setting.RTPActivity
import com.indepay.umps.pspsdk.transaction.collectApprove.TxnCollectApproveActivity
import com.indepay.umps.pspsdk.transaction.history.TxnHistoryActivity
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import com.indepay.umps.utils.*
import com.indepay.umps.utils.Base64
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.jetbrains.anko.*
import retrofit2.HttpException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList


@RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
class HomeActivity : BaseActivity(), View.OnClickListener, BillPaymentFragment.OnFragmentInteractionListener,
        WalletFragment.OnWalletFragmentInteractionListener {

    private val MERCHANT_ID = BuildConfig.MERCHANT_ID
    private val KEY = BuildConfig.MERCHANT_KEY
    private val KI = BuildConfig.MERCHANT_KI
    //private val INITIATOR_PA = BuildConfig.INITIATOR_PA
    //private val INITIATOR_PA_WALLET = "my-wallet@ayopop"


    //private val MERCHANT_ID_WALLET = "my-wallet"
    //Passed merchant ID rather than merchant wallet ID, to work it through
    private val MERCHANT_ID_WALLET = BuildConfig.MERCHANT_ID

    private val REQUEST_CODE_REGISTRATION = 10100
    private val reqCode = 700
    private val REQUEST_CODE_WALLET_TOPUP = 800
    private val IS_REGISTERED_KEY = "is_registered"
    private val PAYMENT_ADDRESS = "payment_address"
    private val REQ_TYPE_COLLECT = "COLLECT"
    private val TAG_BILL_PAYMENT_FRAGMENT = "BILL_PAYMENT_FRAGMENT"
    private val TAG_BILL_WALLET_FRAGMENT = "TAG_BILL_WALLET_FRAGMENT"
    private lateinit var toolbar: Toolbar
    private val TAG_WALLET_FRAGMENT = "tag_wallet_fragment"
    private val sSMSManagerIntentSENT = "package.DeliveryReport.SMS_SENT";
    var order_id:Int =0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
      /*  if (BuildConfig.ENABLE_CRASH_REPORT) {
            Fabric.with(this, Crashlytics())
        }*/

        toolbar = findViewById(R.id.toolbar_home)
        toolbar.title = ""
        setSupportActionBar(toolbar)

        img_bills.setOnClickListener(this)

        img_pay.setOnClickListener(this)
        img_request.setOnClickListener(this)
        img_request_2.setOnClickListener(this)
        img_txn_history_2.setOnClickListener(this)
        img_pulsa_2.setOnClickListener(this)
        img_pulsa_1.setOnClickListener(this)

        /*img_cesc.setOnClickListener(this)
        img_alliance.setOnClickListener(this)
        img_bsnl.setOnClickListener(this)
        img_airtel.setOnClickListener(this)
        img_vodafone.setOnClickListener(this)
        img_dishtv.setOnClickListener(this)*/

        llProfile.setOnClickListener(this)
        customer_name.text = getStringData(this, com.indepay.umps.activities.RegistrationActivity.USER_NAME)

    }

    override fun onClick(v: View?) {
            when (v?.id) {
                img_bills.id ->{
                    startActivity(intentFor<BillPaymentActivity>().newTask().singleTop())


                }
                img_pulsa_1.id ->{

                }
                img_pay.id -> {
                    fetchAppToken { token ->
                        startActivity(intentFor<PayCollectActivity>(SdkBaseActivity.COMING_FROM to BeneContactsActivity.TXN_TYPE_PAY, BeneContactsActivity.USER_TOKEN to token).newTask().singleTop())
                       // startActivity(intentFor<BeneContactsActivity>(BeneContactsActivity.TRANSACTION_TYPE to BeneContactsActivity.TXN_TYPE_PAY, BeneContactsActivity.USER_TOKEN to token).newTask().singleTop())
                    }
                }
                img_request.id -> {
                     fetchAppToken { token ->
                         startActivity(intentFor<PayCollectActivity>(SdkBaseActivity.COMING_FROM to BeneContactsActivity.TXN_TYPE_COLLECT, BeneContactsActivity.USER_TOKEN to token).newTask().singleTop())
                        // startActivity(intentFor<BeneContactsActivity>(BeneContactsActivity.TRANSACTION_TYPE to BeneContactsActivity.TXN_TYPE_COLLECT, BeneContactsActivity.USER_TOKEN to token).newTask().singleTop())
                     }
                }
                img_txn_history_2.id -> {
                    //Crashlytics.getInstance().crash()
                    fetchAppToken { token ->
                        startActivity(intentFor<TxnHistoryActivity>(TxnHistoryActivity.PENDING_TXN_ONLY to false).newTask().singleTop())
                    }
                }
                img_pulsa_2.id -> {
                    /*fetchAppToken { token ->
                      //  fetchMerchantToken(amount = "500", remarks = "asdfasfd", merchantId = MERCHANT_ID, initiatorPa = INITIATOR_PA, reqCode = REQUEST_CODE_MERCHANT_PAYMENT, merchName = "anmol")
                        initiateBillPayment(R.drawable.gas_negara)
                    }*/
                 //   fetchAppToken { token ->
                    order_id = Math.abs(order_id+ UUID.randomUUID().hashCode())
                        callTransaction("500", "", "sudhir"+order_id+"@gmail.com","GAS", order_id.toString())

//                    }
                }
                img_request_2.id -> {
                    fetchAppToken { token ->
                        initiateWalletTopup()
                    }
                }
                llProfile.id -> {

                    /*fetchAppToken { token ->
                        //startActivity(intentFor<TxnHistoryActivity>(TxnHistoryActivity.PENDING_TXN_ONLY to false, TxnHistoryActivity.USER_TOKEN to token).newTask().singleTop())
                        startActivity(intentFor<ProfileActivity>(ProfileActivity.USER_TOKEN to token).newTask().singleTop())
                    }*/
                    val density = when (resources.displayMetrics.density) {
                        0.75F -> "LDPI"
                        1.0F -> "MDPI"
                        1.5F -> "HDPI"
                        2.0F -> "XHDPI"
                        3.0F -> "XXHDPI"
                        4.0F -> "XXXHDPI"
                        else -> "NODPI"
                    }
                    Snackbar.make(home_container, "App Version: ${BuildConfig.VERSION_NAME} \nDevice Density: $density", Snackbar.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.notification -> {
                if(!getBooleanData(this, IS_REGISTERED_KEY)) {
                    //fetchAppToken {token ->
                        showRegistrationDialog()
                    //}

                } else
               // fetchAppToken {token ->
                    startActivity(intentFor<TxnHistoryActivity>(TxnHistoryActivity.PENDING_TXN_ONLY to true).newTask())
              //  }
                true
            }
            R.id.settings -> {

                startActivity(intentFor<RTPActivity>(RegistrationActivity.USER_NAME to getStringData(this@HomeActivity,
                        com.indepay.umps.activities.RegistrationActivity.USER_NAME),
                        RegistrationActivity.APP_ID to BuildConfig.APP_NAME, com.indepay.umps.activities.RegistrationActivity._LOCALE to "en",
                        RegistrationActivity.USER_MOBILE to getStringData(this,com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER)))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showRegistrationDialog() {
                startActivityForResult<RegistrationActivity>(REQUEST_CODE_REGISTRATION,
                        RegistrationActivity.USER_NAME to getStringData(this@HomeActivity, com.indepay.umps.activities.RegistrationActivity.USER_NAME),
                        RegistrationActivity.APP_ID to BuildConfig.APP_NAME,com.indepay.umps.activities.RegistrationActivity._LOCALE to "en",
                        RegistrationActivity.USER_MOBILE to getStringData(this@HomeActivity, com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER ))

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.e("HomeActivity","Result"+" "+requestCode)
        when {
            requestCode == REQUEST_CODE_REGISTRATION && resultCode == Activity.RESULT_OK && data != null -> {
                saveBooleanData(this, IS_REGISTERED_KEY, true)
                saveStringData(this, PAYMENT_ADDRESS, data.getStringExtra(RegistrationActivity.USER_PA))
            }
            requestCode == REQUEST_CODE_WALLET_TOPUP && resultCode == Activity.RESULT_OK && data != null -> {
                val prvBal: Double = if (getWalletBalance(this).isNullOrEmpty()) {
                    "0.00".toDouble()
                } else {
                    getWalletBalance(this).toDouble()
                }
                val successBal = data.getStringExtra(TxnCollectApproveActivity.WALLET_BAL)?.toDouble()
                val total = prvBal + successBal!!
                saveWalletBalance(this, total.toString())
                //refreshFragment()
            }

            requestCode == 1002  && resultCode == Activity.RESULT_OK && data != null -> {

                val successpaymenttxnid = data.getStringExtra(PaymentAccountActivity.TXN_ID).toString()
                val paymentstatus = data.getBooleanExtra(PaymentAccountActivity.PAYMENT_STATUS,false)
                Log.d("success quote","successpaymenttxnid::"+successpaymenttxnid)
                Log.d("success quote","paymentstatus::"+paymentstatus)
                Log.d("success quote","successpayment::"+requestCode)
            }
        }
    }

    private fun fetchAppToken(actionCallback: (token: String) -> Unit) = uiScope.launch {
        if (((getAccessTokenExpireTime(this@HomeActivity) - System.currentTimeMillis())+30000) < DateUtils.MINUTE_IN_MILLIS) {
//            showDialog()
            llProgressBar.visibility = View.VISIBLE
            val result = withContext(bgDispatcher) {
                loadAppTokenRequestData()
            }

//            hideDialog()
            llProgressBar.visibility = View.GONE

            result?.let { respStr ->
                val tokenRespStr = String(PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(respStr.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(KEY)), StandardCharsets.UTF_8)
                val accessToken = Gson().fromJson(tokenRespStr, AccessToken::class.java)
                accessToken?.let { tkn ->
                    saveAccessToken(this@HomeActivity, tkn.token)
                    saveLoginToken(this@HomeActivity, tkn.token)
                    saveAccessTokenIssueTime(this@HomeActivity, tkn.issuedAtMillis)
                    saveAccessTokenExpireTime(this@HomeActivity, tkn.validTillMillis)
                    actionCallback(tkn.token)
                }

             //   Log.d("access token","Sudhir access::"+accessToken)
            }
        } else {
            actionCallback(getAccessToken(this@HomeActivity))
//            Log.d("Login","Sudhir access token response else::"+ getAccessToken(this@HomeActivity))
//            saveLoginToken(this@HomeActivity, getAccessToken(this@HomeActivity))

        }
    }

    private fun fetchMerchantToken(amount: String, remarks: String?, merchantId: String, reqCode: Int, merchName: String)
            = uiScope.launch {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        val result = withContext(bgDispatcher) {
            loadMerchantTokenRequestData(merchantId)
        }

//        hideDialog()
        llProgressBar.visibility = View.GONE

        Log.e("<<Response>>",""+result)

        result?.let { respStr ->
            val tokenRespStr = String(PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(respStr.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(KEY)), StandardCharsets.UTF_8)
            val accessToken = Gson().fromJson(tokenRespStr, AccessToken::class.java)
            accessToken?.let { tkn ->
                var placeholderTxt = "Paid by ${getStringData(this@HomeActivity, com.indepay.umps.activities.RegistrationActivity.USER_NAME)}" //Bill payment by
                if (remarks.isNullOrBlank()) {
                    callMerchantCollectApi(amount = amount, remarks = placeholderTxt, accessToken = tkn.token, merchantId = merchantId, reqCode = reqCode, merchName = merchName)
                } else {
                    callMerchantCollectApi(amount = amount, remarks = remarks, accessToken = tkn.token, merchantId = merchantId, reqCode = reqCode, merchName = merchName)
                }
            }
        //    Log.d("Login","Sudhir access token merchnat response::"+accessToken)

        }


    }

    //Added Merchant Name to saparate the other transactions to merchant transactions
    private fun callMerchantCollectApi(amount: String, remarks: String?, accessToken: String, merchantId: String, reqCode: Int, merchName: String) = uiScope.launch {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        val result = withContext(bgDispatcher) {
            loadMerchantCollectData(amount = amount, remarks = remarks, token = accessToken, merchantId = merchantId, merchName = merchName)
        }

//        hideDialog()
        llProgressBar.visibility = View.GONE

        if (result is Result.Success) {
            if (result.data.success) {
                fetchAppToken { token ->
                    startActivityForResult(intentFor<TxnCollectApproveActivity>(TxnCollectApproveActivity.MRCH_TXN_ID to result.data.transactionId,
                            TxnCollectApproveActivity.USER_TOKEN to token), reqCode)
                }

            } else {
                val errCode = result.data.errorCode.orEmpty()
                val errReason = result.data.errorReason.orEmpty()

                alert(errReason, errCode) {
                    okButton {}
                }.show()
            }
        } else if (result is Error) {
            alert(resources.getString(R.string.server_error), result.message) {
                okButton {}
            }.show()
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
                    request = RequestBody.create(("text/plaen").toMediaType(), requestString)
            )
            val result = response.await()
            Log.e("<<<AppToken>>>", Gson().toJson(response))
            if (result.isSuccessful) {
                result.body()
            } else {
                null
            }
        } catch (e: Throwable) {
            null
        }
    }


    private suspend fun loadMerchantTokenRequestData(merchantId: String): String? {
        return try {
            Log.e("<<Print log>>",""+merchantId)

            val userToken = UserToken(
                    merchantId = merchantId
            )

            val reqJson = Gson().toJson(userToken)
            val requestString = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(reqJson.toByteArray(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)

            val sslData = getBaseAppSslConfig(this)
            val apiService = BaseAppApiService.create(sslData)

            Log.e("request",reqJson+" "+requestString + " "+sslData+" "+apiService)
            Log.e("requestString"," "+requestString)

            val response = apiService.fetchMerchantTokenAsync(
                    ki = KI,
                    request = RequestBody.create(("text/plaen").toMediaType(), requestString)
            )

            val result = response.await()

            Log.e("<<<ResponseApi>>>",""+result.body())

            if (result.isSuccessful) {
                result.body()
            } else {
                Log.e("<<<ResponseFailure>>>", Gson().toJson(result))
                null
            }
        } catch (e: Throwable) {
            Log.e("EXP",""+e)
            null
        }
    }


    private suspend fun loadMerchantCollectData(amount: String, remarks: String?, token: String, merchantId: String, merchName: String): Result<MerchantCollectResponse> {
        return try {

            val payees = ArrayList<Payee>()
            payees.add(Payee(
                    amount = amount
            ))
            val merchantCollectRequest = MerchantCollectRequest(
                    merchantId = merchantId,
                    accessToken = token,
                    type = REQ_TYPE_COLLECT,
                    //initiatorPA = initiatorPa,
                    payer = Payer(
                            //pa = getStringData(this, com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER) + PA_HANDLE
                            mobile = getStringData(this, com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER),
                            appId = BuildConfig.APP_NAME
                    ),
                    payees = payees,
                    remarks = remarks,
                    subMerchantName = merchName // Merchant1
            )

            val sslData = getBaseAppSslConfig(this)
            val apiService = BaseAppApiService.create(sslData)
            val response = apiService.initiateMerchantTransactionAsync(merchantCollectRequest)
            val result = response.await()
            Result.Success(result)

        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    private fun initiateBillPayment(logo: Int) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.home_container, BillPaymentFragment.newInstance(logo))
                .addToBackStack(TAG_BILL_PAYMENT_FRAGMENT)
                .commit()
    }

    private fun initiateWalletTopup() {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.home_container, WalletFragment.newInstance(), TAG_WALLET_FRAGMENT)
                .addToBackStack(TAG_BILL_WALLET_FRAGMENT)
                .commit()
    }

    override fun onFragmentInteraction(amount: String, remarks: String?, merchName: String) {
        //fetchMerchantToken(amount = amount, remarks = remarks, merchantId = MERCHANT_ID, reqCode = REQUEST_CODE_MERCHANT_PAYMENT, merchName = merchName)
        startActivityForResult(intentFor<TxnCollectApproveActivity>(TxnCollectApproveActivity.AMOUNT to amount,
                TxnCollectApproveActivity.REMARKS to remarks,TxnCollectApproveActivity.MERCHANT_NAME to merchName,
                RegistrationActivity.USER_NAME to getStringData(this@HomeActivity, com.indepay.umps.activities.RegistrationActivity.USER_NAME),
                RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                RegistrationActivity.USER_MOBILE to getStringData(this,com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER)), reqCode)
        supportFragmentManager.popBackStack()
    }

    override fun onWalletFragmentInteraction(amount: String, remarks: String?, merchName: String) {
        fetchMerchantToken(amount = amount, remarks = remarks, merchantId = MERCHANT_ID_WALLET, reqCode = REQUEST_CODE_WALLET_TOPUP, merchName = merchName)
        supportFragmentManager.popBackStack()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }

    }

    private fun refreshFragment() {
        val frg = supportFragmentManager.findFragmentByTag(TAG_WALLET_FRAGMENT)
        val ft = supportFragmentManager.beginTransaction()
        ft.detach(frg)
        ft.attach(frg)
        ft.commit()
    }

        fun callTransaction(amount: String, remarks: String?,email: String, merchName: String,order_id:String) {
            startActivityForResult(intentFor<PaymentAccountActivity>(PaymentAccountActivity.AMOUNT to amount,
                    PaymentAccountActivity.EMAIL to email,
                    PaymentAccountActivity.ORDER_ID to order_id,
                    PaymentAccountActivity.REMARKS to remarks,PaymentAccountActivity.MERCHANT_NAME to merchName,
                    RegistrationActivity.USER_NAME to getStringData(this@HomeActivity, com.indepay.umps.activities.RegistrationActivity.USER_NAME),
                    RegistrationActivity.APP_ID to BuildConfig.APP_NAME,com.indepay.umps.activities.RegistrationActivity._LOCALE to "en",
                    RegistrationActivity.USER_MOBILE to getStringData(this,com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER)),   1002)

        }

}
