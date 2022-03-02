package com.indepay.umps.pspsdk.accountSetup

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.annotation.Keep
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.gson.Gson
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.models.CommonResponse
import com.indepay.umps.pspsdk.utils.*
import com.indepay.umps.pspsdk.utils.getCurrentLocale
import com.indepay.umps.pspsdk.utils.getPspId
import com.indepay.umps.spl.models.*
import com.indepay.umps.spl.models.TransactionType
import com.indepay.umps.spl.pinpad.PinpadView
import com.indepay.umps.spl.utils.*
import com.indepay.umps.spl.utils.Base64
import com.indepay.umps.spl.utils.PKIEncryptionDecryptionUtils
import com.indepay.umps.spl.utils.Result
import com.indepay.umps.spl.utils.SplMessageUtils.createRefreshOtpEncryptionRequest
import com.indepay.umps.spl.utils.SplMessageUtils.createValidateOtpRetrievalRequest
import kotlinx.android.synthetic.main.otp_layout.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.*


class OtpVerificationActivity : SdkBaseActivity(), PinpadView.Callback, View.OnClickListener {
    private var context : Context? = null
    private var isConfirmationRequired: Boolean = false

    private lateinit var setResetMpinTxnId: String

    private lateinit var bic:String
    private lateinit var bankName:String

    private lateinit var dataIntent: Intent
    private lateinit var apiService: SplApiService
    private lateinit var encRetResp: EncryptionKeyRetrievalResponse
    private lateinit var appId: String
    private lateinit var transactionType: TransactionType
    private lateinit var resultretrieve: EncryptionFetchOtpRetrievalResponse


    //Intent data
    private lateinit var txnId: String
    private lateinit var mobileNumber: String
    private lateinit var credType: CredType
    private lateinit var referenceId: String
    private lateinit var otpExpiry: String
    private lateinit var otpChallengecode: String
    private lateinit var ki: String
    private lateinit var bankkey: String
    private lateinit var sessionkey: String
    private lateinit var accountTokenId: String
    private lateinit var otpEnteredText: String
    private lateinit var action1: String
    private lateinit var transactiontype : String
    private lateinit var otpphoneNumber: String
   private var countrycode =""
    private var checkbool : Boolean = false


    var START_MILLI_SECONDS = 60000L

    lateinit var countdown_timer: CountDownTimer
    var isRunning: Boolean = false;
    var time_in_milli_seconds = 0L


    @Keep
    companion object : SdkCommonMembers() {
        const val TXN_ID = "txn_id"
        const val BIC = "bic"
        const val APP_ID = "app_id"
        const val MOBILE_NO = "mobile_no"
        const val PSP_ID = "psp_id"
        const val TXN_TYPE = "TXN_TYPE"
        const val REFERENCE_ID = "referenceId"
        const val OTP_EXPIRY = "otpExpiry"
        const val OTP_CHALLENGE_CODE = "otpChallengeCode"
        const val BAKKEY = "bankKi"
        const val KI = "publicKey"
        const val SESSIONKEY = "sessionKey"
        const val ACCOUNT_TOKEN_ID = "accountTokenId"
        const val CHECKBOOL = "checkbool"

    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp_layout)
        Log.d("OTP Verification","Entered")

        dataIntent = intent

        txnId = dataIntent.getStringExtra(TXN_ID).toString()
        mobileNumber = getMobileNo(this)
        bic = dataIntent.getStringExtra(BIC).toString()
        appId = dataIntent.getStringExtra(APP_ID).toString()
        //referenceId = dataIntent.getStringExtra(REFERENCE_ID)//ajay
        otpExpiry= dataIntent.getStringExtra(OTP_EXPIRY).toString()
        otpChallengecode = dataIntent.getStringExtra(OTP_CHALLENGE_CODE).toString()
        bankkey = dataIntent.getStringExtra(BAKKEY).toString()
        ki = dataIntent.getStringExtra(KI).toString()
        sessionkey = dataIntent.getStringExtra(SESSIONKEY).toString()
        checkbool = dataIntent.getBooleanExtra(CHECKBOOL,false)
        if(checkbool == true) {
           action1 = "CARD_REGISTRATION"
            transactiontype = "REGISTER_CARD_ACC_DETAIL"
//            accountTokenId = dataIntent.getStringExtra(ACCOUNT_TOKEN_ID)
        }else{
            action1 = "PURCHASE"
            transactiontype = "FINANCIAL_TXN"
        }
     //   Log.d("Sudhir","Sudhir Check bool::"+checkbool)

        apiService = SplApiService.create(getSSLConfig(this))

        txt_otpchallengecode.text = "Otp Challenge Code : "+otpChallengecode
        txt_otp.setOnClickListener{
            txt_otp.isActivated = true
            view_otppinpad.visibility = View.VISIBLE
            button_continue.visibility = View.GONE
            view_otppinpad.viewProvider = txt_otp
        }

       // txt_otp.setText(otpChallengecode)

        val time  = otpExpiry.toInt()/60
        time_in_milli_seconds = time.toLong() *60000L
        startTimer(time_in_milli_seconds)

        val cardNumber: String = getMobileNo(this)
       val mask: String = cardNumber.replace("\\w(?=\\w{3})".toRegex(), "*")


        val manager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
       val CountryID = manager.simCountryIso.toUpperCase()

            if(CountryID=="IN"){
                countrycode = "+91"
            }else if(CountryID == "ID"){
                countrycode = "+62"
            }
            val mobnum = countrycode + mask
        //Log.d("Sudhir","sudhir ::"+mobnum)

        txt_entercodesent.setText("Enter verification code sent to "+mobnum)


        view_otppinpad.callback = this
        context = this


        back_arrowimage.setOnClickListener{
            onBackPressed()
        }

        txt_resend_otp.setOnClickListener {
            Log.d("OTP Verification","Resent OTP ::")
            callRefreshOtpOtp()

        }
        button_continue.setOnClickListener{

            otpEnteredText = txt_otp.text.toString()
            if(otpEnteredText.length>5) {
                callRetrieveKeysValidateOtp()
            }else{
                val myToast = Toast.makeText(applicationContext,"Please Enter Complete OTP",Toast.LENGTH_SHORT)
                myToast.setGravity(Gravity.LEFT,200,200)
                myToast.show()
            }
            Log.d("OTP Verification","Continue Clicked")
           // button_continue.isEnabled = false
        }

    }

    override fun onResume() {
        super.onResume()

    }

    private fun callRetrieveKeysValidateOtp() = uiScope.launch {
    //    Log.d("COnnect New Acount","Sudhir callRetrieveKeysAfterauthorize:: ")
        llProgressBar.visibility = View.VISIBLE

        val result = withContext(bgDispatcher) {
            loadRetrieveKeysDataValidateOtp()
        }
        llProgressBar.visibility = View.GONE

        if (result is Result.Success) {
        //    Log.d("COnnect New Acount","Sudhir Validate OTP : success: ")

            if(checkbool == true) {
                TrackAccountDetails(result)
//                setResult(Activity.RESULT_OK)
//                finish()
            }else {
                val intent = Intent()
                intent.putExtra("VALIDATE_MESSAGE", result.data.commonResponse!!.success)
                setResult(Activity.RESULT_OK,intent)
                finish()
            }

        } else if (result is Result.Error) {

            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            } else {
//                DeleteAccountApi()
                showError(resources.getString(R.string.server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private suspend fun loadRetrieveKeysDataValidateOtp(): Result<EncryptionFetchOtpRetrievalResponse> {
    //    Log.d("COnnect New Acount","Sudhir loadRetrieveKeysDataAfterauthorize:: ")

        return try {
            val encKeyRetReq: EncryptionValidateOtpRetrievalRequest = createValidateOtpRetrievalRequest(
                    txnId = txnId,
                    bic = bic,
                    referenceId = referenceId,
                    otp = otpEnteredText,
                    context = this,
                    bankKey = bankkey,
                    sessionKey = sessionkey,
                    ki = ki,
                    mobileNumber = getMobileNo(this),
                    action = action1,
                    symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                    activity = this,
                    appId = appId,
                    txnType = TransactionType.FINANCIAL_TXN
            )
            val response = apiService.createValidateOtpRequest(encKeyRetReq)
            resultretrieve = response.await()
            //  Log.e("<<<Response>>>", Gson().toJson(response))
            Result.Success(resultretrieve)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    fun TrackAccountDetails(result: Result<EncryptionFetchOtpRetrievalResponse>) {

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
                                            appName = getAppName(this),
                                            mobileNumber = getMobileNo(this)
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
            setResult(Activity.RESULT_OK)
                finish()

        }
    }






    private fun callRefreshOtpOtp() = uiScope.launch {
    //    Log.d("RefreshOtp","Sudhir Refresh Otp:: ")
        llProgressBar.visibility = View.VISIBLE

        val result = withContext(bgDispatcher) {
            createRefreshOtp()
        }
        llProgressBar.visibility = View.GONE

        if (result is Result.Success) {
         //   Log.d("RefreshOtp","Sudhir Refresh OTP : success: ")

            restarttimer()

        } else if (result is Result.Error) {

            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            } else {
//                DeleteAccountApi()
                showError(resources.getString(R.string.server_error), result.toString())
                sendError("A153")
            }
        }
    }


    private suspend fun createRefreshOtp(): Result<EncryptionFetchOtpRetrievalResponse> {



        return try {
            val request: EncryptionRefreshOtpRequest = createRefreshOtpEncryptionRequest(
                    txnId = txnId,
                    bic = bic,
                    referenceId = referenceId,
                    context = this,
                    mobileNumber = mobileNumber,
                    action = action1,
                    symmetricKey = PKIEncryptionDecryptionUtils.generateAes(),
                    activity = this,
                    appId = appId,
                    txnType = TransactionType.REGISTER_CARD_ACC_DETAIL
            )

            val response = apiService.createrefreshOtpRequest(request)
            val result = response.await()
            EncyDecyRefreshOtp(result)
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            //    DeleteAccountApi()
         //   Log.d("Error","Sudhir Error HttpException OTP")
            Result.Error(e)
        } catch (e: Throwable) {
         //   Log.d("Error","Sudhir Error Throwable OTP"+e)

            Result.Error(e)
        }
    }

    private fun EncyDecyRefreshOtp(result: EncryptionFetchOtpRetrievalResponse){

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
                        val otpExpiryres = responseObj.otpExpiry
                            val otpChallengeCoderes = responseObj.otpChallengeCode
                            val referenceIdres = responseObj.referenceId
                            val actionres = responseObj.action
                            val otpphoneres = responseObj.otpPhoneNumber
//                            if (null != bankKey && null != ki && null != sessionKey) {
//                                Log.d("Connect New Acount", "Sudhir AFter initite bankKey::"+bankKey)
//                                Log.d("Connect New Acount", "Sudhir AFter initite ki::"+ki)
//                                Log.d("Connect New Acount", "Sudhir AFter initite sessionKey::"+sessionKey)
                            Log.d("Connect New Acount", "Sudhir otpExpiry::"+otpExpiryres)
                            Log.d("Connect New Acount", "Sudhir otpChallengeCode::"+otpChallengeCoderes)
                            Log.d("Connect New Acount", "Sudhir referenceId::"+referenceIdres)

                            referenceId = referenceIdres!!.toString()
                            otpExpiry = otpExpiryres!!.toString()
                            otpChallengecode = otpChallengeCoderes!!.toString()
                            action1 = actionres.toString()
                            otpphoneNumber = otpphoneres!!.toString()

                            txt_otpchallengecode.text = "Otp Challenge Code : "+otpChallengecode

                            val cardNumber: String = getMobileNo(this)
                            val mask: String = cardNumber.replace("\\w(?=\\w{3})".toRegex(), "*")

                            val mobnum = countrycode + mask
                            //Log.d("Sudhir","sudhir ::"+mobnum)

                            txt_entercodesent.setText("Enter verification code sent to "+mobnum)


                        }

                    }
                }
            }
        }
    }


    private fun DeleteAccountApi(){
        Log.d("DeleteAccountApi","Sudhir DeleteAccountApi Start")
        var accountTokenIds: ArrayList<String>? = null
        val accountTokenIdsdata = accountTokenId

        accountTokenIds = ArrayList()
        accountTokenIds.add(accountTokenIdsdata)


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
//                                    transactionId = null,
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
        Log.d("onSuccessDeleteAccount","Sudhir Delete Success::")

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
//            finish()
        }

    }


    private fun startTimer(time_in_seconds: Long) {
        isRunning = true;
        countdown_timer = object : CountDownTimer(time_in_seconds, 1000) {
            override fun onFinish() {
                //loadConfeti()
                txt_otp_time.text = "OTP Expired "
            }

            override fun onTick(p0: Long) {
                time_in_milli_seconds = p0
                updateTextUI()
            }
        }
        countdown_timer.start()

    }



    private fun updateTextUI() {
        val minute = (time_in_milli_seconds / 1000) / 60
        val seconds = (time_in_milli_seconds / 1000) % 60

        txt_otp_time.text = "sent OTP will expired in : $minute:$seconds"
    }


    private fun restarttimer() {
        if (!isRunning) {
            isRunning = true;
            countdown_timer.start();
        } else {
            countdown_timer.cancel(); // cancel
            countdown_timer.start();  // then restart
        }
    }


    override fun onClick(p0: View?) {
        when(p0?.id){
            txt_otp.id -> {

            }
        }
    }

    override fun onPressSubmit(passcode: String) {
        view_otppinpad.visibility = View.GONE
        button_continue.visibility = View.VISIBLE
        Log.d("OtpActivity","OTP entered::"+txt_otp.text)
        if (isConfirmationRequired) {
            Log.d("OtpActivity","OTP entered::"+txt_otp.text)
            when {
                txt_otp.text.isEmpty().or(txt_otp.text.isBlank()) -> Toast.makeText(this, resources.getString(R.string.spl_pin_cannot_blank), Toast.LENGTH_SHORT).show()
            }
        } else {
            when {
                txt_otp.text.isEmpty().or(txt_otp.text.isBlank()) -> Toast.makeText(this, resources.getString(R.string.spl_pin_cannot_blank), Toast.LENGTH_SHORT).show()

                else -> {
//                    val returnIntent = Intent()
//                    returnIntent.putExtra(PinPadActivity.MPIN, txt_pin.text.toString())
//                    setResult(Activity.RESULT_OK, returnIntent)
//                    finish()
                }

            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

}
