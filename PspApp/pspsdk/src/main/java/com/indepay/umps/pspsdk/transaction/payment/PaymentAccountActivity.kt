package com.indepay.umps.pspsdk.transaction.payment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.Keep
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.accountSetup.*
import com.indepay.umps.pspsdk.adapter.CrediCardAccountListAdapter
import com.indepay.umps.pspsdk.adapter.PaymentAccountListAdapter
import com.indepay.umps.pspsdk.adapter.SpaceItemDecoration
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnPaymentAccountListInteractionListner
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.models.CommonResponse
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.transaction.collectApprove.TxnCollectApproveActivity
import com.indepay.umps.pspsdk.utils.*
import com.indepay.umps.pspsdk.utils.getCurrentLocale
import com.indepay.umps.pspsdk.utils.getPspId
import com.indepay.umps.pspsdk.utils.saveLocale
import com.indepay.umps.spl.models.*
import com.indepay.umps.spl.models.TransactionType
import com.indepay.umps.spl.transaction.AuthenticateTxnActivity
import com.indepay.umps.spl.utils.*
import com.indepay.umps.spl.utils.Base64
import com.indepay.umps.spl.utils.PKIEncryptionDecryptionUtils
import com.indepay.umps.spl.utils.Result
import com.indepay.umps.spl.utils.SplMessageUtils.createAFterInitiateAccountEncryptionKeyRetrievalRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createAfterauthorizeAccountEncryptionKeyRetrievalRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createCredentialSubmissionAfterInititingAccountRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createFetchOtpEncryptionKeyRetrievalRequest
import kotlinx.android.synthetic.main.activity_manage_account.rv_acc_list_container
import kotlinx.android.synthetic.main.activity_payment_account.*
import kotlinx.android.synthetic.main.activity_payment_account.llProgressBar
import kotlinx.android.synthetic.main.activity_txn_collect_approve.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.jetbrains.anko.*
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList


class PaymentAccountActivity : SdkBaseActivity(), OnPaymentAccountListInteractionListner {

    private val REQ_CODE_TRANSACTION = 1005
    private val REQ_CODE_APPROVE_COLLECT = 10900
    private val REQ_CODE_ADD_ACCOUNT = 10700
    private lateinit var txnId: String
    private var txnId_g :String =""
    private var comingFrom: Int = 0
    private lateinit var remarks:String
    private lateinit var collectDetails: Transaction
    private lateinit var balEnqTxnId: String
    private lateinit var setResetMpinTxnId: String
    private val REQ_CODE_BALANCE_ENQUIRY = 10500
    private val REQ_CODE_SET_RESET_MPIN = 10600
    private val mappedAccounts: ArrayList<MappedAccount> = ArrayList()
    private val creditcardmappedAccounts: ArrayList<CreditCardMappedAccount> = ArrayList()

    private var defaultselectedAccount: MappedAccount? = null
    private var selectedAccount: MappedAccount? = null
    private var selectedCreditcardAccount: CreditCardMappedAccount? = null

    private lateinit var amount:String
    private lateinit var order_id:String

    private lateinit var email:String
    private lateinit var merchName:String
    private lateinit var logintoken: String

    private lateinit var encRetResp: EncryptionKeyRetrievalResponse
    private lateinit var credType: CredType
    private lateinit var apiService: SplApiService
    private lateinit var resultretrieve:EncryptionKeyRetrievalResponse
    private lateinit var transactionType: TransactionType
    private val checkbool : Boolean = false

    private lateinit var bic: String
    private lateinit var appId: String
    private lateinit var mobileNumber: String

    private lateinit var sessionKeyFromRetrieve :String
    private lateinit var bankKeyFromRetrieve :String
    private lateinit var kiFromRetrieve :String

    private var ki_g: String = ""
    private var sessionKey_g: String =""
    private var bankKey_g: String = ""

    //  private  var mcPaymentsCreditcardsdataUrl: String = "https://dev.tara.app/v0.1/tara/payments/mcpayment/detail/card/"
    //   private  var mcPaymentsCreditcardsdataUrl: String = "https://qa.tara.app/v0.1/tara/payments/mcpayment/detail/card/"
//    private  var mcPaymentsCreditcardsdataUrl: String = "https://dev.tara.app:9005/v0.1/mcpayment/detail/card/"

    var customerId:String = ""
    private val REQUEST_CODE_REGISTRATION = 10100

    var accountTokenId : Long = 0

    private var client = OkHttpClient()
    private lateinit var webView: WebView

    private var defaultbool:Boolean = true
    //     private lateinit var addnewmodel: MappedAccount
    private var defaultaccounttokenid: Long = 0

    private val tempReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {



            var otp: String = intent?.getStringExtra("otp").toString()

            createCredSubmissionRequest(
                bankKey = bankKey_g, ki = ki_g, sessionKey = sessionKey_g , mpin = otp, txnType = TransactionType.FINANCIAL_TXN
            )


            println(otp)

//            var successPayment : String = intent?.getStringExtra("result").toString()
//
//            if(successPayment=="true"){
//
//
//
//
//            }







        }

    }


    @Keep
    companion object : SdkCommonMembers(){
        const val LOGIN_TOKEN = "login_token"
        @Keep  const val AMOUNT = "amount"
        @Keep  const val ORDER_ID = "order_id"
        @Keep  const val EMAIL = "email"
        @Keep  const val MERCHANT_NAME = "merchant_name"
        @Keep  const val NOTE = "note"
        @Keep  const val TXN_ID = "txn_id"
        @Keep   const val PSP_ID = "psp_id"

        @Keep   const val REMARKS = "remarks"

        @Keep   const val PAYMENT_STATUS = "payment_status"

        @Keep   const val BIC = "bic"

        @Keep const val TXN_TYPE_PAY = "PAY"
        //const val TXN_TYPE_COLLECT = "COLLECT"
        //FOR MERCHANT PAYMENT
        @Keep  const val TXN_TYPE = "txn_type"
        @Keep  const val BANK_NAME = "Bank_name"
        @Keep  const val TRANSACTION_TYPE = "transaction_type"
        //const val PAYMENT_ADDRESS = "payment_address"
        @Keep  const val MERCHANT_TXN_ID = "merchant_txn_id"
        @Keep  const val BENE_ID = "bene_id"
        @Keep   const val TARGET_SELF_ACCOUNT_ID = "target_self_account_id"
        @Keep  const val INITIATOR_ACCOUNT_ID = "intitiator_account_id"
        @Keep   const val PAYEE_NAME = "payee_name"
        @Keep  const val ACCOUNT_NO = "account_no"
        @Keep  const val MOBILE_NO = "mobile_no"
        @Keep   const val APP_ID = "APP_ID"

        @Keep
        const val WALLET_BAL = "wallet_bal"
        internal const val COLLECT_DETAILS = "collect_details"





    }


    /**
     * This method is called by Base App to see mapped account and to Manage Account
     *              Like (Set/Reset Mpin, Balance Enquiry, Add Bank Account)
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_account)
        LocalBroadcastManager.getInstance(this).registerReceiver(tempReceiver , IntentFilter("PaymentAccountActivity"));
        putPaymentActivity(this)

//        showDialog()
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);



        if(intent.getStringExtra(COLLECT_DETAILS).isNullOrBlank()) {
            amount = intent.getStringExtra(AMOUNT).toString()
            order_id = (intent.getStringExtra(ORDER_ID)).toString()
            email = intent.getStringExtra(EMAIL).toString()
            remarks = intent.getStringExtra(REMARKS).toString()
            merchName = intent.getStringExtra(MERCHANT_NAME).toString()//intent.getStringExtra(MOBILE_NO)
        }else{
            val txnDetails = Gson().fromJson(intent.getStringExtra(COLLECT_DETAILS), Transaction::class.java)
            amount = txnDetails.amount.toString()
            remarks = txnDetails.remarks.toString()
            merchName = txnDetails.counterpartName.toString()
        }

        txt_orderidvalue.setText(order_id)
        if(intent.getStringExtra(RegistrationActivity._LOCALE).isNullOrBlank()){
            getCurrentLocale(this)
        }else {
            saveLocale(this, intent.getStringExtra(RegistrationActivity._LOCALE).toString())
        }

        if(!intent.getStringExtra(RegistrationActivity.APP_ID).isNullOrBlank() && !intent.getStringExtra(RegistrationActivity.USER_NAME).isNullOrBlank()
            && !intent.getStringExtra(RegistrationActivity.USER_MOBILE).isNullOrBlank()) {
            saveAppName(this, intent.getStringExtra(RegistrationActivity.APP_ID).toString())
            saveUserName(this, intent.getStringExtra(RegistrationActivity.USER_NAME).toString())
            saveMobileNo(this, intent.getStringExtra(RegistrationActivity.USER_MOBILE).toString())
        }


        if (!getBooleanData(this, IS_REGISTERED)) {
            if(getUserToken(this).isNullOrBlank()) {
                fetchAppToken { token ->
//                    hideDialog()
                    llProgressBar.visibility = View.GONE
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    //  showRegistrationDialog(token)
                    startActivityForResult<RegistrationActivity>(REQUEST_CODE_REGISTRATION,
                        RegistrationActivity.USER_NAME to getStringData(this, RegistrationActivity.USER_NAME),
                        RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                        RegistrationActivity.USER_MOBILE to getMobileNo(this),
                        USER_TOKEN to token,RegistrationActivity.AMOUNT to amount,
                        RegistrationActivity.ORDER_ID to order_id,RegistrationActivity.EMAIL to email,RegistrationActivity.MERCHANT_NAME to merchName,
                        RegistrationActivity.REMARKS to remarks)
                    finish()
                }
            }else{
                fetchAppToken { token ->
//                    hideDialog()
                    llProgressBar.visibility = View.GONE
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    //  showRegistrationDialog(getUserToken(this))
                    startActivityForResult<RegistrationActivity>(REQUEST_CODE_REGISTRATION,
                        RegistrationActivity.USER_NAME to getStringData(this, RegistrationActivity.USER_NAME),
                        RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                        RegistrationActivity.USER_MOBILE to getMobileNo(this),
                        USER_TOKEN to getUserToken(this))
                    finish()
                }
            }
        }else {
//            // showDialog()
//            fetchAppToken { token ->
//                saveUserToken(this, token)
//                saveAccessToken(this,token)
//                Log.d("Sudhir","Sudhir after Inside else block")
//            }
            apiService = SplApiService.create(getSSLConfig(this))
            appId =BuildConfig.APP_NAME
            transactionType = TransactionType.FINANCIAL_TXN

        }
//        fetchAppToken { token ->
//            Log.d("Sudhir","Sudhir after else block")
//        }



        val layoutManager = GridLayoutManager(this,2)
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        comingFrom = intent.getIntExtra(COMING_FROM, 0)

//        logintoken = intent.getStringExtra(PaymentAccountActivity.LOGIN_TOKEN)
//        saveLoginToken(this , logintoken)
        mobileNumber = getMobileNo(this)

        txt_payment_amt.text = intent.getStringExtra(AMOUNT)



/*
        val selectedAccount: ArrayList<MappedAccount> = ArrayList()
        val mappedAccounts = selectedAccount
*/
        //    Log.d("Sudhir","Creditcard size::"+creditcardmappedAccounts.size)
//        if(creditcardmappedAccounts.size<1) {
//            val layoutManagercreditcard = android.support.v7.widget.LinearLayoutManager(this)
//            val itemDecorationcreditcard = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)
//
//            credit_card_acc_list_container.layoutManager = layoutManagercreditcard
//            credit_card_acc_list_container.addItemDecoration(itemDecorationcreditcard)
////            credit_card_acc_list_container.addItemDecoration( SpaceItemDecoration(10))
//
//        }else{
        val layoutManagercreditcard = GridLayoutManager(this, 2)
        val itemDecorationcreditcard = DividerItemDecoration(this, layoutManager.orientation)

        credit_card_acc_list_container.layoutManager = layoutManagercreditcard
//        credit_card_acc_list_container.addItemDecoration(itemDecorationcreditcard)
        credit_card_acc_list_container.addItemDecoration( SpaceItemDecoration(10))

//        }


        val creditcardaccountListAdapter = CrediCardAccountListAdapter(
            creditcardaccountList = creditcardmappedAccounts,
            accountList = mappedAccounts,
            listener = this)
        credit_card_acc_list_container.adapter = creditcardaccountListAdapter



        rv_acc_list_container.layoutManager = layoutManager
//        rv_acc_list_container.addItemDecoration(itemDecoration)
        rv_acc_list_container.addItemDecoration( SpaceItemDecoration(10))


        val picasso = getPicassoInstance(this, getPspSslConfig(this))
        val accountListAdapter = PaymentAccountListAdapter(
            accountList = mappedAccounts,
            creditcardaccountList = creditcardmappedAccounts,
            listener = this,
            picassoInstance = picasso,
            accessToken = getAccessToken(this),
            appName = getAppName(this),
            custPspId = getPspId(this))
        rv_acc_list_container.adapter = accountListAdapter


        my_default_account.setOnClickListener {
            //   Log.d("Sudhir","Default account clicked")
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
            startActivity(intentFor<EcosystemBanksActivity>(EcosystemBanksActivity.AMOUNT to amount,
                EcosystemBanksActivity.ORDER_ID to order_id,EcosystemBanksActivity.EMAIL to email,EcosystemBanksActivity.MERCHANT_NAME to merchName,
                EcosystemBanksActivity.REMARKS to remarks).singleTop().clearTop())

        }
        txt_single_credit_card_add_New.setOnClickListener {
//            AddCreditCard("https://dev.tara.app/v0.1/tara/payments/mcpayment/request/tokenization")
//            AddCreditCard("https://qa.tara.app/v0.1/tara/payments/mcpayment/request/tokenization")

//            AddCreditCard("https://dev.tara.app:9005/v0.1/mcpayment/request/tokenization")

        }

        cash_payment.setOnClickListener {
            Log.d("Sudhir","cash_payment clicked")
//            startActivity(intentFor<MapsActivity>().singleTop().clearTop())
            startActivity(intentFor<MapboxActivity>().singleTop().clearTop())

        }

//        txt_credit_card_add_New.setOnClickListener {
//
//            Log.d("Sudhir","credit card Add New clicked")
//
//            AddCreditCard("http://107.20.4.43:9005/v0.1/mcpayment/request/tokenization")
//
//        }
        back_arrowimage.setOnClickListener {
            //    Log.d("Sudhir","Back Arrow clicked")
            onBackPressed()
        }
//        txt_credit_card_delete.setOnClickListener {
//            Log.d("Sudhir","credit card Delete clicked")
//
//            DeleteCreditCard("http://107.20.4.43:9005/v0.1/mcpayment/delete/token/4142da30d01935b17cddb6c4184f0f59f2d4bda372f97a250daf86741ec15cec")
//
//        }
//        txt_add_New_bank_account.setOnClickListener {
//            Log.d("Sudhir","Other Add New clicked")
//
//            startActivity(intentFor<EcosystemBanksActivity>().singleTop().clearTop())
//        }

        button_proceed.setOnClickListener {

            if (selectedCreditcardAccount != null) {
                if (selectedCreditcardAccount!!.isSelected == true && defaultbool == false) {
                    //         Log.d("Sudhir", "credit card selected ::" + selectedCreditcardAccount!!.callbackResponsePk!!.mcPaymentCardId)

                    mcPaymentPostRequestAPI("https://dev.tara.app/v0.1/tara/payments/mcpayment/request/payment", selectedCreditcardAccount!!)
//                    mcPaymentPostRequestAPI("https://qa.tara.app/v0.1/tara/payments/mcpayment/request/payment", selectedCreditcardAccount!!)


//              getmcPaymentInfo("http://107.20.4.43:9005/v0.1/mcpayment/detail/payment/"+ selectedCreditcardAccount!!.callbackResponsePk!!.mcPaymentCardId)

                } else {
                    if (selectedAccount!!.isSelected == true || defaultbool == true) {
                        if (comingFrom == 1) {
                            initiateTransaction()
                            //approveCollectTransaction(intent.getStringExtra(TXN_ID), intent.getStringExtra(BIC))
                        } else {

                            if (getBooleanData(this, IS_REGISTERED)) {
//                    showDialog()
                                llProgressBar.visibility = View.VISIBLE
                                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                initiateCollectRequest()
                            }
                            //approveCollectTransaction(intent.getStringExtra(TXN_ID), selectedAccount?.bic.toString())
                        }
                    } else {
                        Toast.makeText(this, "Please Select a Card to Proceed", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                if (selectedAccount!!.isSelected == true || defaultbool == true) {
                    if (comingFrom == 1) {
                        initiateTransaction()
                        //approveTransferTransaction(intent.getStringExtra(TXN_ID), intent.getStringExtra(BIC))
                        //approveCollectTransaction(intent.getStringExtra(TXN_ID), intent.getStringExtra(BIC))
                        if (getBooleanData(this, IS_REGISTERED)) {
//                    showDialog()
                            llProgressBar.visibility = View.VISIBLE
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            initiateCollectRequest()
                        }
                    } else {

                        if (getBooleanData(this, IS_REGISTERED)) {
//                    showDialog()
                            llProgressBar.visibility = View.VISIBLE
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            initiateCollectRequest()
                        }
                        //approveCollectTransaction(intent.getStringExtra(TXN_ID), selectedAccount?.bic.toString())
                    }
                } else {
                    Toast.makeText(this, "Please Select a Card to Proceed", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    fun AddCreditCard(url: String) {
        val json = "{\"register_id\":\"null\",\"callback_url\":\"null\",\"return_url\":\"null\",\"is_transaction\":\"true\",\"transaction\":{\"amount\":\"${1000}\",\"description\":\"Coffee\"},\"optionalData\":{\"customerId\":\"${customerId}\"}}"

        Log.d("Sudhir","URL ::"+url)
        Log.d("Sudhir","AddCreditCard json body::"+json)

        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
            .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
            .url(url)
            .post(body)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                val jsonData: String? = response.body?.string()
                Log.d("Sudhir", "jsonDataResponse::" + jsonData)
                val gson = GsonBuilder().create()
                val Model = gson.fromJson(jsonData, WebApiModelResponse::class.java)
                //         Log.d("Sudhir","AddCreditCard Model::"+Model.data!!.seamless_url)

                startActivityForResult(
                    intentFor<McPaymentsWebview>(McPaymentsWebview.SEAMLESSURL to Model.data!!.seamless_url),
                    REQ_CODE_TRANSACTION
                )

            }


        })


    }

    fun DeleteCreditCard(url: String) {

        val request = Request.Builder()
            .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
            .url(url)
            .delete()
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) = println(response.body?.string())

        })


    }
    fun registerTara(url: String) {

        val json = "{\"mobileNumber\":\"+91${getMobileNo(this) }\",\"password\":null,\"customerProfile\":{\"firstName\":null,\"email\":null,\"customerType\":\"Consumer\",\"registrationStatus\":\"rtp\"}}"


        Log.d("Sudhir","registerTara::"+url)
        Log.d("Sudhir","registerTara json body::"+json)

//        val connectionSpecs: ArrayList<ConnectionSpec> = ArrayList()
//        connectionSpecs.add(ConnectionSpec.COMPATIBLE_TLS)
//        client.connectionSpecs(connectionSpecs)

        //   Log.d("Sudhir","registerTara url New::"+url)
        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
            .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
            .url(URL(url))
            .post(body)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //      Log.d("Sudhir","registerTara failure::"+e)
            }
            override fun onResponse(call: Call, response: Response) //= println(response.body()?.string())
            {
                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
//                val Model= gson.fromJson(jsonData,WebApiModelResponse::class.java)
                Log.d(
                    "Sudhir", "" +
                            "Response Status::" + response.code
                )
                if (response.code == 500 || response.code == 200) {
                    //       getCustomerId("https://dev.tara.app/v0.1/tara/crm/customer?mobile_number=91" + getMobileNo(this@PaymentAccountActivity))
//                    getCustomerId("https://qa.tara.app/v0.1/tara/crm/customer?mobile_number=91" + getMobileNo(this@PaymentAccountActivity))

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
//                    getrun(mcPaymentsCreditcardsdataUrl + Model.id)
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
                        //    Log.d("Sudhir", "Model::" + Model)

                        creditcardmappedAccounts.clear()

                        selectedCreditcardAccount = CreditCardMappedAccount()
                        selectedCreditcardAccount!!.addnew = "ADD New"
                        creditcardmappedAccounts.add(0, selectedCreditcardAccount!!)

                        for (i in 0..Model.size) {
                            if (i < Model.size) {
                                if (Model.get(i).status == "success") {
                                    //      Log.d("Sudhir", "if pos::" + i)
                                    creditcardmappedAccounts.add(Model.get(i))
                                } else {
                                    //      Log.d("Sudhir", "removed pos::" + i)
//                            creditcardmappedAccounts.removeAt(i)

                                }
                            }
                        }

                        //        Log.d("Sudhir", "creditcardmappedAccounts size::" + creditcardmappedAccounts.size)

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

    private fun initiateCollectRequest(){
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        if(defaultbool == true){
            accountTokenId = defaultaccounttokenid
        }else {
            accountTokenId = when (selectedAccount) {
                null -> getAccountTokenId(this)
                else -> selectedAccount?.accountTokenId!!
            }
        }
        val payees = ArrayList<Payee>()
//        remarks = intent.getStringExtra(NOTE)
        if (remarks.isNullOrBlank()) {
            remarks = resources.getString(R.string.paid_by)+" ${getUserName(this)}"
        }
        payees.add(Payee(
            amount = intent.getStringExtra(AMOUNT).toString()
        ))
        callMerchantApi(
            merchantId = BuildConfig.MERCHANT_ID,

            apiToCall = { sdkApiService ->
                sdkApiService.initiateMerchantTransactionAsync(
                    MerchantCollectRequest(
                        merchantId = BuildConfig.MERCHANT_ID,
                        accessToken = getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID),
                        type = "COLLECT",
                        //initiatorPA = initiatorPa,
                        payer = Payer(
                            //pa = getStringData(this, com.indepay.umps.activities.RegistrationActivity.MOBILE_NUMBER) + PA_HANDLE
                            mobile = getMobileNo(this),
                            appId = BuildConfig.APP_NAME
                        ),
                        payees = payees,
                        remarks = remarks,
                        refURL = BuildConfig.REFERENCE_URL,
                        refId = order_id,
                        subMerchantName = intent.getStringExtra(MERCHANT_NAME).toString(),
                        custRefId = "Merchant:"+ System.currentTimeMillis()

                    ))
            },
            successCallback = { merchantCollectResponse ->
                saveStringData(this@PaymentAccountActivity, TxnCollectApproveActivity.MRCH_TXN_ID, merchantCollectResponse.transactionId);
                when {
                    !(getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID).isNullOrBlank()) -> {
                        fetchTxnDetails(getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID))
                    }
                    intent.hasExtra(TxnCollectApproveActivity.ORIG_TXN_ID) -> {
                        collect_approval_root.visibility = View.VISIBLE
                        fetchTxnDetails(intent.getStringExtra(TxnCollectApproveActivity.ORIG_TXN_ID).toString())
                    }
//                            else -> {
//                                hideDialog()
//                                collectDetails = Gson().fromJson(intent.getStringExtra(TxnCollectApproveActivity.COLLECT_DETAILS), Transaction::class.java)
//                                collect_approval_root.visibility = View.VISIBLE
//
//                                //callApproveCollectInitApi()
//                            }
                }
            },
            errorCallback = { it ->
//                        hideDialog()
                llProgressBar.visibility = View.GONE
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                val errCode = it.errorCode
                val errReason = it.errorReason

                errReason?.let { it1 ->
                    alert(it1, errCode) {
                        okButton {}
                    }.show()
                }
            }
        )
    }

    private fun fetchTxnDetails(txnId: String) {
        callApi(
            accessToken = retrieveAccessToken(this),
            custPSPId = getPspId(this),
            appName = getAppName(this),
            apiToCall = { sdkApiService ->
                sdkApiService.getTransactionHistoryDetailsAsync(
                    appName = getAppName(this),
                    accessToken = getAccessToken(this),
                    custPSPId = getPspId(this),
                    txnId = txnId,
                    requestedLocale = getCurrentLocale(this),
                    includeWaitingForApprovalOnly = true
                )
            },
            successCallback = { commonResponse -> onSuccessTxnDetails(commonResponse) },
            errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun onSuccessTxnDetails(response: CommonResponse) {
        if (response is TxnHistoryResponse) {
            if (response.transactionList.isEmpty()) {
                hideDialog()
                alert(resources.getString(R.string.search_again), resources.getString(R.string.trans_not_found)) {
                    okButton {
                        when {
                            !(getStringData(this@PaymentAccountActivity, TxnCollectApproveActivity.MRCH_TXN_ID).isNullOrBlank()) -> {
                                fetchTxnDetails(getStringData(this@PaymentAccountActivity, TxnCollectApproveActivity.MRCH_TXN_ID))
                            }
                            intent.hasExtra(TxnCollectApproveActivity.ORIG_TXN_ID) -> {
                                fetchTxnDetails(intent.getStringExtra(TxnCollectApproveActivity.ORIG_TXN_ID).toString())
                            }
                        }
                    }
                    cancelButton { finish() }
                    isCancelable = false
                }.show()
            } else {
//                hideDialog()
                llProgressBar.visibility = View.GONE
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                collectDetails = response.transactionList[0] //It should contain only one item.
                var bic = ""
                if(selectedAccount != null){
                    bic = selectedAccount?.bic.toString()
                }
                approveCollectTransaction(collectDetails.transactionId.orEmpty(), bic)
            }
        }
    }

    private fun approveTransferTransaction(txnId: String, bic: String){
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        val mrchTxnId = getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID)
        val txnId = if (mrchTxnId == null) {
            intent.getStringExtra(TxnCollectApproveActivity.ORIG_TXN_ID) ?: txnId
        } else {
            null
        }

        if(defaultbool == true){
            accountTokenId = defaultaccounttokenid
        }else {
            accountTokenId = when (selectedAccount) {
                null -> getAccountTokenId(this)
                else -> selectedAccount?.accountTokenId!!
            }
        }

        callApi(
            accessToken = retrieveAccessToken(this),
            custPSPId = getPspId(this),
            appName = getAppName(this),
            apiToCall = { sdkApiService ->
                sdkApiService.collectApprovalRequestAsync(
                    CollectApproveRequest(
                        acquiringSource = AcquiringSource(
                            appName = getAppName(this)
                        ),
                        approvedAmount =  intent.getStringExtra(AMOUNT).toString(),
                        approved = true,
                        //paAccountId = getPaAccountId(this),
                        accountId = accountTokenId,
                        custPSPId = getPspId(this),
                        accessToken = getAccessToken(this),
                        transactionId = txnId,
                        merchantTxnId = mrchTxnId,
                        merchantId = BuildConfig.MERCHANT_ID,
                        requestedLocale = getCurrentLocale(this)
                    )
                )
            },
            successCallback = { commonResponse -> onSuccessCollectApprove(commonResponse) },
            errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }
    private fun approveCollectTransaction(txnId: String, bic: String){
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        val mrchTxnId = getStringData(this, TxnCollectApproveActivity.MRCH_TXN_ID)
        val txnId = if (mrchTxnId == null) {
            intent.getStringExtra(TxnCollectApproveActivity.ORIG_TXN_ID) ?: txnId
        } else {
            null
        }

        if(defaultbool == true){
            accountTokenId = defaultaccounttokenid
        }else {
            accountTokenId = when (selectedAccount) {
                null -> getAccountTokenId(this)
                else -> selectedAccount?.accountTokenId!!
            }
        }

        callApi(
            accessToken = retrieveAccessToken(this),
            custPSPId = getPspId(this),
            appName = getAppName(this),
            apiToCall = { sdkApiService ->
                sdkApiService.collectApprovalRequestAsync(
                    CollectApproveRequest(
                        acquiringSource = AcquiringSource(
                            appName = getAppName(this)
                        ),
                        approvedAmount =  intent.getStringExtra(AMOUNT).toString(),
                        approved = true,
                        //paAccountId = getPaAccountId(this),
                        accountId = accountTokenId,
                        custPSPId = getPspId(this),
                        accessToken = getAccessToken(this),
                        transactionId = txnId,
                        merchantTxnId = mrchTxnId,
                        merchantId = BuildConfig.MERCHANT_ID,
                        requestedLocale = getCurrentLocale(this)
                    )
                )
            },
            successCallback = { commonResponse -> onSuccessCollectApprove(commonResponse) },
            errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }

    private fun onSuccessCollectApprove(response: CommonResponse) {
//        hideDialog()
//        llProgressBar.visibility = View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if (response is CollectApproveResponse) {
            txnId = response.transactionId.orEmpty()
            bic = response.bic.orEmpty()
            var accountNo : String = ""

            if(defaultbool==true){
                accountNo = defaultselectedAccount?.maskedAccountNumber!!.replace("#", "\n")
            }else {
                accountNo=  when (selectedAccount) {
                    null -> ""
                    else -> selectedAccount?.maskedAccountNumber!!.replace("#", "\n")
                }
            }
            callRetrieveKeysAfterInitiateTransactionApi()



        }
    }
    fun String.digitsOnly(): String{
        val regex = Regex("[^0-9]")
        return regex.replace(this, "")
    }
    fun String.alphaNumericOnly(): String{
        val regex = Regex("[^A-Za-z0-9 ]")
        return regex.replace(this, "")
    }

    private fun initiateTransaction() {
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        var remarks = intent.getStringExtra(NOTE)
        var amount = intent.getStringExtra(AMOUNT)
        var txnType = "PAY"//intent.getStringExtra(TRANSACTION_TYPE)
        var mobile = intent.getStringExtra(MOBILE_NO)//"1234567890"

        //var remarks = intent.getStringExtra(NOTE)
        if(defaultbool == true){
            accountTokenId = defaultaccounttokenid
        }else {

            accountTokenId  = when(selectedAccount) {
                null -> getAccountTokenId(this).toLong()
                else -> selectedAccount?.accountTokenId!!.toLong()
            }
        }
        if (remarks.isNullOrBlank()) {
            remarks = "-"
        }
        var payees: ArrayList<Payee>? = null
        var payer: Payer? = null
        if (intent.getStringExtra(TRANSACTION_TYPE).equals(TXN_TYPE_PAY, ignoreCase = true)) { //intent.getStringExtra(TRANSACTION_TYPE)
            val payee = Payee(
                beneId = intent.getStringExtra(BENE_ID),
                //amount = amount, //intent.getStringExtra(AMOUNT),
                amount = intent.getStringExtra(AMOUNT).toString(),
                //targetSelfPaAccountId = intent.getStringExtra(TARGET_SELF_PA_ACCOUNT_ID)
                //mobile = mobile, //intent.getStringExtra(MOBILE_NO),
                mobile = intent.getStringExtra(MOBILE_NO).toString().digitsOnly(),
                targetSelfAccountId = intent.getStringExtra(TARGET_SELF_ACCOUNT_ID),
                appId = intent.getStringExtra(APP_ID)!!


            )
            payees = ArrayList()
            payees.add(payee)
        }

        callApi(
            accessToken = retrieveAccessToken(this),
            custPSPId = getPspId(this),
            appName = getAppName(this),
            apiToCall = { sdkApiService ->
                sdkApiService.initTransactionAsync(
                    TransactionRequest(
                        custPSPId = getPspId(this),
                        requestedLocale = getCurrentLocale(this),
                        acquiringSource = AcquiringSource(
                            appName = getAppName(this)
                        ),
                        accessToken = getAccessToken(this),
                        merchantTxnId = intent.getStringExtra(MERCHANT_TXN_ID),
                        type = intent.getStringExtra(TRANSACTION_TYPE), //intent.getStringExtra(TRANSACTION_TYPE),
                        remarks = remarks,
                        payees = payees,
                        payer = payer,
                        //initiatorPaAccountId = intent.getStringExtra(INITIATOR_PA_ACCOUNT_ID)
                        //initiatorAccountId = intent.getLongExtra(PaymentAccountActivity.INITIATOR_ACCOUNT_ID, 0).toString()
                        //initiatorAccountId = selectedAccount?.accountTokenId.toString()
                        initiatorAccountId = accountTokenId.toString()
                    )
                )
            },
            successCallback = { commonResponse ->
                if (commonResponse is TransactionResponse) {
                    txnId = commonResponse.transactionId.orEmpty()
                    //if (intent.getStringExtra(TRANSACTION_TYPE).equals(TXN_TYPE_PAY, ignoreCase = true)) {
                    if (intent.getStringExtra(TRANSACTION_TYPE).equals(TXN_TYPE_PAY, ignoreCase = true)) {
                        //ajaycallSplForTransaction(commonResponse.bic.orEmpty())
                        //callAccountSelection(commonResponse.bic.orEmpty())

                    }

                }
            },
            errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }






    override fun onResume() {
        super.onResume()


        if(getBooleanData(this, IS_REGISTERED)) {
            defaultbool = true
            my_default_account.setForeground(getDrawable(R.drawable.selector))
            my_default_account.useCompatPadding = false
            selected_card_imge.visibility = View.VISIBLE

//            registerTara("https://dev.tara.app/v0.1/tara/auth/credential")
//            registerTara("https://qa.tara.app/v0.1/tara/auth/credential")
//            getCustomerId("http://107.20.4.43:9001/v1/tara/crm/customer?mobile_number=91"+ getMobileNo(this))
//            getrun(mcPaymentsCreditcardsdataUrl+39)

            fetchAppToken { token ->
                //    Log.d("Sudhir","Sudhir Payment Account onResume::"+token)

                fetchAddedBankAcList()
            }
        }
    }


    @SuppressLint("ResourceAsColor")
    private fun onSuccessMappedAccFetch(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if (result is CustomerProfileResponse && result.mappedBankAccounts != null) {
            mappedAccounts.clear()
            selectedAccount =  MappedAccount()
            selectedAccount!!.addnew = "ADD New"
            mappedAccounts.add(0, selectedAccount!!)

            mappedAccounts.addAll(result.mappedBankAccounts)
            //   Log.d("Sudhir", "mapped Account size::" + mappedAccounts.size)
//            val picassoInstance: Picasso
            val picassoInstance = getPicassoInstance(this, getPspSslConfig(this))
            if (mappedAccounts.isNotEmpty()) {
                for (i in 1..mappedAccounts.size) {
                    if (i<mappedAccounts.size) {
                        if (mappedAccounts.get(i).isDefault == true) {
//                            rdo_set_default_my.isChecked=true
                            defaultselectedAccount = mappedAccounts.get(i)
                            defaultaccounttokenid = mappedAccounts.get(i).accountTokenId!!
                            var lstValues: List<String> = emptyList()
                            if (mappedAccounts.get(i).maskedAccountNumber!!.contains("#")){

                                lstValues = mappedAccounts.get(i).maskedAccountNumber!!.split("#").map { it -> it.trim()}
                                txt_debitcard_no.text = lstValues.get(1)
                            }else{

                                //lstValues = listOf(mappedAccounts.get(i).maskedAccountNumber) as List<String>
                                txt_debitcard_no.text = mappedAccounts.get(i).maskedAccountNumber
                            }
                            //val lstValues: List<String> =  mappedAccounts.get(i).maskedAccountNumber!!.split("#").map { it -> it.trim() }

//                            txt_expiry_no.text = lstValues.get(2)
                            picassoInstance.load(getPspBaseUrlForBankLogo(mappedAccounts.get(i).bic.orEmpty(), getAccessToken(this), getAppName(this), getPspId(this))).placeholder(R.drawable.ic_bank_place_holder).into(img_bank_logo)
                            txt_bank_name.text = mappedAccounts.get(i).bankName
//                            txt_account_type.text = mappedAccounts.get(i).accountType
                            //             Log.d("Sudhir", "text::" + txt_debitcard_no.text)
                            mappedAccounts.removeAt(i)
                        }
                    }
                }
                if(mappedAccounts.size<1){
                    my_default_account.visibility = View.GONE
                    add_new_cardview.visibility = View.VISIBLE
                    txt_other_bank_accounts.visibility = View.GONE
                    rv_acc_list_container.visibility = View.GONE
                }else{
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

    private fun fetchAddedBankAcList() {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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






    /**
     * This method is called by SDK to SPL, to initiate Set/Reset Mpin of mapped account.
     * @param SetResetMpinInitRequest
     * @return SetResetMpinInitResponse
     */



    private fun callSplForTransaction(bic: String) {
        startActivityForResult<AuthenticateTxnActivity>(REQ_CODE_TRANSACTION,
            AuthenticateTxnActivity.APP_ID to getAppName(this),
            AuthenticateTxnActivity.BIC to bic,
            AuthenticateTxnActivity.TXN_ID to txnId,
            AuthenticateTxnActivity.MOBILE_NO to getMobileNo(this),
            AuthenticateTxnActivity.PSP_ID to getPspId(this),
            AuthenticateTxnActivity.TXN_TYPE to TransactionType.FINANCIAL_TXN.name,
            AuthenticateTxnActivity.BANK_NAME to getBankNameFromBic(this, bic),
            AuthenticateTxnActivity.PAYEE_NAME to intent.getStringExtra(PAYEE_NAME),
            AuthenticateTxnActivity.ACCOUNT_NO to intent.getStringExtra(ACCOUNT_NO),
            AuthenticateTxnActivity.AMOUNT to intent.getStringExtra(AMOUNT),
            AuthenticateTxnActivity.NOTE to intent.getStringExtra(NOTE)
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {

            requestCode == REQ_CODE_TRANSACTION && resultCode == Activity.RESULT_OK -> {
//                llProgressBar.visibility = View.VISIBLE
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                callTxnTrackerApi(
//                        trackerApi = { sdkApiService ->
//                            sdkApiService.trackTransactionAsync(
//                                    createTxnTrackerRequest(txnId)
//                            )
//                        },
//                        successCallback = { result ->
//                            llProgressBar.visibility = View.GONE
//                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//                            if (result is TrackerResponse) {
//                val  validate_success = data!!.getBooleanExtra("VALIDATE_MESSAGE",false)
//                Log.d("SUdhir","validate::"+validate_success)
                alert(resources.getString(R.string.payment_complete), resources.getString(R.string.success)) {
                    okButton {
                        val resultIntent = Intent()
                        resultIntent.putExtra(PaymentAccountActivity.TXN_ID, txnId)
                        resultIntent.putExtra(PaymentAccountActivity.PAYMENT_STATUS, true)
//                                        resultIntent.putExtra(PaymentAccountActivity.PAYMENT_STATUS, result.success)
                        setResult(Activity.RESULT_OK, resultIntent)

                        finish() }
                    isCancelable = false
                }.show()
//                            }
//                        }
//                )
            }


            requestCode == REQ_CODE_APPROVE_COLLECT && resultCode == Activity.RESULT_OK -> {
//                showDialog()
                llProgressBar.visibility = View.VISIBLE
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                callTxnTrackerApi(
                    trackerApi = { sdkApiService ->
                        sdkApiService.trackTransactionAsync(
                            createTxnTrackerRequest(txnId)
                        )
                    },
                    successCallback = { result ->
//                            hideDialog()
                        llProgressBar.visibility = View.GONE
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        if (result is TrackerResponse) {
                            alert(resources.getString(R.string.collect_approve), resources.getString(R.string.trn_cmplt)) {
                                okButton {
                                    /*val resultIntent = Intent()
                                    resultIntent.putExtra(TxnCollectApproveActivity.WALLET_BAL, collectDetails.amount)
                                    setResult(Activity.RESULT_OK, resultIntent)*/
                                    finish()
                                }
                                isCancelable = false
                            }.show()
                        }
                    }

                )
            }
            requestCode == REQ_CODE_APPROVE_COLLECT && resultCode == Activity.RESULT_CANCELED -> {
                alert(resources.getString(R.string.transaction_in_pro), resources.getString(R.string.tran_status)) {
                    okButton { finish() }
                    isCancelable = false
                }.show()
            }

            requestCode == REQ_CODE_BALANCE_ENQUIRY && resultCode == Activity.RESULT_OK -> {
//                showDialog()
                llProgressBar.visibility = View.VISIBLE
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                callTxnTrackerApi(
                    trackerApi = { sdkApiService ->
                        sdkApiService.trackBalanceEnquiryAsync(
                            createTxnTrackerRequest(balEnqTxnId)
                        )
                    },
                    successCallback = { result ->
//                            hideDialog()
                        llProgressBar.visibility = View.GONE
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        if (result is TrackerResponse) {
                            alert(resources.getString(R.string.ledger_bal)+": ${result.ledgerBalance}\n\n"+resources.getString(R.string.avail_bal)+": ${result.availableBalance}", resources.getString(R.string.avail_bal)) {
                                okButton {
                                    //finish()
                                }
                                isCancelable = false
                            }.show()
                        }
                    }
                )
            }
            requestCode == REQ_CODE_SET_RESET_MPIN && resultCode == Activity.RESULT_OK -> {
//                showDialog()
                llProgressBar.visibility = View.VISIBLE
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                callTxnTrackerApi(
                    trackerApi = { sdkApiService ->
                        sdkApiService.trackResetCredRequestAsync(
                            createTxnTrackerRequest(setResetMpinTxnId)
                        )
                    },
                    successCallback = { result ->
//                            hideDialog()
                        llProgressBar.visibility = View.GONE
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        if (result is TrackerResponse) {
                            alert(resources.getString(R.string.mpin_succ), resources.getString(R.string.trn_cmplt)) {
                                okButton { finish() }
                                isCancelable = false
                            }.show()
                        }
                    }
                )
            }

            requestCode == REQ_CODE_ADD_ACCOUNT && resultCode == Activity.RESULT_OK -> {
//                showDialog()
                llProgressBar.visibility = View.VISIBLE
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                alert(resources.getString(R.string.collect_approve), resources.getString(R.string.trn_cmplt)) {
                    okButton {

                        finish()
                    }
                    isCancelable = false
                }.show()
            }

            else -> {
                // Toast.makeText(this,resources.getString(R.string.something_wrong), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onSetDefaultAccount(mappedAccount: MappedAccount) {
        if(defaultbool == true) {
            selected_card_imge.visibility = View.GONE
            my_default_account.setForeground(null)
            my_default_account.useCompatPadding = true

            defaultbool = false
        }
        if(mappedAccount.addnew=="+ ADD NEW"){
            //    Log.d("Sudhir","Add New")
            startActivity(intentFor<EcosystemBanksActivity>(EcosystemBanksActivity.AMOUNT to amount,
                EcosystemBanksActivity.ORDER_ID to order_id,EcosystemBanksActivity.EMAIL to email,EcosystemBanksActivity.MERCHANT_NAME to merchName,
                EcosystemBanksActivity.REMARKS to remarks).singleTop().clearTop())
        }
        selectedAccount = mappedAccount

        credit_card_acc_list_container.adapter.notifyDataSetChanged()

        rv_acc_list_container.adapter.notifyDataSetChanged()

        //   Log.d("Sudhir","mapedAccount"+ selectedAccount!!.maskedAccountNumber)
    }

    override fun onSetDefaultAccount(position: Int) {
        //    Log.d("Sudhir","position::"+ position)
        my_default_account.setForeground(null)
        my_default_account.useCompatPadding = true

        credit_card_acc_list_container.adapter.notifyDataSetChanged()

        rv_acc_list_container.adapter.notifyDataSetChanged()
        //    Log.d("Sudhir","Add New::")
        startActivity(intentFor<EcosystemBanksActivity>(EcosystemBanksActivity.AMOUNT to amount,
            EcosystemBanksActivity.ORDER_ID to order_id,EcosystemBanksActivity.EMAIL to email,EcosystemBanksActivity.MERCHANT_NAME to merchName,
            EcosystemBanksActivity.REMARKS to remarks).singleTop().clearTop())

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
        //   Log.d("Sudhir","mapedAccount"+ selectedCreditcardAccount!!.maskedCardNumber)

    }
    override fun onSetCreditCardAccount(position: Int) {

        //   Log.d("Sudhir","position::"+ position)
        AddCreditCard("https://dev.tara.app/v0.1/tara/payments/mcpayment/request/tokenization")
//        AddCreditCard("https://qa.tara.app/v0.1/tara/payments/mcpayment/request/tokenization")
//        AddCreditCard("https://dev.tara.app:9005/v0.1/mcpayment/request/tokenization")

    }


    fun mcPaymentPostRequestAPI(url: String,creditcardmappedAccount: CreditCardMappedAccount) {

        val json = "{\"register_id\":\"${creditcardmappedAccount.callbackResponsePk!!.mcPaymentCardId}\",\"return_url\":\"https://dev.tara.app/v0.1/tara/payments/mcpayment/payment/callback\",\"token\":\"${creditcardmappedAccount.maskedCardNumber}\",\"amount\":\"${1000}\",\"description\":\"Coffee\"}"

        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) {
                println(response.body?.string())

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
        //  Log.d("Sudhir","getmcPaymentInfo url::"+url)

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
                //     Log.d("Sudhir","id::"+ Model.id)
                //     Log.d("Sudhir","register_id::"+ Model.register_id)
                //     Log.d("Sudhir","amount::"+ Model.amount)
                //     Log.d("Sudhir","description::"+ Model.description)
                //     Log.d("Sudhir","status::"+ Model.status)

            }
        })

    }

    fun deleteMcPaymentToken(url: String) {
        // Log.d("Sudhir","getmcPaymentInfo url::"+url)

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, response: Response) //= println(response.body()?.string())
            {
                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
                val Model = gson.fromJson(jsonData, McPaymentDetailPaymentResponse::class.java)
                //   Log.d("Sudhir","id::"+ Model.id)
                //   Log.d("Sudhir","register_id::"+ Model.register_id)
                //   Log.d("Sudhir","amount::"+ Model.amount)
                //   Log.d("Sudhir","description::"+ Model.description)
                //   Log.d("Sudhir","status::"+ Model.status)

            }
        })

    }


    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_OK)
        finish()
    }



    //retrieveKey first time for Payment flow
    private fun callRetrieveKeysAfterInitiateTransactionApi() = uiScope.launch {
        //        showDialog()
        Log.e("paymntAccountActivity","callRetrieveKeysAfterInitiateTransactionApi,1464")
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        val result = withContext(bgDispatcher) {
            loadRetrieveKeysDataAFterInitiateTransaction()
        }
//        hideDialog()
//        llProgressBar.visibility = View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if (result is Result.Success) {

            encRetResp = result.data
            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {

                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this@PaymentAccountActivity)))
                pubKeyArr?.let {
                    val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(encRetResp.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                    symmetricKey?.let {
                        val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(encRetResp.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
                        responseData.let {
                            val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
                            // Log.e("EncryptionKPayload", Gson().toJson(responseObj))
                            sessionKeyFromRetrieve = responseObj.sessionKey.toString()
                            bankKeyFromRetrieve = responseObj.publicKey.toString()
                            kiFromRetrieve = responseObj.bankKi.toString()

                            responseObj?.let {
                                val credFormat = responseObj.resetCredentialFormat
                                credType= CredType.REGISTER_ACCOUNT

                                EncyDecyAfterInitiateTransaction(resultretrieve)

                                Log.e("paymntAccountActivity","callRetrieveKeysAfterInitiateTransactionApi,1497")
//
                            }

                        }
                    }
                }

            } else {
                alert(resources.getString(R.string.spl_continue_trans), resources.getString(R.string.spl_device_failed)) {
                    okButton { sendError("A153") }
                }.show().setCancelable(false)
            }

        } else if (result is Result.Error) {
            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.internet_connec))
                sendError("A152")
            } else {
                showError(resources.getString(R.string.server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private suspend fun loadRetrieveKeysDataAFterInitiateTransaction(): Result<EncryptionKeyRetrievalResponse> {


        return try {
            val encKeyRetReq: EncryptionKeyRetrievalRequest = createAFterInitiateAccountEncryptionKeyRetrievalRequest(
                txnId = txnId,
                bic = bic,
                context = this,
                mobileNumber = mobileNumber,
                resetCredentialCall = false,
                symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                activity = this,
                appId = BuildConfig.APP_NAME,
                txnType = TransactionType.FINANCIAL_TXN
            )
            val response = apiService.createRetrieveKeysRequest(encKeyRetReq)
            resultretrieve = response.await()
//            Log.e("<<<Response>>>", Gson().toJson(response))

            txnId_g = txnId
            Result.Success(resultretrieve)


        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }


    }


    private fun EncyDecyAfterInitiateTransaction(result: EncryptionKeyRetrievalResponse){

        if(null != result) {
            val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
            pubKeyArr?.let {
                val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(result.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                symmetricKey?.let {
                    val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
                    responseData.let {
                        val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
                        // Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
                        responseObj?.let {
                            val ki = responseObj.bankKi
                            val credFormat = responseObj.resetCredentialFormat
                            val bankKey = responseObj.publicKey
                            val sessionKey = responseObj.sessionKey
                            if (null != bankKey && null != ki && null != sessionKey) {

                                if(bic=="VICNID"){

                                    bankKey_g = bankKey
                                    ki_g = ki

                                    sessionKey_g = sessionKey

                                    val intent = Intent(this, otpAuthActivity::class.java)

                                    startActivityForResult(intent,0)

                                }else{
                                    createCredSubmissionRequest(
                                        bankKey = bankKey, ki = ki, sessionKey = sessionKey , mpin = "", txnType = transactionType
                                    )}
                            }

                        }

                    }
                }
            }
        }
    }

    private fun createCredSubmissionRequest(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType) = uiScope.launch {
//         showDialog()
//        llProgressBar.visibility = View.VISIBLE
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


        val result = withContext(bgDispatcher) {

            createCredSubmissionReq(bankKey, ki, sessionKey, mpin, txnType)
        }


//        hideDialog()
//        llProgressBar.visibility = View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if (result is Result.Success) {
            //Return to sdk
            if(null != result.data) {
                setResult(Activity.RESULT_OK)

                Toast.makeText(this@PaymentAccountActivity, "Payment is Successful", Toast.LENGTH_LONG).show()

            }else{
                sendError("A251")
            }

        }else if(result is Result.Error){
            if(result.exception.cause is ConnectException){
                showError(resources.getString(com.indepay.umps.spl.R.string.spl_no_internet_connec),resources.getString(com.indepay.umps.spl.R.string.spl_internet_connec))
                sendError("A152")
            }else {
                showError(resources.getString(com.indepay.umps.spl.R.string.spl_server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private suspend fun createCredSubmissionReq(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType): Result<CredentialSubmissionResponse> {
        Log.e("inside ","1612 create credntial submissionReq")




        return try {
            val request: CredentialSubmissionRequest = createCredentialSubmissionAfterInititingAccountRequest(
                txnId = txnId,
                pspId = com.indepay.umps.spl.utils.getPspId(this),
                mobileNumber = getMobileNo(this),
                activity = this,
                bankKey = bankKey,
                ki = ki,
                mpin = mpin,
                sessionKey = sessionKey,
                transactionType = txnType,
                appId = appId
            )

            val response = apiService.createCredentialSubmissionRequest(request)
            val result = response.await()
//            callRetrieveKeysAfterauthorize()
            EncyDecyAfterauthorize(resultretrieve)

            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
        Log.e("inside ","1640 create credntial submissionReq")
    }

    private fun callRetrieveKeysAfterauthorize() = uiScope.launch {
        Log.e("inside ","1699 callRetrieveKeysDataAfterauthorize")

        //            showDialog()
        llProgressBar.visibility = View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        val result = withContext(bgDispatcher) {
            loadRetrieveKeysDataAfterauthorize()
        }
//        hideDialog()
        llProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if (result is Result.Success) {

            encRetResp = result.data
            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {

                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this@PaymentAccountActivity)))
                pubKeyArr?.let {
                    val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(encRetResp.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                    symmetricKey?.let {
                        val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(encRetResp.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
                        responseData.let {
                            val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
                            // Log.e("EncryptionKPayload", Gson().toJson(responseObj))
                            responseObj?.let {
                                val credFormat = responseObj.resetCredentialFormat
                                credType= CredType.REGISTER_ACCOUNT


                                EncyDecyAfterauthorize(resultretrieve)
//
                            }

                        }
                    }
                }

            } else {
//                alert(resources.getString(R.string.continue_trans), resources.getString(R.string.device_failed)) {
//                    okButton { sendError("A153") }
//                }.show().setCancelable(false)
            }

        } else if (result is Result.Error) {
            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.internet_connec))
                sendError("A152")
            } else {
                showError(resources.getString(R.string.server_error), result.toString())
                sendError("A153")
            }
        }
        Log.e("outside ","1699 callRetrieveKeysDataAfterauthoriz")
    }

    private suspend fun loadRetrieveKeysDataAfterauthorize(): Result<EncryptionKeyRetrievalResponse> {
        Log.e("inside","1700 loadRetrieveKeysDataAfterauthorize")
        return try {
            val encKeyRetReq: EncryptionKeyRetrievalRequest = createAfterauthorizeAccountEncryptionKeyRetrievalRequest(
                txnId = txnId,
                bic = bic,
                context = this,
                mobileNumber = getMobileNo(this),
                resetCredentialCall = false,
                symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                activity = this,
                appId = appId,
                txnType = TransactionType.FINANCIAL_TXN
            )
            val response = apiService.createRetrieveKeysRequest(encKeyRetReq)
            resultretrieve = response.await()
            Log.e("<<<Response>>>", Gson().toJson(response))

            Result.Success(resultretrieve)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }

        Log.e("outside","1726 loadRetrieveKeysDataAfterauthorize")
    }


    private fun EncyDecyAfterauthorize(result: EncryptionKeyRetrievalResponse){
        Log.e("inside","1731 EncyDecyAfterauthorize")
        if(null != result) {
            val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
            pubKeyArr?.let {
                val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(result.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                symmetricKey?.let {
                    val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
                    responseData.let {
                        val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
                        // Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
                        responseObj?.let {
                            val ki = responseObj.bankKi
                            val credFormat = responseObj.resetCredentialFormat
                            val bankKey = responseObj.publicKey
                            val sessionKey = responseObj.sessionKey
                            if (null != bankKey && null != ki && null != sessionKey) {
                                createCredSubmissionAfterAuthorizeRequest(
                                    bankKey = bankKey,
                                    ki = ki,
                                    sessionKey = sessionKey,
                                    mpin = "",
                                    txnType = transactionType
                                )
                            }

                        }

                    }
                }
            }
        }

        Log.e("outside","1777 EncyDecyAfterauthorize")
    }

    private fun createCredSubmissionAfterAuthorizeRequest(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType) = uiScope.launch {
//         showDialog()
//         llProgressBar.visibility = View.VISIBLE
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        val result = withContext(bgDispatcher) {

            if(bic!="VICNID"){
                createCredSubmissionReqAfterAuthorize(bankKey, ki, sessionKey, mpin, txnType)
            }else{

                val result = "Success"

                val successPayment = Intent("SuccessPayment")
                successPayment.putExtra("result", true)
                LocalBroadcastManager.getInstance(this@PaymentAccountActivity).sendBroadcast(successPayment)

                Result.Success(result)


            }







        }

//        hideDialog()
//        llProgressBar.visibility = View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        if (result is Result.Success) {
            //Return to sdk
            if(null != result.data) {
                setResult(Activity.RESULT_OK)
                // finish()
            }else{
                sendError("A251")
            }

        }else if(result is Result.Error){
            if(result.exception.cause is ConnectException){
                showError(resources.getString(com.indepay.umps.spl.R.string.spl_no_internet_connec),resources.getString(com.indepay.umps.spl.R.string.spl_internet_connec))
                sendError("A152")
            }else {
                showError(resources.getString(com.indepay.umps.spl.R.string.spl_server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private suspend fun createCredSubmissionReqAfterAuthorize(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType): Result<EncryptionFetchOtpRetrievalResponse> {

        return try {
            val request: EncryotionOtpFetchRetrievalRequest = createFetchOtpEncryptionKeyRetrievalRequest(
                txnId = txnId,
                bic = bic,
                context = this,
                mobileNumber = mobileNumber,
                action = "PURCHASE",
                symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                sessionKey = sessionKey,
                activity = this,
                appId = appId,
                txnType = TransactionType.FINANCIAL_TXN
            )

            val response = apiService.createfetchOtpChallengeRequest(request)



            val result = response.await()
            EncyDecyFetchOtp(result)
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            //    DeleteAccountApi()
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    private fun EncyDecyFetchOtp(result: EncryptionFetchOtpRetrievalResponse){

        if(null != result) {
            val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
            pubKeyArr?.let {
                val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(result.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                symmetricKey?.let {
                    val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.fetchOtpCodeResponsePayloadEnc?.toByteArray(), symmetricKey)
                    responseData.let {
                        val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), FetchOtpRetrievalResponsePayload::class.java)
                        // Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
                        responseObj?.let {
                            val ki = responseObj.bankKi
                            val bankKey = responseObj.publicKey
                            val sessionKey = responseObj.sessionKey
                            val otpExpiry = responseObj.otpExpiry
                            val otpChallengeCode = responseObj.otpChallengeCode
                            val referenceId = responseObj.referenceId
                            if (null != bankKey && null != ki && null != sessionKey) {
                                var maskedcardNumber: List<String>
                                if(selectedAccount!!.maskedAccountNumber !=null) {
//                                    Log.d("Sudhir", "Card Number ::" + selectedAccount!!.maskedAccountNumber)
                                    maskedcardNumber = selectedAccount!!.maskedAccountNumber!!.split("#").map { it -> it.trim() }
//                                    Log.d("Sudhir", "Card Number ::" + maskedcardNumber.get(1))
                                }else{
//                                    Log.d("Sudhir", "Card Number ::" + defaultselectedAccount!!.maskedAccountNumber)
                                    maskedcardNumber = defaultselectedAccount!!.maskedAccountNumber!!.split("#").map { it -> it.trim() }
//                                    Log.d("Sudhir", "Card Number ::" + maskedcardNumber.get(1))
                                }

                                startActivityForResult(intentFor<VerifyWithOTP>(VerifyWithOTP.BAKKEY to bankKeyFromRetrieve, VerifyWithOTP.SESSIONKEY to sessionKeyFromRetrieve,
                                    VerifyWithOTP.KI to kiFromRetrieve,VerifyWithOTP.MASKED_CARD_NUMBER to maskedcardNumber.get(1),
                                    VerifyWithOTP.BIC to bic, VerifyWithOTP.APP_ID to appId,
                                    VerifyWithOTP.MOBILE_NO to mobileNumber,VerifyWithOTP.CHECKBOOL to checkbool,
                                    VerifyWithOTP.TXN_ID to txnId, VerifyWithOTP.REFERENCE_ID to referenceId, VerifyWithOTP.OTP_EXPIRY to otpExpiry,
                                    VerifyWithOTP.OTP_CHALLENGE_CODE to otpChallengeCode).singleTop().clearTop(),
                                    REQ_CODE_TRANSACTION)

                            }

                        }

                    }
                }
            }
        }
    }




}

