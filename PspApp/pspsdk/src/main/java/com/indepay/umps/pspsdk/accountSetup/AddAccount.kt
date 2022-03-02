package com.indepay.umps.pspsdk.accountSetup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.annotation.Keep
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.models.CommonResponse
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import com.indepay.umps.pspsdk.transaction.payment.otpAuthActivity
import com.indepay.umps.pspsdk.utils.*
import com.indepay.umps.pspsdk.utils.getCurrentLocale
import com.indepay.umps.pspsdk.utils.getPspId
import com.indepay.umps.spl.models.*
import com.indepay.umps.spl.models.TransactionType
import com.indepay.umps.spl.utils.*
import com.indepay.umps.spl.utils.Base64
import com.indepay.umps.spl.utils.PKIEncryptionDecryptionUtils
import com.indepay.umps.spl.utils.Result
import com.indepay.umps.spl.utils.SplMessageUtils.ConfirmRegisterAccountRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createEncryptionKeyRetrievalRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createFetchOtpEncryptionKeyRetrievalRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createRegisterAccountRequest
import kotlinx.android.synthetic.main.account_added_successfully.*
import kotlinx.android.synthetic.main.activity_add_account.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*
import retrofit2.HttpException
import java.lang.System.currentTimeMillis
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.CharSequence
import kotlin.Int
import kotlin.Long
import kotlin.NumberFormatException
import kotlin.String
import kotlin.Throwable
import kotlin.apply
import kotlin.let
import kotlin.to
import kotlin.toString


class AddAccount : SdkBaseActivity()  {

    private val REQ_CODE_ADD_ACCOUNT = 10700


    private lateinit var dataIntent: Intent
    private lateinit var apiService: SplApiService
    private lateinit var encRetResp: EncryptionKeyRetrievalResponse
    private lateinit var appId: String
    private lateinit var transactionType: TransactionType

    private var symmetricKey_g: ByteArray = byteArrayOf()

    private var registeredName_g : String = ""

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
    private lateinit var resultretrieve: EncryptionKeyRetrievalResponse
    private var checkbool : Boolean = false
    val millisInFuture:Long = 1000 * 30 // 1000 milliseconds = 1 second
    private lateinit var amount:String
    private lateinit var order_id:String
    private lateinit var merchName:String
    private lateinit var remarks:String

    private lateinit var bankonfo: EcosystemBankResponse

    var otp:String = ""
    // Count down interval 1 second
    val countDownInterval:Long = 1000
    var isClicked=false
    //   private var sdkBaseActivity: SdkBaseActivity
    private val mappedAccounts: ArrayList<MappedAccount> = ArrayList()
    private lateinit var accountTokenId:String
    private lateinit var feeTaxRefId:String

    private lateinit var sessionKeyFromRetrieve :String

    private lateinit var bankKeyFromRetrieve :String
    private lateinit var kiFromRetrieve :String

    private val tempReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {



            var otp: String = intent?.getStringExtra("otp").toString()



            callRegisterAccountConfirmApi(
                symmetricKey = symmetricKey_g , sessionKey = sessionKeyFromRetrieve,
                registeredName = registeredName_g ,
                otp = otp

            )
            println(otp)


        }

    }

    @Keep
    companion object : SdkCommonMembers() {
        const val TXN_ID = "txn_id"
        const val BIC = "bic"
        const val APP_ID = "app_id"
        const val MOBILE_NO = "mobile_no"
        const val PSP_ID = "psp_id"
        const val TXN_TYPE = "TXN_TYPE"

        const val BANK_INFO = "bank_info"

        const val AMOUNT = "amount"
        const val PAYEE_NAME = "payee_name"
        const val BANK_NAME = "bank_name"
        const val ACCOUNT_NO = "account"
        const val NOTE = "note"
        const val _LOCALE ="_locale"

        @Keep  const val ORDER_ID = "order_id"
        @Keep  const val MERCHANT_NAME = "merchant_name"
        @Keep   const val REMARKS = "remarks"
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_account)

        LocalBroadcastManager.getInstance(this).registerReceiver(tempReceiver , IntentFilter("PaymentAccountActivity"));



        dataIntent = intent

        apiService = SplApiService.create(getSSLConfig(this))

        et_add_expiryMM.addTextChangedListener(getTextWatcherInstance(
            moveFrom = et_card_number, moveFocusTo = et_add_expiryMM, maxDigits = 2, maxNum = 12))
        et_add_expiryYY.addTextChangedListener(getTextWatcherInstance(
            moveFrom = et_add_expiryMM, moveFocusTo = et_add_expiryYY, maxDigits = 2, maxNum = 99))
        et_add_CVV.addTextChangedListener(getTextWatcherInstance(
            moveFrom = et_add_expiryYY, moveFocusTo = et_add_CVV, maxDigits = 4, maxNum = 9999))

        if (!TextUtils.isEmpty(dataIntent.getStringExtra(PSP_ID)) && getPspId(this) == dataIntent.getStringExtra(PSP_ID)) {

            if (TextUtils.isEmpty(dataIntent.getStringExtra(TXN_ID)) || TextUtils.isEmpty(dataIntent.getStringExtra(TXN_TYPE)) ||
                TextUtils.isEmpty(dataIntent.getStringExtra(BIC)) || TextUtils.isEmpty(dataIntent.getStringExtra(APP_ID)) ||
                TextUtils.isEmpty(dataIntent.getStringExtra(MOBILE_NO)) || TextUtils.isEmpty(dataIntent.getStringExtra(BIC))
            ) {
                Log.e("Error", "Data missing")
                sendError("A001")
            } else {

//                bankonfo = dataIntent.getByteArrayExtra(BANK_INFO) as EcosystemBankResponse
//                Log.d("Sudhir","bankInfo:"+bankonfo)
//                for(i in 0..bankonfo.data.size){
//
//                }

                txnId = dataIntent.getStringExtra(TXN_ID).toString()

                mobileNumber = dataIntent.getStringExtra(MOBILE_NO).toString()
                bic = dataIntent.getStringExtra(BIC).toString()
                appId = dataIntent.getStringExtra(APP_ID).toString()
                if(dataIntent.getStringExtra(AMOUNT)!=null) {
                    amount = dataIntent.getStringExtra(AMOUNT).toString()
                    order_id = intent.getStringExtra(VerifyWithOTP.ORDER_ID).toString()
                    remarks = intent.getStringExtra(VerifyWithOTP.REMARKS).toString()
                    merchName = intent.getStringExtra(VerifyWithOTP.MERCHANT_NAME).toString()
                }

//                Log.d("Sudhir","Amount:"+amount)
                fullName = "Sudhir"
                et_add_phone_number.setText(getMobileNo(this)).toString()
                configureAccount(bic)

                button_continue.setOnClickListener{

                    Log.d("Sudhir","Add Account Continue Clicked:")
//                    et_card_number.setText("6109000000140008").toString()
//                    et_add_expiryMM.setText("03").toString()
//                    et_add_expiryYY.setText("23").toString()
//                    et_add_CVV.setText("123").toString()

                    if (isValidate()) {
                        validateCardData()
                    }



                }

//                fetchAppToken { token->
//                    saveAccessToken(this,token)
//                }

                addllProgressBar.visibility = View.VISIBLE
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                callRetrieveKeysApi()


            }

        } else {
            //  sendError("A011")
        }

        back_add_arrowimage.setOnClickListener{
            onBackPressed()
        }

        switch_on_off.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // The switch is enabled/checked
                Log.d("Sudhir","Swith is ON")

            } else {
                Log.d("Sudhir","Swith is OFF")
            }
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
        addllProgressBar.visibility =View.VISIBLE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        val result = withContext(bgDispatcher) {
            loadRetrieveKeysData()
        }
        addllProgressBar.visibility =View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)


        if (result is Result.Success) {
            Log.d("COnnect New Acount","Sudhir callRetrieveKeysApi:Result success: ")

            encRetResp = result.data
            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {

                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this@AddAccount)))
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
    }

    private suspend fun loadRetrieveKeysData(): Result<EncryptionKeyRetrievalResponse> {
        Log.d("COnnect New Acount","Sudhir loadRetrieveKeysData:: ")

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


//    private suspend fun loadRetrieveKeysDataAFterInitiateTransaction(): Result<EncryptionKeyRetrievalResponse> {
//        Log.d("COnnect New Acount","Sudhir loadRetrieveKeysData:: ")
//
//        return try {
//            val encKeyRetReq: EncryptionKeyRetrievalRequest = createAFterInitiateAccountEncryptionKeyRetrievalRequest(
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
//            Log.e("<<<Response>>>", Gson().toJson(response))
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


//    private suspend fun createCredSubmissionReq(bankKey: String, ki: String, sessionKey: String, mpin: String, txnType: TransactionType): Result<CredentialSubmissionResponse> {
//        return try {
//            val request: CredentialSubmissionRequest = createCredentialSubmissionAfterInititingAccountRequest(
//                    txnId = txnId,
//                    pspId = com.indepay.umps.spl.utils.getPspId(this),
//                    mobileNumber = getMobileNo(this),
//                    activity = this,
//                    bankKey = bankKey,
//                    ki = ki,
//                    mpin = mpin,
//                    sessionKey = sessionKey,
//                    transactionType = txnType,
//                    appId = appId
//            )
//
//            val response = apiService.createCredentialSubmissionRequest(request)
//            val result = response.await()
//            Result.Success(result)
//
//        } catch (e: HttpException) {
//            // Catch http errors
//            Result.Error(e)
//        } catch (e: Throwable) {
//            Result.Error(e)
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
                txnType = TransactionType.FINANCIAL_TXN
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
                            Log.d("Connect New Acount", "Sudhir Card Digits::"+cardDigits)
//                            Log.d("Connect New Acount", "Sudhir accountTokenId::"+accountTokenId)

                            checkbool = true
                            if(dataIntent.getStringExtra(AMOUNT)!=null) {
                                startActivityForResult(intentFor<VerifyWithOTP>(VerifyWithOTP.BAKKEY to bankKeyFromRetrieve, VerifyWithOTP.SESSIONKEY to sessionKeyFromRetrieve,
                                    VerifyWithOTP.KI to kiFromRetrieve, VerifyWithOTP.MASKED_CARD_NUMBER to cardDigits,
                                    VerifyWithOTP.BIC to bic, VerifyWithOTP.APP_ID to appId, VerifyWithOTP.AMOUNT to amount,
                                    VerifyWithOTP.ORDER_ID to order_id, VerifyWithOTP.MERCHANT_NAME to merchName,
                                    VerifyWithOTP.REMARKS to remarks,
                                    VerifyWithOTP.MOBILE_NO to mobileNumber, VerifyWithOTP.CHECKBOOL to checkbool,
                                    VerifyWithOTP.TXN_ID to txnId, VerifyWithOTP.REFERENCE_ID to referenceId, VerifyWithOTP.OTP_EXPIRY to otpExpiry,
                                    VerifyWithOTP.OTP_CHALLENGE_CODE to otpChallengeCode).singleTop().clearTop(),
                                    REQ_CODE_ADD_ACCOUNT)
                            }else{

                                startActivityForResult(intentFor<VerifyWithOTP>(VerifyWithOTP.BAKKEY to bankKeyFromRetrieve, VerifyWithOTP.SESSIONKEY to sessionKeyFromRetrieve,
                                    VerifyWithOTP.KI to kiFromRetrieve, VerifyWithOTP.MASKED_CARD_NUMBER to cardDigits,
                                    VerifyWithOTP.BIC to bic, VerifyWithOTP.APP_ID to appId,
                                    VerifyWithOTP.MOBILE_NO to mobileNumber, VerifyWithOTP.CHECKBOOL to checkbool,
                                    VerifyWithOTP.TXN_ID to txnId, VerifyWithOTP.REFERENCE_ID to referenceId, VerifyWithOTP.OTP_EXPIRY to otpExpiry,
                                    VerifyWithOTP.OTP_CHALLENGE_CODE to otpChallengeCode).singleTop().clearTop(),
                                    REQ_CODE_ADD_ACCOUNT)


                            }

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
                        Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
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
                        Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))

                        responseObj?.let {
                            val registeredName = responseObj.registeredName
//                            val sessionKey = responseObj.sessionKey
                            if (null != registeredName) {
                                if(bic=="VICNID"){

                                    symmetricKey_g = symmetricKey
                                    registeredName_g = registeredName

                                    val intent = Intent(this, otpAuthActivity::class.java).apply{}

                                    startActivityForResult(intent,0)


                                }else{
                                    callRegisterAccountConfirmApi(
                                        symmetricKey = symmetricKey,
                                        sessionKey = sessionKeyFromRetrieve,
                                        registeredName = registeredName, otp = ""
                                    )}
                            }

                        }

                    }
                }
            }
        }else{

            Log.e("result","result is null here")
        }
    }



    private fun callRegisterAccountConfirmApi(
        registeredName: String,
        sessionKey: String, symmetricKey: ByteArray, otp: String) = uiScope.launch {
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



    private suspend fun loadAccountDetailsConirmData(registeredName: String, sessionKey: String,
                                                     symmetricKey: ByteArray,
                                                     mobileNumber: String,otp: String
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
                bankKey = bankKeyFromRetrieve,
                ki = kiFromRetrieve,
                otp = otp

            )

            val response = apiService.confirmRegisterCardDetailRequest(request)
            val result = response.await()

            Log.e("loadAccountDetail 591","Initiating OTP addacount.kt")
            if(bic=="VICNID")
            {
                TrackAccountDetails(result)

                Result.Success(result)

            }else{
                createCredSubmissionReqAfterAuthorize(sessionKey)
//
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
        Log.e("onSuccessAccDetailsFtch", "start")

        addllProgressBar.visibility = View.GONE
        //container_add_account.visibility = View.SHOW


        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        if (result is TrackAccountDetailsResponse) {



            finish()
        }else{

            Toast.makeText(applicationContext,"this is toast message",Toast.LENGTH_SHORT).show()

            val toast = Toast.makeText(applicationContext, result.toString(), Toast.LENGTH_SHORT)
            toast.show()

        }

//        if (result is TrackAccountDetailsResponse) {
//
//           MapAccountDetails(result)
//        }
        Log.e("onSuccessAccDetailsFtch", "end")

    }

    private fun MapAccountDetails(bankAccount: TrackAccountDetailsResponse){

        Log.e("MapAccountDetails", "start")

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
        Log.e("MapAccountDetails", "end")

    }

    private fun onSuccessmapAccDetails(result: CommonResponse) {
//        hideDialog()
        Log.e("onSuccessmapAccDetails", "start")

        addllProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        CustomerProfileDetailsApi()
        Log.e("onSuccessmapAccDetails", "end")

    }
//    private fun onErrormapAccDetails(result: CommonResponse) {
////        hideDialog()
//        llProgressBar.visibility = View.GONE
//        Log.d("Sudhir Mapping","Sudhir Mapping Error::"+result.errorCode)
//        Log.d("Sudhir Mapping","Sudhir Mapping Error::"+result.errorReason)
//      finish()
//    }


    private fun CustomerProfileDetailsApi() {
        //Log.e("CustomerProfileDetailsApi", "start")

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
        //Log.e("CustomerProfileDetailsApi", "end")

    }
    private fun onSuccesscustomerDetailsFetch(result: CommonResponse) {
//        hideDialog()
        //Log.e("onSuccesscustomerDetailsFetch", "start")

        addllProgressBar.visibility = View.GONE
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
            //commented of by chinmay just for coustomer account registration only not for gas negara
        }
        // Log.e("onSuccesscustomerDetailsFetch", "end")

    }


    private fun PreTransactionAPI(){
        Log.e("PreTransactionAPI", "start")
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
        Log.e("PreTransactionAPI", "end")

    }

    private fun onSuccessPreTransactionFetch(result: CommonResponse) {
//        hideDialog()
        //Log.e("onSuccessPreTransactionFetch", "start")
        addllProgressBar.visibility = View.GONE
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        if (result is PreTransactionResponse) {
            feeTaxRefId = result.feeTaxRefId.toString()

        }
        Log.d("ConnectNewAccount","Sudhir feeTaxRefId::"+feeTaxRefId)

        initiateTransaction()
        Log.e("onSuccessPreTranFetch", "end")

    }

    private fun initiateTransaction() {

        Log.e("initiateTransaction", "start")

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
        Log.e("initiateTransaction", "end")

    }




    private fun validateCardData() {
        if (
            !TextUtils.isEmpty(et_card_number.text.toString()) ||
            !TextUtils.isEmpty(et_add_expiryMM.text.toString()) || !TextUtils.isEmpty(et_add_expiryYY.text.toString()) ||
            !TextUtils.isEmpty(et_add_CVV.text.toString())||
            !TextUtils.isEmpty(et_account_number.text.toString())
        ) {
//            accountNumber = et_account_number.text.toString()
            accountNumber = et_account_number.text.toString()
            cardDigits = et_card_number.text.toString()
            cardExpiryMM = et_add_expiryMM.text.toString()
            cardExpiryYY = et_add_expiryYY.text.toString()

            cardPin = et_add_CVV.text.toString()


            Log.d("Validate","sudhir Validate Card")
            addllProgressBar.visibility = View.VISIBLE
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            EncyDecy(resultretrieve )
//            startActivityForResult<PinPadActivity>(100,
//                    PinPadActivity.IS_REQUIRED_CONFIRMATION to true,
//                    PinPadActivity.TXN_TYPE to TransactionType.REGISTER_ACC.name
//            )
            button_continue.isEnabled = false

        } else {
            alert {
                resources.getString(R.string.spl_enter_all_details)
                okButton {
                    sendError("A001")
                }
            }.show()
        }
    }

    private fun callRegisterAccountApi(bankKey: String, ki: String, sessionKey: String, symmetricKey: ByteArray) = uiScope.launch {
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
    String, symmetricKey: ByteArray, mobileNumber: String, accountNumber: String, cardDigits: String, cardExpiryMM: String,cardExpiryYY: String, cardCvv: String,fullName: String
    ): Result<RegisterCardDetailResponse> {
        Log.e("result reg card details","start load account data");
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
            Log.e("httpException","catch varifywithotp")
            Result.Error(e)


        } catch (e: Throwable) {

            Log.e("result reg card details","end of load account data");
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
            (TextUtils.isEmpty(et_card_number.text.toString()) || TextUtils.isEmpty(et_add_expiryMM.text.toString()) || TextUtils.isEmpty(et_add_expiryYY.text.toString()) || TextUtils.isEmpty(et_add_CVV.text.toString()) )&& TextUtils.isEmpty(et_account_number.text.toString())->{
                Snackbar.make(et_card_number, resources.getString(R.string.spl_enter_all_details), Snackbar.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }

    }
    fun configureAccount(bic : String ){

        if(bic=="BRINID"){


            card_holder_name_layout.setVisibility(View.GONE);
            phone_number_layout.setVisibility(View.GONE);
            lbl_phone_information.setVisibility(View.GONE);






        }else if(bic=="VICNID"){

            card_holder_name_layout.setVisibility(View.GONE);
            lbl_phone_information.setVisibility(View.GONE);
            phone_number_layout.setVisibility(View.GONE);
            lastsix_digit_card_number.setVisibility(View.GONE);
            et_card_number.setVisibility(View.GONE);
            expiry_cvv_layout.setVisibility(View.GONE);
            set_default_card_layout.setVisibility(View.GONE);



        }


    }

}