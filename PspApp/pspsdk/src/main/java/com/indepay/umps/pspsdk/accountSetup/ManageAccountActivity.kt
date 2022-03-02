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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.Keep
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.google.gson.GsonBuilder
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.ManageCreditCardListAdapter
import com.indepay.umps.pspsdk.adapter.MappedAccountListAdapter
import com.indepay.umps.pspsdk.adapter.SpaceItemDecoration
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnAccountListInteractionListner
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_manage_account.*
import kotlinx.android.synthetic.main.activity_manage_account.add_new_cardview
import kotlinx.android.synthetic.main.activity_manage_account.back_arrowimage
import kotlinx.android.synthetic.main.activity_manage_account.credit_card_acc_list_container
import kotlinx.android.synthetic.main.activity_manage_account.img_bank_logo
import kotlinx.android.synthetic.main.activity_manage_account.llProgressBar
import kotlinx.android.synthetic.main.activity_manage_account.my_default_account
import kotlinx.android.synthetic.main.activity_manage_account.rv_acc_list_container
import kotlinx.android.synthetic.main.activity_manage_account.selected_card_imge
import kotlinx.android.synthetic.main.activity_manage_account.txt_bank_name
import kotlinx.android.synthetic.main.activity_manage_account.txt_debitcard_no
import kotlinx.android.synthetic.main.activity_manage_account.txt_other_bank_accounts
import kotlinx.android.synthetic.main.activity_manage_account.txt_single_credit_card_add_New
import kotlinx.android.synthetic.main.activity_payment_account.*
import okhttp3.*
import okhttp3.MediaType.Companion.parse
import okhttp3.MediaType.Companion.toMediaType
import org.jetbrains.anko.*
import java.io.IOException
import java.net.URL


@Keep
class ManageAccountActivity : SdkBaseActivity(), OnAccountListInteractionListner {

    private lateinit var balEnqTxnId: String
    private lateinit var setResetMpinTxnId: String
    private val REQ_CODE_BALANCE_ENQUIRY = 10500
    private val REQ_CODE_SET_RESET_MPIN = 10600
    private val mappedAccounts: ArrayList<MappedAccount> = ArrayList()

    private val creditcardmappedAccounts: ArrayList<CreditCardMappedAccount> = ArrayList()

    private var defaultselectedAccount: MappedAccount? = null
    private var selectedAccount: MappedAccount? = null
    private var selectedCreditcardAccount: CreditCardMappedAccount? = null
    private var defaultaccounttokenid: Long = 0

    private var defaultbool:Boolean = true
    private lateinit var txnId: String

    private var client = OkHttpClient()
    var customerId:String = ""
    private  var mcPaymentsCreditcardsdataUrl: String = "https://dev.tara.app/v0.1/tara/payments/mcpayment/detail/card/"
    private val REQ_CODE_TRANSACTION = 1005




    @Keep
    companion object : SdkCommonMembers(){

    }


    /**
     * This method is called by Base App to see mapped account and to Manage Account
     *              Like (Set/Reset Mpin, Balance Enquiry, Add Bank Account)
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)
        llProgressBar.visibility = View.VISIBLE

//        showDialog()
                fetchAppToken {token->
                val layoutManager = android.support.v7.widget.GridLayoutManager(this,2)
                val itemDecoration = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)

                    rv_acc_list_container.layoutManager = layoutManager
                    rv_acc_list_container.addItemDecoration( SpaceItemDecoration(10))


                    val picasso = getPicassoInstance(this, getPspSslConfig(this))
                val accountListAdapter = MappedAccountListAdapter(
                        accountList = mappedAccounts,
                        creditcardaccountList = creditcardmappedAccounts,
                        listener = this,
                        picassoInstance = picasso,
                        accessToken = getAccessToken(this),
                        appName = getAppName(this),
                        custPspId = getPspId(this))
                rv_acc_list_container.adapter = accountListAdapter


                    val layoutManagercreditcard = android.support.v7.widget.GridLayoutManager(this, 2)
                    val itemDecorationcreditcard = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)

                    credit_card_acc_list_container.layoutManager = layoutManagercreditcard
//        credit_card_acc_list_container.addItemDecoration(itemDecorationcreditcard)
                    credit_card_acc_list_container.addItemDecoration( SpaceItemDecoration(10))


                    val creditcardaccountListAdapter = ManageCreditCardListAdapter(
                            creditcardaccountList = creditcardmappedAccounts,
                            accountList = mappedAccounts,
                            listener = this)
                    credit_card_acc_list_container.adapter = creditcardaccountListAdapter




                }



        button_delete.setOnClickListener {
//            Log.d("Sudhir","Button Clicked::")
            if (selectedCreditcardAccount != null) {
         //       Log.d("Sudhir","Button Clickedif::")
                if (selectedCreditcardAccount!!.isSelected == true && defaultbool == false) {
          //          Log.d("Sudhir","Button Clicked if if ::")
           //         Log.d("Sudhir", "credit card selected ::" + selectedCreditcardAccount!!.callbackResponsePk!!.mcPaymentCardId)
                    DeleteCreditCard("https://dev.tara.app/v0.1/tara/payments/mcpayment/delete/token/"+selectedCreditcardAccount!!.token)


//                    mcPaymentPostRequestAPI("https://dev.tara.app:9005/v0.1/mcpayment/request/payment", selectedCreditcardAccount!!)
//                    DeleteCreditCard("")
                } else {

                    if (selectedAccount!!.isSelected == true ) {
                        if (getBooleanData(this, IS_REGISTERED)) {
                            llProgressBar.visibility = View.VISIBLE
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                                initiateCollectRequest()
                  //          Log.d("Sudhir","Button Clicked if else if ::")

                            DeleteAccountApi()
                        }
                    }
                }
            }else {
           //     Log.d("Sudhir","Button Clicked else::")
                if (selectedAccount!!.isSelected == true || defaultbool == true) {
          //          Log.d("Sudhir","Button Clicked else if::")
          //          Log.d("Sudhir", "DELETE::" + selectedAccount!!.accountTokenId)
                    DeleteAccountApi()
                }
            }
        }

        my_default_account.setOnClickListener {
            Log.d("Sudhir","Default account clicked")
            defaultbool = true

            my_default_account.setForeground(getDrawable(R.drawable.selector))
            my_default_account.useCompatPadding = false
            selected_card_imge.visibility = View.VISIBLE

            for (i in 0..mappedAccounts.size-1){
                mappedAccounts.get(i).isSelected = false
            }
            for (i in 0..creditcardmappedAccounts.size-1){
                creditcardmappedAccounts.get(i).isSelected = false
            }
            rv_acc_list_container.adapter.notifyDataSetChanged()
            credit_card_acc_list_container.adapter.notifyDataSetChanged()
        }
        add_new_cardview.setOnClickListener {
            startActivity(intentFor<EcosystemBanksActivity>().singleTop().clearTop())

        }
        txt_single_credit_card_add_New.setOnClickListener {
            AddCreditCard("https://dev.tara.app/v0.1/tara/payments/mcpayment/request/tokenization")

        }
//        txt_credit_card_add_New.setOnClickListener {
//
//            Log.d("Sudhir","credit card Add New clicked")
//
//            AddCreditCard("http://107.20.4.43:9005/v0.1/mcpayment/request/tokenization")
//
//        }
        back_arrowimage.setOnClickListener {
            Log.d("Sudhir","Back Arrow clicked")
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if(getBooleanData(this, IS_REGISTERED)){

            val layoutManager = android.support.v7.widget.GridLayoutManager(this,2)
            val itemDecoration = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)
            rv_acc_list_container.layoutManager = layoutManager
            rv_acc_list_container.addItemDecoration( SpaceItemDecoration(10))


            val picasso = getPicassoInstance(this, getPspSslConfig(this))
            val accountListAdapter = MappedAccountListAdapter(
                    accountList = mappedAccounts,
                    creditcardaccountList = creditcardmappedAccounts,
                    listener = this,
                    picassoInstance = picasso,
                    accessToken = getAccessToken(this),
                    appName = getAppName(this),
                    custPspId = getPspId(this))
            rv_acc_list_container.adapter = accountListAdapter
//            showDialog()
            llProgressBar.visibility = View.VISIBLE

                defaultbool = true
                my_default_account.setForeground(getDrawable(R.drawable.selector))
                my_default_account.useCompatPadding = false
                selected_card_imge.visibility = View.VISIBLE

                registerTara("https://dev.tara.app/v0.1/tara/auth/credential")

            fetchAppToken {token->
                fetchAddedBankAcList()
            }
        }

        //fetchAddedBankAcList()
    }

    private fun onSuccessMappedAccFetch(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE

        if (result is CustomerProfileResponse && result.mappedBankAccounts != null) {
            mappedAccounts.clear()
            selectedAccount = MappedAccount()
            selectedAccount!!.addnew = "ADD New"
            mappedAccounts.add(0, selectedAccount!!)

            mappedAccounts.addAll(result.mappedBankAccounts)
            Log.d("Sudhir", "mapped Account size::" + mappedAccounts.size)
            val picassoInstance = getPicassoInstance(this, getPspSslConfig(this))
            if (mappedAccounts.isNotEmpty()) {
                for (i in 1..mappedAccounts.size) {
                    if (i < mappedAccounts.size) {
                        if (mappedAccounts.get(i).isDefault == true) {
//                            rdo_set_default_my.isChecked=true
                            defaultselectedAccount = mappedAccounts.get(i)
                            defaultaccounttokenid = mappedAccounts.get(i).accountTokenId!!
                            val lstValues: List<String> = mappedAccounts.get(i).maskedAccountNumber!!.split("#").map { it -> it.trim() }
                            txt_debitcard_no.text = lstValues.get(1)
//                            txt_expiry_no.text = lstValues.get(2)
                            picassoInstance.load(getPspBaseUrlForBankLogo(mappedAccounts.get(i).bic.orEmpty(), getAccessToken(this), getAppName(this), getPspId(this))).placeholder(R.drawable.ic_bank_place_holder).into(img_bank_logo)
                            txt_bank_name.text = mappedAccounts.get(i).bankName
//                            txt_account_type.text = mappedAccounts.get(i).accountType
                            Log.d("Sudhir", "text::" + txt_debitcard_no.text)
                            mappedAccounts.removeAt(i)
                        }
                    }
                }
                if (mappedAccounts.size < 1) {
                    my_default_account.visibility = View.GONE
                    add_new_cardview.visibility = View.VISIBLE
                    txt_other_bank_accounts.visibility = View.GONE
                    rv_acc_list_container.visibility = View.GONE
                } else {
                    my_default_account.visibility = View.VISIBLE
                    add_new_cardview.visibility = View.GONE
                    txt_other_bank_accounts.visibility = View.VISIBLE
                    rv_acc_list_container.visibility = View.VISIBLE
                }

                rv_acc_list_container.adapter.notifyDataSetChanged()

            } else {
                alert(message = resources.getString(R.string.no_acc_with_mobile_number_add), title = resources.getString(R.string.no_acc_title)) {
                    okButton {
                        startActivity(intentFor<EcosystemBanksActivity>().singleTop().clearTop())
                    }
                    cancelButton {
                        finish()
                    }
                    isCancelable = false
                }.show()
            }
        }
    }

        private fun callSetDefaultAccountApi(mappedAccount: MappedAccount) {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.setDefaultAccountAsync(
                            SetDefaultAccountRequest(
                                    bic = mappedAccount.bic.orEmpty(),
                                    //paAccountId = mappedAccount.paAccountId.orEmpty(),
                                    //paId = mappedAccount.paId.orEmpty(),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    accountTokenId = mappedAccount.accountTokenId.orZero(),
                                    custPSPId = getPspId(this),
                                    requestedLocale = getCurrentLocale(this)

                            )
                    )
                },
                successCallback = {
                    //savePaAccountId(this, mappedAccount.paAccountId.toString())
                    saveAccountTokenId(this, mappedAccount.accountTokenId.orZero())
                    fetchAddedBankAcList()
                },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }

    private fun fetchAddedBankAcList() {
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
                successCallback = { commonResponse -> onSuccessMappedAccFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == REQ_CODE_BALANCE_ENQUIRY && resultCode == Activity.RESULT_OK -> {
//                showDialog()
                llProgressBar.visibility = View.VISIBLE

                callTxnTrackerApi(
                        trackerApi = { sdkApiService ->
                            sdkApiService.trackBalanceEnquiryAsync(
                                    createTxnTrackerRequest(balEnqTxnId)
                            )
                        },
                        successCallback = { result ->
//                            hideDialog()
                            llProgressBar.visibility = View.GONE

                            if (result is TrackerResponse) {
                                alert(resources.getString(R.string.ledger)+": ${result.ledgerBalance}\n\n"+resources.getString(R.string.available)+": ${result.availableBalance}", "Account Balance") {
                                    okButton { finish() }
                                    isCancelable = false
                                }.show()
                            }
                        }
                )
            }
            requestCode == REQ_CODE_SET_RESET_MPIN && resultCode == Activity.RESULT_OK -> {
//                showDialog()
                llProgressBar.visibility = View.VISIBLE

                callTxnTrackerApi(
                        trackerApi = { sdkApiService ->
                            sdkApiService.trackResetCredRequestAsync(
                                    createTxnTrackerRequest(setResetMpinTxnId)
                            )
                        },
                        successCallback = { result ->
                            if (result is TrackerResponse) {
//                                hideDialog()
                                llProgressBar.visibility = View.GONE

                                alert(resources.getString(R.string.mpin_succ), resources.getString(R.string.trn_cmplt)) {
                                    okButton { finish() }
                                    isCancelable = false
                                }.show()
                            }
                        }
                )
            }
        }
    }


//    override fun onSetDefaultAccount(mappedAccount: MappedAccount) {
//        callSetDefaultAccountApi(mappedAccount)
//    }


    @SuppressLint("ResourceAsColor")
    override fun onSetDefaultAccount(mappedAccount: MappedAccount) {
        if(defaultbool == true) {
            selected_card_imge.visibility = View.GONE
            my_default_account.setForeground(null)
            my_default_account.useCompatPadding = true

            defaultbool = false
        }
        if(mappedAccount.addnew=="+ ADD NEW"){
            Log.d("Sudhir","Add New")
            startActivity(intentFor<EcosystemBanksActivity>().singleTop().clearTop())
        }
        selectedAccount = mappedAccount

        credit_card_acc_list_container.adapter.notifyDataSetChanged()

        rv_acc_list_container.adapter.notifyDataSetChanged()

        Log.d("Sudhir","mapedAccount"+ selectedAccount!!.maskedAccountNumber)
    }

    override fun onSetDefaultAccount(position: Int) {
        Log.d("Sudhir","position::"+ position)
        my_default_account.setForeground(null)
        my_default_account.useCompatPadding = true

        credit_card_acc_list_container.adapter.notifyDataSetChanged()

        rv_acc_list_container.adapter.notifyDataSetChanged()
        Log.d("Sudhir","Add New::")
        startActivity(intentFor<EcosystemBanksActivity>().singleTop().clearTop())

    }

    override fun onSetCreditCardAccount(creditcardmappedAccount: CreditCardMappedAccount) {
        if(defaultbool == true) {
            selected_card_imge.visibility = View.GONE
            my_default_account.setForeground(null)
            my_default_account.useCompatPadding = true

            defaultbool = false
        }
        selectedCreditcardAccount = creditcardmappedAccount

        credit_card_acc_list_container.adapter.notifyDataSetChanged()

        rv_acc_list_container.adapter.notifyDataSetChanged()
        Log.d("Sudhir","mapedAccount"+ selectedCreditcardAccount!!.maskedCardNumber)

    }
    override fun onSetCreditCardAccount(position: Int) {

        Log.d("Sudhir","position::"+ position)
//        AddCreditCard("https://dev.tara.app/v0.1/mcpayment/request/tokenization")
        AddCreditCard("https://dev.tara.app/v0.1/tara/payments/mcpayment/request/tokenization")

    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_OK)
        finish()
    }


    private fun DeleteAccountApi(){
        Log.d("DeleteAccountApi","Sudhir DeleteAccountApi Start")
        var accountTokenIds: java.util.ArrayList<String>? = null
        val accountTokenIdsdata = selectedAccount!!.accountTokenId

        accountTokenIds = java.util.ArrayList()
       accountTokenIds.add(accountTokenIdsdata.toString())


        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.deleteAccountrequest(
                            DeleteAccountRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            mobileNumber = getMobileNo(this),
                                            appName = getAppName(this)
                                    ),
                                    transactionId = null,
                                    merchantId = null,
                                    accountTokenIds= accountTokenIds

                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessDeleteAccount(commonResponse) },

                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }


    private fun onSuccessDeleteAccount(result: CommonResponse) {
        Log.d("onSuccessDeleteAccount","Sudhir Delete Success::"+result.transactionId)

//        hideDialog()
        if (result is CommonResponse) {
            Log.d("onSuccessDeleteAccount","Sudhir Delete Success::")
            txnId = result.transactionId.toString()
            TrackDeleteAccountApi()
        }



    }

    private fun TrackDeleteAccountApi(){
        Log.d("TrackDeleteAccountApi","Sudhir track Delete before API::")

        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.trackDeleteAccountrequest(
                            TrackDeleteAccountRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            mobileNumber = getMobileNo(this),
                                            appName = getAppName(this)
                                    ),
                                    transactionId = txnId,
                                    merchantId = null,
                                    requestedLocale = getCurrentLocale(this)

                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessTrackDeleteAccount(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun onSuccessTrackDeleteAccount(result: CommonResponse) {
        if (result is TrackDeleteAccountResponse) {

            Log.d("onSuccessDeleteAccount","Sudhir track Success::")
//            startActivity(intentFor<ManageAccountActivity>(RegistrationActivity.USER_NAME to getStringData(this@ManageAccountActivity, RegistrationActivity.USER_NAME),
//                    RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
//                    RegistrationActivity.USER_MOBILE to getStringData(this@ManageAccountActivity, RegistrationActivity.USER_MOBILE )).newTask().singleTop())
//                overridePendingTransition(0,0)
            recreate()
//            finish()
        }

    }

    fun AddCreditCard(url: String) {
        val json = "{\"register_id\":\"null\",\"callback_url\":\"null\",\"return_url\":\"null\",\"is_transaction\":\"true\",\"transaction\":{\"amount\":\"${1000}\",\"description\":\"Coffee\"},\"optionalData\":{\"customerId\":\"${customerId}\"}}"



        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
                val Model = gson.fromJson(jsonData, WebApiModelResponse::class.java)
                Log.d("Sudhir", "AddCreditCard Model::" + Model.data!!.seamless_url)

                startActivityForResult(
                    intentFor<McPaymentsWebview>(McPaymentsWebview.SEAMLESSURL to Model.data.seamless_url),
                    REQ_CODE_TRANSACTION
                )

            }


        })


    }

    fun DeleteCreditCard(url: String) {
        Log.d("Sudhir","DeleteCreditCard url::"+url)

        val request = Request.Builder()
                .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
                .url(url)
                .delete()
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body?.string())



        })
        recreate()

    }
    fun registerTara(url: String) {

        val json = "{\"mobileNumber\":\"+91${getMobileNo(this) }\",\"password\":null,\"customerProfile\":{\"firstName\":null,\"email\":null,\"customerType\":\"Consumer\",\"registrationStatus\":\"rtp\"}}"


        Log.d("Sudhir","registerTara::"+url)
        Log.d("Sudhir","registerTara json body::"+json)

        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
                .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
                .url(URL(url))
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
                        "https://dev.tara.app/v0.1/tara/crm/customer?mobile_number=91" + getMobileNo(
                            this@ManageAccountActivity
                        )
                    )
                }
            }


        })


    }
    fun getCustomerId(url: String) {
        Log.d("Sudhir","getCustomerId url::"+url)
        val request = Request.Builder()
                .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
                .header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJJbmRlcGF5Iiwic3ViIjoiVGFyYS1Ub2tlbiIsImN1c3RvbWVySWQiOjIsImp0aSI6ImJhZWE4ZTUzLWViMzUtNGJmYy05MjQ1LTRhMTU4MTI3MDg5MyIsImlhdCI6MTYxOTQyMjAyMSwiZXhwIjoxNjE5NTA4NDIxfQ.vX6jZEUkfIY2F88_2gi2nVnhzEf_VZrOKXZwtzeLr3s")
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
                    getrun(mcPaymentsCreditcardsdataUrl + Model.id)
//                    getrun(mcPaymentsCreditcardsdataUrl + 39)
                }
            }

        })
    }

    fun getrun(url: String) {
        Log.d("Sudhir","getrun url::"+url)
        val request = Request.Builder()
                .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
                .url(url)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
//                println(response.body()?.string())
                Log.d("Sudhir", "status code::" + response.code)

                if (response.code == 204) {
                    Handler(Looper.getMainLooper()).post {

                        credit_card_acc_list_container.visibility = View.GONE
                        txt_single_credit_card_add_New.visibility = View.VISIBLE
                    }

                } else {
                    val jsonData: String? = response.body?.string()
                    val gson = GsonBuilder().create()
                    if (jsonData != null && jsonData != "") {
                        val Model =
                            gson.fromJson(jsonData, Array<CreditCardMappedAccount>::class.java)
                                .toList()
                        Log.d("Sudhir", "Model::" + Model)

                        creditcardmappedAccounts.clear()

                        selectedCreditcardAccount = CreditCardMappedAccount()
                        selectedCreditcardAccount!!.addnew = "ADD New"
                        creditcardmappedAccounts.add(0, selectedCreditcardAccount!!)

                        for (i in 0..Model.size) {
                            if (i < Model.size) {
                                if (Model.get(i).status == "success") {
                                    Log.d("Sudhir", "if pos::" + i)
                                    creditcardmappedAccounts.add(Model.get(i))
                                } else {
                                    Log.d("Sudhir", "removed pos::" + i)
//                            creditcardmappedAccounts.removeAt(i)

                                }
                            }
                        }

                        Log.d(
                            "Sudhir",
                            "creditcardmappedAccounts size::" + creditcardmappedAccounts.size
                        )

                    }
                    Handler(Looper.getMainLooper()).post {

                        if (creditcardmappedAccounts.size == 1 || creditcardmappedAccounts.size == null) {
                            credit_card_acc_list_container.visibility = View.GONE
                            txt_single_credit_card_add_New.visibility = View.VISIBLE

                        } else {
                            credit_card_acc_list_container.visibility = View.VISIBLE
                            txt_single_credit_card_add_New.visibility = View.GONE


                        }
                        credit_card_acc_list_container.adapter.notifyDataSetChanged()
                    }


//                        credit_card_acc_list_container.adapter.notifyDataSetChanged()

                }

            }
        })
    }

    fun mcPaymentPostRequestAPI(url: String,creditcardmappedAccount: CreditCardMappedAccount) {

        val json = "{\"register_id\":\"${creditcardmappedAccount.callbackResponsePk!!.mcPaymentCardId}\",\"return_url\":\"https://dev.tara.app:9005/v0.1/mcpayment/payment/callback\",\"token\":\"${creditcardmappedAccount.maskedCardNumber}\",\"amount\":\"${1000}\",\"description\":\"Coffee\"}"

        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
//               println(response.body()?.string())

                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
                val Model = gson.fromJson(jsonData, WebApiModelResponse::class.java)
                Log.d(
                    "Sudhir",
                    "mcPaymentPostRequestAPI Response Status ::" + Model.data!!.seamless_url
                )

                startActivityForResult(
                    intentFor<McPaymentsWebview>(McPaymentsWebview.SEAMLESSURL to Model.data!!.seamless_url),
                    REQ_CODE_TRANSACTION
                )

//                if(Model.data.seamless_url!= null){
//
//
//                    Handler(Looper.getMainLooper()).post {
//                        webView.loadUrl(Model.data.seamless_url)
//                    }
//
//                }
//                getmcPaymentInfo("http://107.20.4.43:9005/v0.1/mcpayment/detail/payment/"+creditcardmappedAccount.callbackResponsePk.mcPaymentCardId)
            }


        })

    }

    fun getmcPaymentInfo(url: String) {
        Log.d("Sudhir","getmcPaymentInfo url::"+url)

        val request = Request.Builder()
                .url(url)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) //= println(response.body()?.string())
            {
                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
                val Model = gson.fromJson(jsonData, McPaymentDetailPaymentResponse::class.java)
                Log.d("Sudhir", "id::" + Model.id)
                Log.d("Sudhir", "register_id::" + Model.register_id)
                Log.d("Sudhir", "amount::" + Model.amount)
                Log.d("Sudhir", "description::" + Model.description)
                Log.d("Sudhir", "status::" + Model.status)

            }
        })

    }

    fun deleteMcPaymentToken(url: String) {
        Log.d("Sudhir","getmcPaymentdelete url::"+url)

        val request = Request.Builder()
                .url(url)
                .delete()
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body?.string())
//            {
//                val jsonData: String? = response.body()?.string()
//                val gson = GsonBuilder().create()
//                val Model= gson.fromJson(jsonData,McPaymentDetailPaymentResponse::class.java)
//                Log.d("Sudhir","id::"+ Model.id)
//                Log.d("Sudhir","register_id::"+ Model.register_id)
//                Log.d("Sudhir","amount::"+ Model.amount)
//                Log.d("Sudhir","description::"+ Model.description)
//                Log.d("Sudhir","status::"+ Model.status)
//
//            }
        })

    }

}
