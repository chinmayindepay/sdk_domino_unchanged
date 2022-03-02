package com.indepay.umps.pspsdk.accountSetup


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Keep
import android.support.design.widget.Snackbar
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.google.gson.Gson
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.models.CommonResponse
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import com.indepay.umps.pspsdk.transaction.payment.otpAuthActivity
import com.indepay.umps.pspsdk.utils.*
import com.indepay.umps.pspsdk.utils.getAccessToken
import com.indepay.umps.pspsdk.utils.getAppName
import com.indepay.umps.pspsdk.utils.getCurrentLocale
import com.indepay.umps.pspsdk.utils.getMobileNo
import com.indepay.umps.pspsdk.utils.getPspId
import com.indepay.umps.pspsdk.utils.retrieveAccessToken
import com.indepay.umps.spl.models.*
import com.indepay.umps.spl.utils.*
import com.indepay.umps.spl.utils.Base64
import com.indepay.umps.spl.utils.Result
import com.indepay.umps.spl.utils.SplMessageUtils.ConfirmRegisterAccountRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createRegisterAccountRequest
import com.indepay.umps.spl.models.TransactionType
import com.indepay.umps.spl.utils.PKIEncryptionDecryptionUtils
import com.indepay.umps.spl.utils.SplMessageUtils.createAFterInitiateAccountEncryptionKeyRetrievalRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createCredentialSubmissionAfterInititingAccountRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createEncryptionKeyRetrievalRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createFetchOtpEncryptionKeyRetrievalRequest
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.connect_account_information.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*
import retrofit2.HttpException
import java.lang.System.currentTimeMillis
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.*



class ConnectNewAccountActivity : SdkBaseActivity() {
    private val SECOND_ACTIVITY_REQUEST_CODE = 0

    var otp:String = ""
    private lateinit var dataIntent: Intent
    private lateinit var apiService: SplApiService
    private lateinit var encRetResp: EncryptionKeyRetrievalResponse
    private lateinit var appId: String
    private lateinit var transactionType: TransactionType


    //Intent data
    private lateinit var txnId: String
    private lateinit var mobileNumber: String
    private lateinit var accountNumber: String
    private lateinit var cardDigits: String
    private  var cardPin: String=""
    private lateinit var credType: CredType
    private lateinit var cardExpiryMM: String
    private lateinit var cardExpiryYY: String
    private lateinit var fullName: String
    private lateinit var bic: String
    private lateinit var resultretrieve:EncryptionKeyRetrievalResponse
   private var checkbool : Boolean = false
    val millisInFuture:Long = 1000 * 30 // 1000 milliseconds = 1 second

    // Count down interval 1 second
    val countDownInterval:Long = 1000
    var isClicked=false
 //   private var sdkBaseActivity: SdkBaseActivity
      private val mappedAccounts: ArrayList<MappedAccount> = ArrayList()
    private lateinit var accountTokenId:String
    private lateinit var feeTaxRefId:String

    private val REQ_CODE_ADD_ACCOUNT = 10700

    private lateinit var sessionKeyFromRetrieve :String
    private lateinit var bankKeyFromRetrieve :String
    private lateinit var kiFromRetrieve :String


    @Keep
    companion object : SdkCommonMembers() {
        const val TXN_ID = "txn_id"
        const val BIC = "bic"
        const val APP_ID = "app_id"
        const val MOBILE_NO = "mobile_no"
        const val PSP_ID = "psp_id"
        const val TXN_TYPE = "TXN_TYPE"
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connect_account_information)

        dataIntent = intent

//        if(!dataIntent.getStringExtra(AuthenticateTxnActivity._LOCALE).isNullOrBlank()){
//            saveLocale(this,dataIntent.getStringExtra(AuthenticateTxnActivity._LOCALE))
//        }

        apiService = SplApiService.create(getSSLConfig(this))

        et_expiryMM.addTextChangedListener(getTextWatcherInstance(
                moveFrom = et_expiryMM, moveFocusTo = et_expiryYY, maxDigits = 2, maxNum = 12))
        et_expiryYY.addTextChangedListener(getTextWatcherInstance(
                moveFrom = et_expiryYY, moveFocusTo = et_CVV, maxDigits = 2, maxNum = 99))
        et_CVV.addTextChangedListener(getTextWatcherInstance(
                moveFrom = et_CVV, moveFocusTo = et_CVV, maxDigits = 3, maxNum = 999))

        if (!TextUtils.isEmpty(dataIntent.getStringExtra(PSP_ID)) && getPspId(this) == dataIntent.getStringExtra(PSP_ID)) {

            if (TextUtils.isEmpty(dataIntent.getStringExtra(TXN_ID)) || TextUtils.isEmpty(dataIntent.getStringExtra(TXN_TYPE)) ||
                    TextUtils.isEmpty(dataIntent.getStringExtra(BIC)) || TextUtils.isEmpty(dataIntent.getStringExtra(APP_ID)) ||
                    TextUtils.isEmpty(dataIntent.getStringExtra(MOBILE_NO)) || TextUtils.isEmpty(dataIntent.getStringExtra(BIC))
            ) {
                Log.e("Error", "Data missing")
                sendError("A001")
            } else {
//                try {
//                    transactionType = TransactionType.FINANCIAL_TXN
//                } catch (e: Exception) {
//                    sendError("A012")
//                }
                txnId = dataIntent.getStringExtra(TXN_ID).toString()
                mobileNumber = dataIntent.getStringExtra(MOBILE_NO).toString()
                bic = dataIntent.getStringExtra(BIC).toString()
                appId = dataIntent.getStringExtra(APP_ID).toString()

                fullName = "Sudhir"
//                et_account_number.setText("1010101010101010").toString()
//                et_card_number.setText("6109000000140008").toString()
//                et_expiryMM.setText("03").toString()
//                et_expiryYY.setText("23").toString()
//                et_CVV.setText("123").toString()



//                button_continue.setOnClickListener{
//
////                    Log.d("COnnect New Acount","Account number:: "+et_account_number.text)
//                    Log.d("COnnect New Acount","Card number:: "+et_card_number.text)
//                    Log.d("COnnect New Acount","Expiry Month:: "+et_expiryMM.text)
//                    Log.d("COnnect New Acount","Expiry Year:: "+et_expiryYY.text)
//                    Log.d("COnnect New Acount","CVV number:: "+et_CVV.text)
//
//                    if (isValidate()) {
//                        validateCardData()
//                    }
//                    // startActivity(intentFor<OtpVerificationActivity>("bankbic" to bankbic,"bankname" to bankname).singleTop().clearTop())
//
//
//                }
                fetchAppToken { token->
                    saveAccessToken(this,token)
                }

                llProgressBar.visibility = View.VISIBLE
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                callRetrieveKeysApi()


            }

        } else {
            //  sendError("A011")
        }


            back_arrowimage.setOnClickListener{
            onBackPressed()
        }

//        btn_verify.setOnClickListener{
//
//            Log.d("Connect New Account","Verify clicked")
//
//                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
//
//
//            account_holder_name.visibility = View.VISIBLE
//
//        }

    }

    private fun callRetrieveKeysApi() = uiScope.launch {

        Log.e("callRetrieveKeysApi","inside")

        llProgressBar.visibility =View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        val result = withContext(bgDispatcher) {
            loadRetrieveKeysData()
        }
        llProgressBar.visibility =View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)


        if (result is Result.Success) {
        //    Log.d("COnnect New Acount","Sudhir callRetrieveKeysApi:Result success: ")

            encRetResp = result.data
            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {

                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this@ConnectNewAccountActivity)))
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
        Log.e("callRetrieveKeysAPI","outside")

    }

    private suspend fun loadRetrieveKeysData(): Result<EncryptionKeyRetrievalResponse> {
//        Log.d("COnnect New Acount","Sudhir loadRetrieveKeysData:: ")
        Log.e("load_retrivekeydata","inside")
        return try {

            val encKeyRetReq: EncryptionKeyRetrievalRequest = createEncryptionKeyRetrievalRequest(
                    txnId = txnId,
                    bic = bic,
                    context = this,
                    mobileNumber = mobileNumber,
                    resetCredentialCall = true,
                    symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                    activity = this,
                    appId = appId,
                    txnType = TransactionType.REGISTER_CARD_ACC_DETAIL
            )
            val response = apiService.createRetrieveKeysRequest(encKeyRetReq)
             resultretrieve = response.await()
            Log.d("<<<Response>>>", Gson().toJson(response))
      //      Log.d("Connect New Acount","Sudhir Result Success::")

            Result.Success(resultretrieve)


        } catch (e: HttpException) {
            // Catch http errors
                Log.e("ConnectNewAccountActvty","catched httpexception")
            Result.Error(e)}
        catch (e: Throwable) {
            Log.e("ConnectNewAccountActvty","catched throwable")
           Result.Error(e)

        }
        Log.e("load_retrivekeydata","outside")
    }


//    private fun callRetrieveKeysAfterInitiateTransactionApi() = uiScope.launch {
//        Log.d("COnnect New Acount","Sudhir callRetrieveKeysAfterInitiateTransactionApi:: ")
//        llProgressBar.visibility =View.VISIBLE
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//        val result = withContext(bgDispatcher) {
//            loadRetrieveKeysDataAFterInitiateTransaction()
//        }
//        llProgressBar.visibility =View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//
//        if (result is Result.Success) {
//            Log.d("COnnect New Acount","Sudhir callRetrieveKeysApi:Result success: ")
//
//            encRetResp = result.data
//            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {
//
//                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this@ConnectNewAccountActivity)))
//                pubKeyArr?.let {
//                    val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(encRetResp.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
//                    symmetricKey?.let {
//                        val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(encRetResp.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
//                        responseData.let {
//                            val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
//                            // Log.e("EncryptionKPayload", Gson().toJson(responseObj))
//                            responseObj?.let {
//                                val credFormat = responseObj.resetCredentialFormat
//                                credType= CredType.REGISTER_ACCOUNT
//
//                                EncyDecyAfterInitiateTransaction(resultretrieve)
////
//                            }
//
//                        }
//                    }
//                }
//
//            } else {
//                alert(resources.getString(R.string.continue_trans), resources.getString(R.string.device_failed)) {
//                    okButton { sendError("A153") }
//                }.show().setCancelable(false)
//            }
//
//        } else if (result is Result.Error) {
//            if (result.exception.cause is ConnectException) {
//                showError(resources.getString(R.string.no_internet_connec), resources.getString(R.string.internet_connec))
//                sendError("A152")
//            } else {
//                showError(resources.getString(R.string.server_error), result.toString())
//                sendError("A153")
//            }
//        }
//    }

    private suspend fun loadRetrieveKeysDataAFterInitiateTransaction(): Result<EncryptionKeyRetrievalResponse> {
        Log.d("COnnect New Acount","Sudhir loadRetrieveKeysData:: ")

        return try {
            val encKeyRetReq: EncryptionKeyRetrievalRequest = createAFterInitiateAccountEncryptionKeyRetrievalRequest(
                    txnId = txnId,
                    bic = bic,
                    context = this,
                    mobileNumber = mobileNumber,
                    resetCredentialCall = false,
                    symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                    activity = this,
                    appId = appId,
                    txnType = TransactionType.FINANCIAL_TXN
            )
            val response = apiService.createRetrieveKeysRequest(encKeyRetReq)
            resultretrieve = response.await()
            Log.e("<<<Response>>>", Gson().toJson(response))
            Log.d("Connect New Acount","Sudhir Result Success::")

            Result.Success(resultretrieve)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }


//    private fun EncyDecyAfterInitiateTransaction(result: EncryptionKeyRetrievalResponse){
//
//        if(null != result) {
//            val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
//            pubKeyArr?.let {
//                val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(result.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
//                symmetricKey?.let {
//                    val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
//                    responseData.let {
//                        val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
//                        // Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
//                        responseObj?.let {
//                            val ki = responseObj.bankKi
//                            val credFormat = responseObj.resetCredentialFormat
//                            val bankKey = responseObj.publicKey
//                            val sessionKey = responseObj.sessionKey
//                            if (null != bankKey && null != ki && null != sessionKey) {
//                                Log.d("Connect New Acount", "Sudhir AFter initite bankKey::"+bankKey)
//                                Log.d("Connect New Acount", "Sudhir AFter initite ki::"+ki)
//                                Log.d("Connect New Acount", "Sudhir AFter initite sessionKey::"+sessionKey)
//                                Log.d("Connect New Acount", "Sudhir AFter initite credfprmat::"+credFormat)
//
//                                createCredSubmissionRequest(
//                                        bankKey = bankKey, ki = ki, sessionKey = sessionKey , mpin = cardPin, txnType = transactionType
//                                )
//                            }
//
//                        }
//
//                    }
//                }
//            }
//        }
//    }

//    private fun createCredSubmissionRequest(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType) = uiScope.launch {
//       // showDialog()
//        llProgressBar.visibility =View.VISIBLE
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//        val result = withContext(bgDispatcher) {
//            createCredSubmissionReq(bankKey, ki, sessionKey, mpin, txnType)
//        }
////        hideDialog()
//        llProgressBar.visibility =View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//
//
//        if (result is Result.Success) {
//            callRetrieveKeysAfterauthorize()
//
//            //Return to sdk
//            if(null != result.data) {
//
//                setResult(Activity.RESULT_OK)
//               // finish()
//            }else{
//                sendError("A251")
//            }
//
//        }else if(result is Result.Error){
//            if(result.exception.cause is ConnectException){
//                showError(resources.getString(com.indepay.umps.spl.R.string.no_internet_connec),resources.getString(com.indepay.umps.spl.R.string.internet_connec))
//                sendError("A152")
//            }else {
//                showError(resources.getString(com.indepay.umps.spl.R.string.server_error), result.toString())
//                sendError("A153")
//            }
//        }
//    }

    private suspend fun createCredSubmissionReq(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType): Result<CredentialSubmissionResponse> {
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
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

//    private fun callRetrieveKeysAfterauthorize() = uiScope.launch {
//        Log.d("COnnect New Acount","Sudhir callRetrieveKeysAfterauthorize:: ")
//        llProgressBar.visibility = View.VISIBLE
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//
//        val result = withContext(bgDispatcher) {
//            loadRetrieveKeysDataAfterauthorize()
//        }
//        llProgressBar.visibility = View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//
//        if (result is Result.Success) {
//            Log.d("COnnect New Acount","Sudhir callRetrieveKeysApi:loadRetrieveKeysDataAfterauthorize success: ")
//
//            encRetResp = result.data
//            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {
//
//                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this@ConnectNewAccountActivity)))
//                pubKeyArr?.let {
//                    val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(encRetResp.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
//                    symmetricKey?.let {
//                        val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(encRetResp.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
//                        responseData.let {
//                            val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
//                            // Log.e("EncryptionKPayload", Gson().toJson(responseObj))
//                            responseObj?.let {
//                                val credFormat = responseObj.resetCredentialFormat
//                                credType= CredType.REGISTER_ACCOUNT
//
//                                EncyDecyAfterauthorize(resultretrieve)
////
//                            }
//
//                        }
//                    }
//                }
//
//            } else {
////                alert(resources.getString(R.string.continue_trans), resources.getString(R.string.device_failed)) {
////                    okButton { sendError("A153") }
////                }.show().setCancelable(false)
//            }
//
//        } else if (result is Result.Error) {
//            if (result.exception.cause is ConnectException) {
//                showError(resources.getString(R.string.no_internet_connec), resources.getString(R.string.internet_connec))
//                sendError("A152")
//            } else {
//                showError(resources.getString(R.string.server_error), result.toString())
//                sendError("A153")
//            }
//        }
//    }
//
//    private suspend fun loadRetrieveKeysDataAfterauthorize(): Result<EncryptionKeyRetrievalResponse> {
//        Log.d("COnnect New Acount","Sudhir loadRetrieveKeysDataAfterauthorize:: ")
//
//        return try {
//            val encKeyRetReq: EncryptionKeyRetrievalRequest = createAfterauthorizeAccountEncryptionKeyRetrievalRequest(
//                    txnId = txnId,
//                    bic = bic,
//                    context = this,
//                    mobileNumber = mobileNumber,
//                    resetCredentialCall = false,
//                    symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
//                    activity = this,
//                    appId = appId,
//                    txnType = TransactionType.FINANCIAL_TXN
//            )
//            val response = apiService.createRetrieveKeysRequest(encKeyRetReq)
//            resultretrieve = response.await()
//          //  Log.e("<<<Response>>>", Gson().toJson(response))
//            Log.d("Connect New Acount","Sudhir Result Success::")
//
//            Result.Success(resultretrieve)
//
//        } catch (e: HttpException) {
//            // Catch http errors
//            Result.Error(e)
//        } catch (e: Throwable) {
//            Result.Error(e)
//        }
//    }


//    private fun EncyDecyAfterauthorize(result: EncryptionKeyRetrievalResponse){
//
//        if(null != result) {
//            val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
//            pubKeyArr?.let {
//                val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(result.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
//                symmetricKey?.let {
//                    val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
//                    responseData.let {
//                        val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
//                        // Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
//                        responseObj?.let {
//                            val ki = responseObj.bankKi
//                            val credFormat = responseObj.resetCredentialFormat
//                            val bankKey = responseObj.publicKey
//                            val sessionKey = responseObj.sessionKey
//                            if (null != bankKey && null != ki && null != sessionKey) {
//                                Log.d("Connect New Acount", "Sudhir AFter initite bankKey::"+bankKey)
//                                Log.d("Connect New Acount", "Sudhir AFter initite ki::"+ki)
//                                Log.d("Connect New Acount", "Sudhir AFter initite sessionKey::"+sessionKey)
//                                Log.d("Connect New Acount", "Sudhir AFter initite credfprmat::"+credFormat)
//                                createCredSubmissionAfterAuthorizeRequest(
//                                        bankKey = bankKey, ki = ki, sessionKey = sessionKey , mpin = cardPin, txnType = transactionType
//                                )
//
//                            }
//
//                        }
//
//                    }
//                }
//            }
//        }
//    }



//    private fun createCredSubmissionAfterAuthorizeRequest(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType) = uiScope.launch {
//        // showDialog()
//        val result = withContext(bgDispatcher) {
//            createCredSubmissionReqAfterAuthorize(bankKey, ki, sessionKey, mpin, txnType)
//        }
////        hideDialog()
//        llProgressBar.visibility =View.GONE
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//
//
//        if (result is Result.Success) {
//            //Return to sdk
//            if(null != result.data) {
//                setResult(Activity.RESULT_OK)
//                // finish()
//            }else{
//                sendError("A251")
//            }
//
//        }else if(result is Result.Error){
//            if(result.exception.cause is ConnectException){
//                showError(resources.getString(com.indepay.umps.spl.R.string.no_internet_connec),resources.getString(com.indepay.umps.spl.R.string.internet_connec))
//                sendError("A152")
//            }else {
//                showError(resources.getString(com.indepay.umps.spl.R.string.server_error), result.toString())
//                sendError("A153")
//            }
//        }
//    }

    private suspend fun createCredSubmissionReqAfterAuthorize(sessionKey: String): Result<EncryptionFetchOtpRetrievalResponse> {



                    return try {
            val request: EncryotionOtpFetchRetrievalRequest = createFetchOtpEncryptionKeyRetrievalRequest(
                    txnId = txnId,
                    bic = bic,
                    context = this,
                    mobileNumber = mobileNumber,
                    action = "CARD_REGISTRATION",
                    symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                    sessionKey = sessionKey,
                    activity = this,
                    appId = appId,
                    txnType = TransactionType.REGISTER_CARD_ACC_DETAIL
            )

            val response = apiService.createfetchOtpChallengeRequest(request)
            val result = response.await()
                        EncyDecyFetchOtp(result)
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
        //    DeleteAccountApi()
            Log.d("Error","Sudhir Error HttpException OTP")
            Result.Error(e)
        } catch (e: Throwable) {
            Log.d("Error","Sudhir Error Throwable OTP"+e)

            Result.Error(e)
        }
    }

    private fun EncyDecyFetchOtp(result: EncryptionFetchOtpRetrievalResponse){
        Log.e("EncyDecyFetchOtp","inside 677 connectnewaccount.kt")
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
//                            val ki = responseObj.bankKi
//                            val bankKey = responseObj.publicKey
//                            val sessionKey = responseObj.sessionKey
                            val otpExpiry = responseObj.otpExpiry
                            val otpChallengeCode = responseObj.otpChallengeCode
                            val referenceId = responseObj.referenceId
//                            if (null != bankKey && null != ki && null != sessionKey) {
//                                Log.d("Connect New Acount", "Sudhir AFter initite bankKey::"+bankKey)
//                                Log.d("Connect New Acount", "Sudhir AFter initite ki::"+ki)
//                                Log.d("Connect New Acount", "Sudhir AFter initite sessionKey::"+sessionKey)
                                Log.d("Connect New Acount", "Sudhir otpExpiry::"+otpExpiry)
                                Log.d("Connect New Acount", "Sudhir otpChallengeCode::"+otpChallengeCode)
                                Log.d("Connect New Acount", "Sudhir referenceId::"+referenceId)
                                checkbool = true
                                startActivityForResult(intentFor<OtpVerificationActivity>(OtpVerificationActivity.BAKKEY to bankKeyFromRetrieve, OtpVerificationActivity.SESSIONKEY to sessionKeyFromRetrieve,
                                        OtpVerificationActivity.KI to kiFromRetrieve,
                                        OtpVerificationActivity.BIC to bic,OtpVerificationActivity.APP_ID to appId,
                                        OtpVerificationActivity.MOBILE_NO to mobileNumber,OtpVerificationActivity.CHECKBOOL to checkbool,
                                        OtpVerificationActivity.TXN_ID to txnId, OtpVerificationActivity.REFERENCE_ID to referenceId,OtpVerificationActivity.OTP_EXPIRY to otpExpiry,
                                        OtpVerificationActivity.OTP_CHALLENGE_CODE to otpChallengeCode).singleTop().clearTop(),
                                        REQ_CODE_ADD_ACCOUNT)
                                finish()

//                            }

                        }

                    }
                }
            }
        }
    }




    private fun EncyDecy(result: EncryptionKeyRetrievalResponse){



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
                            Log.d("Connect New Acount", "Sudhir callSetCredentialApi::")
                            callRegisterAccountApi(
                                    bankKey = bankKey, ki = ki, symmetricKey = symmetricKey, sessionKey = sessionKey
                            )
                        }

                    }

                }
            }
        }
    }
}

    private fun EncyDecyNew(result: RegisterCardDetailResponse){

        if(null != result) {
            val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
            pubKeyArr?.let {
                val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(result.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                symmetricKey?.let {
                    val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.registerCardDetailResponsePayloadEnc?.toByteArray(), symmetricKey)
                    responseData.let {
                        val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), RegisterCardDetailResponsePayloadEnc::class.java)
                        // Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
                        responseObj?.let {
                            val registeredName = responseObj.registeredName
//                            val sessionKey = responseObj.sessionKey
                            if (null != registeredName) {
                                if(bic=="VICNID"){



                                    val intent = Intent(this, otpAuthActivity::class.java).apply{


                                    }
                                    startActivityForResult(intent,0)

                                    otp = intent.getStringExtra("keyName").toString()
                                    val textView = findViewById<TextView>(R.id.txt_otp_mpin)
                                    textView.setText(otp);





                                    callRegisterAccountConfirmApi(
                                        symmetricKey = symmetricKey,sessionKey = sessionKeyFromRetrieve,registeredName = registeredName ,otp = otp
                                    )

                                }else{
                                    callRegisterAccountConfirmApi(
                                        symmetricKey = symmetricKey,sessionKey = sessionKeyFromRetrieve,registeredName = registeredName, otp = ""
                                    )}
                            }

                        }

                    }
                }
            }
        }
    }



    private fun callRegisterAccountConfirmApi(registeredName: String,sessionKey: String, symmetricKey: ByteArray, otp: String) = uiScope.launch {
        val result = withContext(bgDispatcher) {
            loadAccountDetailsConirmData(
                    registeredName = registeredName,
                    sessionKey = sessionKey,
                    symmetricKey = symmetricKey,
                    mobileNumber = mobileNumber,
                    otp = otp

            )
        }

        if (result is Result.Success) {
            //Return to sdk
            setResult(Activity.RESULT_OK)
          //  finish()
        } else if (result is Result.Error) {
            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            } else {
                showError(resources.getString(R.string.server_error), result.toString())
                sendError("A153")
            }
        }
    }



    private suspend fun loadAccountDetailsConirmData(mobileNumber: String ,registeredName: String,sessionKey: String, symmetricKey: ByteArray, otp: String
    ): Result<ConfirmRegisterCardDetailResponse> {
        return try {
            val request: ConfirmRegisterCardDetailRequest = ConfirmRegisterAccountRequest(
                    txnId = txnId,
                    symmetricKey = symmetricKey,
                    registeredName = registeredName,
                    sessionKey = sessionKey,
                    appId = appId,
                    deviceId = getDeviceId(this),
                    imei1 = getImei1(this,this),
                    imei2 = getImei2(this,this),
                    mobileNumber = mobileNumber,
                    splId = getSplId(this),
                    pspId = com.indepay.umps.spl.utils.getPspId(this),
                    bic = bic,
                    otp = otp,
                    ki = kiFromRetrieve,
                    bankKey = bankKeyFromRetrieve


            )

            val response = apiService.confirmRegisterCardDetailRequest(request)
            val result = response.await()
            if(bic=="VICNID")
            {

                Result.Success(result)
            }else{
                createCredSubmissionReqAfterAuthorize(sessionKey)
//            TrackAccountDetails(result)
                Result.Success(result)

            }



        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

 fun TrackAccountDetails(result: ConfirmRegisterCardDetailResponse) {

    callApi(
            accessToken = retrieveAccessToken(this),
            custPSPId = getPspId(this),
            appName = getAppName(this),
            apiToCall = { sdkApiService ->
                sdkApiService.trackAccountDetailsApi(
                        TrackAccountDetailsRequest(
                                custPSPId = getPspId(this),
                                accessToken = getAccessToken(this),
                                transactionId = txnId,
                                acquiringSource = AcquiringSource(
                                        appName = getAppName(this)
                                ),

                                merchantId = null,
                                requestedLocale = Locale.getDefault().getLanguage()
                        )
                )
            },
            successCallback = { commonResponse -> onSuccessAccDetailsFetch(commonResponse) },
            errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
    )
}
    private fun onSuccessAccDetailsFetch(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        if (result is TrackAccountDetailsResponse) {

            MapAccountDetails(result)
        }
    }

    private fun MapAccountDetails(bankAccount: TrackAccountDetailsResponse){
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.mapAccountToProfileAsync(
                            MapAccountToProfileRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    transactionId = txnId,
                                    requestedLocale = getCurrentLocale(this),
                                    //paId = getPaId(this),
                                    fetchAccountDetailsAccountSeq = bankAccount.listOfMappedAccount!!.get(0).mappedAccountSeq
                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessmapAccDetails(commonResponse)},
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun onSuccessmapAccDetails(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        CustomerProfileDetailsApi()
    }
//    private fun onErrormapAccDetails(result: CommonResponse) {
////        hideDialog()
//        llProgressBar.visibility = View.GONE
//        Log.d("Sudhir Mapping","Sudhir Mapping Error::"+result.errorCode)
//        Log.d("Sudhir Mapping","Sudhir Mapping Error::"+result.errorReason)
//      finish()
//    }


    private fun CustomerProfileDetailsApi() {
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
                successCallback = { commonResponse -> onSuccesscustomerDetailsFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }
    private fun onSuccesscustomerDetailsFetch(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        if (result is CustomerProfileResponse && result.mappedBankAccounts != null) {

            accountTokenId = result.mappedBankAccounts[0].accountTokenId.toString()
            mappedAccounts.clear()
            mappedAccounts.addAll(result.mappedBankAccounts)
            if (mappedAccounts.isNotEmpty()) {

            }else {
                result.mappedBankAccounts.forEach {

                    if (it.isDefault) {
                        txt_bank_name.text = it.bankName
                        txt_account_no.text = it.maskedAccountNumber

                        txt_account_type.text = it.accountType
                        txt_mobile_no.text = getMobileNo(this)
                        //savePaAccountId(this, it.paAccountId.orEmpty())
                        // saveAccountTokenId(this, it.accountTokenId.orZero())
                        accountTokenId = it.accountTokenId.toString()

                        Log.d("connect New Activity", "Sudhir customer Api success" + accountTokenId)
                    }

                }
            }
                    Log.d("connect New Activity","Sudhir accountTokenId::"+accountTokenId)
                PreTransactionAPI()
            }
    }


    private fun PreTransactionAPI(){

        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.pretransactionrequest(
                            PreTransactionRequestApi(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            mobileNumber = getMobileNo(this),
                                            appName = getAppName(this)
                                    ),
                                    transactionId = null,
                                    merchantId = null,
                                    type = "PAY",
                                    amount = "100"

                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessPreTransactionFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    private fun onSuccessPreTransactionFetch(result: CommonResponse) {
//        hideDialog()
        llProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        if (result is PreTransactionResponse) {
            feeTaxRefId = result.feeTaxRefId.toString()

        }
            Log.d("ConnectNewAccount","Sudhir feeTaxRefId::"+feeTaxRefId)

        initiateTransaction()
    }

    private fun initiateTransaction() {
        var remarks =  "Card Validation"

        var payees: ArrayList<Payee>? = null
        var payer: Payer? = null
            val payee = Payee(
                    beneId = intent.getStringExtra(PaymentAccountActivity.BENE_ID),
                    //amount = amount, //intent.getStringExtra(AMOUNT),
                    amount = "100",
                    mobile = "8368951367" ,//"8368951368", //"8368951367"---Nilesh provided old number
                    targetSelfAccountId = null,
                    appId = getAppName(this)


            )
            payees = ArrayList()
            payees.add(payee)

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
                                            mobileNumber = getMobileNo(this),
                                            appName = getAppName(this)
                                    ),
                                    accessToken = getAccessToken(this),
                                    merchantTxnId = getMerchantTxnId(),
                                    type = "PAY", //intent.getStringExtra(TRANSACTION_TYPE),
                                    remarks =remarks,
                                    payees = payees,
                                    payer = null,
                                    timeTillExpireMins = null,
                                    initiatorAccountId = accountTokenId,
                                    feeTaxRefId = feeTaxRefId
                            )
                    )
                },
                successCallback = { commonResponse ->
                    if (commonResponse is TransactionResponse) {
                        txnId = commonResponse.transactionId.orEmpty()

                        Log.d("Initiate Transaction","Sudhir transaction ID::"+txnId)
                        Log.d("cratekeyretrievepayload","Sudhir initiateTransaction Timestamp::"+ currentTimeMillis())

//                        callRetrieveKeysAfterInitiateTransactionApi()


                    }
                },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }




    private fun validateCardData() {
        if (
                !TextUtils.isEmpty(et_card_number.text.toString()) ||
                !TextUtils.isEmpty(et_expiryMM.text.toString()) || !TextUtils.isEmpty(et_expiryYY.text.toString()) ||
                !TextUtils.isEmpty(et_CVV.text.toString())
        ) {
//            accountNumber = et_account_number.text.toString()
            accountNumber = "XXXXXX"
            cardDigits = et_card_number.text.toString()
            cardExpiryMM = et_expiryMM.text.toString()
            cardExpiryYY = et_expiryYY.text.toString()

            cardPin = et_CVV.text.toString()


            Log.d("Validate","sudhir Validate Card")
            llProgressBar.visibility = View.VISIBLE
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            EncyDecy(resultretrieve )
//            startActivityForResult<PinPadActivity>(100,
//                    PinPadActivity.IS_REQUIRED_CONFIRMATION to true,
//                    PinPadActivity.TXN_TYPE to TransactionType.REGISTER_ACC.name
//            )
//            button_continue.isEnabled = false

        } else {
            alert {
                resources.getString(R.string.spl_enter_all_details)
                okButton {
                    sendError("A001")
                }
            }.show()
        }
    }

    private fun callRegisterAccountApi(bankKey: String, ki: String, sessionKey:
    String, symmetricKey: ByteArray) = uiScope.launch {
        val result = withContext(bgDispatcher) {
            loadAccountDetailsData(
                    bankKey = bankKey,
                    ki = ki,
                    sessionKey = sessionKey,
                    symmetricKey = symmetricKey,
                    mobileNumber = mobileNumber,
                    accountNumber = accountNumber,
                    cardDigits = cardDigits,
                    cardCvv = cardPin,
                    cardExpiryMM = cardExpiryMM,
                    cardExpiryYY = cardExpiryYY,
                    fullName = fullName

            )
        }

        if (result is Result.Success) {
            //Return to sdk
            setResult(Activity.RESULT_OK)
           // finish()
        } else if (result is Result.Error) {
            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            } else {
                showError(resources.getString(R.string.server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private suspend fun loadAccountDetailsData(bankKey: String, ki: String, sessionKey:
    String, symmetricKey: ByteArray,
                                              mobileNumber: String, accountNumber: String, cardDigits: String, cardExpiryMM: String,cardExpiryYY: String, cardCvv: String,fullName: String
    ): Result<RegisterCardDetailResponse> {
        return try {
            val request: RegisterCardDetailRequest = createRegisterAccountRequest(
                    txnId = txnId,
                    symmetricKey = symmetricKey,
                    ki = ki,
                    bankKey = bankKey,
                    sessionKey = sessionKey,
                    appId = appId,
                    deviceId = getDeviceId(this),
                    imei1 = getImei1(this,this),
                    imei2 = getImei2(this,this),
                    mobileNumber = mobileNumber,
                    splId = getSplId(this),
                    pspId = com.indepay.umps.spl.utils.getPspId(this),
                    accountNumber = accountNumber,
                    cardDigits = cardDigits,
                    cardExpiryMM = cardExpiryMM,
                    cardExpiryYY = cardExpiryYY,
                    cardPin = cardCvv,
                    fullName = "Sudhir",
                    bic = bic
            )

            val response = apiService.registerCardDetailRequest(request)
            val result = response.await()
            EncyDecyNew(result)
           Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }



    private fun getTextWatcherInstance(moveFrom: TextView, moveFocusTo: TextView, maxDigits: Long, maxNum: Long): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if ((s.length) >= maxDigits) {
                    moveFrom.clearFocus()
                    moveFocusTo.requestFocus()
                    moveFocusTo.setCursorVisible(true)
                }
            }

            override fun afterTextChanged(s: Editable) {
                try {
                    if (Integer.parseInt(s.toString()) > maxNum) {
                        s.clear()
                        moveFrom.requestFocus()
                    }
                } catch (e: NumberFormatException) {
                    s.clear()
                    moveFrom.requestFocus()
                }

            }
        }

    }

    override fun onResume() {
        super.onResume()

    }

    private fun isValidate(): Boolean {
        when {
             TextUtils.isEmpty(et_card_number.text.toString()) || TextUtils.isEmpty(et_expiryMM.text.toString()) || TextUtils.isEmpty(et_expiryYY.text.toString()) || TextUtils.isEmpty(et_CVV.text.toString()) ->{
                Snackbar.make(et_card_number, resources.getString(R.string.spl_enter_all_details), Snackbar.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }

    }

}