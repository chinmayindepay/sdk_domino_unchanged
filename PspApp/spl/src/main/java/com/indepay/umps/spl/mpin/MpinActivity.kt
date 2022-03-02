package com.indepay.umps.spl.mpin

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.annotation.Keep
import android.support.design.widget.Snackbar
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.indepay.umps.spl.R
import com.indepay.umps.spl.activity.PinPadActivity
import com.indepay.umps.spl.activity.SplBaseActivity
import com.indepay.umps.spl.models.*
import com.indepay.umps.spl.transaction.AuthenticateTxnActivity
import com.indepay.umps.spl.utils.*
import com.indepay.umps.spl.utils.Base64
import com.indepay.umps.spl.utils.SplMessageUtils.createEncryptionKeyRetrievalRequest
import kotlinx.android.synthetic.main.activity_set_change_mpin.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.okButton
import org.jetbrains.anko.startActivityForResult
import retrofit2.HttpException
import java.net.ConnectException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit



class MpinActivity : SplBaseActivity() {

    private lateinit var dataIntent: Intent
    private lateinit var apiService: SplApiService
    private lateinit var encRetResp: EncryptionKeyRetrievalResponse
    private lateinit var appId: String
    private lateinit var transactionType: TransactionType

    //Intent data
    private lateinit var txnId: String
    private lateinit var mobileNumber: String
    private lateinit var cardDigits: String
    private  var cardPin: String=""
    private  var cardOtp: String=""
    private lateinit var credType: CredType
    private lateinit var cardExpiry: String
    private lateinit var bic: String

    val millisInFuture:Long = 1000 * 30 // 1000 milliseconds = 1 second

    // Count down interval 1 second
    val countDownInterval:Long = 1000
    var isClicked=false

    @Keep
    companion object : SplCommonMembers() {
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
        setContentView(R.layout.activity_set_change_mpin)

        dataIntent = intent

        if(!dataIntent.getStringExtra(AuthenticateTxnActivity._LOCALE).isNullOrBlank()){
            saveLocale(this, dataIntent.getStringExtra(AuthenticateTxnActivity._LOCALE).toString())
        }

        apiService = SplApiService.create(getSSLConfig(this))
        edtFirstTwo.addTextChangedListener(getTextWatcherInstance(
                moveFrom = edtFirstTwo, moveFocusTo = edtLastFour, maxDigits = 2, maxNum = 99))
        edtLastFour.addTextChangedListener(getTextWatcherInstance(
                moveFrom = edtLastFour, moveFocusTo = edtExpiryMM, maxDigits = 4, maxNum = 9999))
        edtExpiryMM.addTextChangedListener(getTextWatcherInstance(
                moveFrom = edtExpiryMM, moveFocusTo = edtExpiryYY, maxDigits = 2, maxNum = 12))
        edtExpiryYY.addTextChangedListener(getTextWatcherInstance(
                moveFrom = edtExpiryYY, moveFocusTo = edtCardPin, maxDigits = 2, maxNum = 99))
        edtCardPin.addTextChangedListener(getTextWatcherInstance(
                moveFrom = edtCardPin, moveFocusTo = edtCardPin, maxDigits = 6, maxNum = 999999))

        if (!TextUtils.isEmpty(dataIntent.getStringExtra(PSP_ID)) && getPspId(this) == dataIntent.getStringExtra(PSP_ID)) {
            progressBar.visibility = View.VISIBLE

            if (TextUtils.isEmpty(dataIntent.getStringExtra(TXN_ID)) || TextUtils.isEmpty(dataIntent.getStringExtra(TXN_TYPE)) ||
                    TextUtils.isEmpty(dataIntent.getStringExtra(BIC)) || TextUtils.isEmpty(dataIntent.getStringExtra(APP_ID)) ||
                    TextUtils.isEmpty(dataIntent.getStringExtra(MOBILE_NO)) || TextUtils.isEmpty(dataIntent.getStringExtra(BIC))
            ) {
                Log.e("Error", "Data missing")
                sendError("A001")
            } else {
                try {
                    transactionType = TransactionType.fromValue(dataIntent.getStringExtra(TXN_TYPE)!!)
                } catch (e: Exception) {
                    sendError("A012")
                }
                txnId = dataIntent.getStringExtra(TXN_ID)!!
                mobileNumber = dataIntent.getStringExtra(MOBILE_NO)!!
                bic = dataIntent.getStringExtra(BIC)!!
                appId = dataIntent.getStringExtra(APP_ID)!!
                button_submit.setOnClickListener {
                    if (isValidate()) {
                        validateCardData()
                    }
                }
                callRetrieveKeysApi()
            }

        } else {
          //  sendError("A011")
        }
        tvResendOtp.setOnClickListener {
            if(isClicked) {
                isClicked=false
                callResendOtpApi()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        button_submit.isEnabled = true
    }

    private fun callResendOtpApi()= uiScope.launch {
        progressBar.visibility = View.VISIBLE
        val result = withContext(bgDispatcher) {
            loadResendOtpKeysData()
        }
        if (result is Result.Success) {
            progressBar.visibility = View.GONE
            tvCountDown.visibility=View.VISIBLE
            tvResendOtp.visibility=View.GONE
            timer(millisInFuture,countDownInterval).start()

        } else if (result is Result.Error) {
            isClicked=true
            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            } else {
                showError(resources.getString(R.string.spl_server_error), result.toString())
                sendError("A153")
            }
        }

    }

    private fun callRetrieveKeysApi() = uiScope.launch {

        val result = withContext(bgDispatcher) {
            loadRetrieveKeysData()
        }
        if (result is Result.Success) {
            encRetResp = result.data
            if (null != encRetResp && encRetResp.commonResponse != null && encRetResp.commonResponse?.success == true) {
                llLayout.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this@MpinActivity)))
                pubKeyArr?.let {
                    val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(encRetResp.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                    symmetricKey?.let {
                        val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(encRetResp.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
                        responseData.let {
                            val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
                            //Log.e("EncryptionKPayload", Gson().toJson(responseObj))
                            responseObj?.let {
                                val credFormat = responseObj.resetCredentialFormat
                                if(credFormat!!.value.equals("OTP",ignoreCase = true)){
                                    tvCountDown.visibility=View.VISIBLE
                                    tvEnterPin.text=resources.getString(R.string.spl_otp)
                                    credType=CredType.OTP
                                    timer(millisInFuture,countDownInterval).start()
                                    isClicked=true
                                }else{
                                    tvEnterPin.text=resources.getString(R.string.spl_card_pin)
                                    credType=CredType.PIN
                                    tvCountDown.visibility=View.GONE
                                }
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
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            } else {
                showError(resources.getString(R.string.spl_server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private fun sendError(error_code: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ERROR_CODE, error_code)
        returnIntent.putExtra(ERROR_REASON, getErrorMessage(error_code))
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    private suspend fun loadRetrieveKeysData(): Result<EncryptionKeyRetrievalResponse> {

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
                    txnType = transactionType
            )
            val response = apiService.createRetrieveKeysRequest(encKeyRetReq)
            val result = response.await()
            Log.e("<<<Response>>>", Gson().toJson(response))
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }


    private suspend fun loadResendOtpKeysData(): Result<EncryptionKeyRetrievalResponse> {

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
                    txnType = transactionType
            )
            val response = apiService.resendOtpApi(encKeyRetReq)
            val result = response.await()
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    private fun validateCardData() {
        if (
                !TextUtils.isEmpty(edtFirstTwo.text.toString()) || !TextUtils.isEmpty(edtLastFour.text.toString()) ||
                !TextUtils.isEmpty(edtExpiryMM.text.toString()) || !TextUtils.isEmpty(edtExpiryYY.text.toString()) ||
                !TextUtils.isEmpty(edtCardPin.text.toString())
        ) {
            cardDigits = edtFirstTwo.text.toString() + edtLastFour.text.toString()
            cardExpiry = edtExpiryMM.text.toString() + edtExpiryYY.text.toString()
            if(credType.value.equals("OTP",ignoreCase = true)){
                cardOtp = edtCardPin.text.toString()
            }else{
                cardPin = edtCardPin.text.toString()
            }

            timer(millisInFuture,countDownInterval).cancel()
            startActivityForResult<PinPadActivity>(100,
                    PinPadActivity.IS_REQUIRED_CONFIRMATION to true,
                    PinPadActivity.TXN_TYPE to TransactionType.SET_PIN.name
            )
            button_submit.isEnabled = false
        } else {
            alert {
                resources.getString(R.string.spl_enter_all_details)
                okButton {
                    sendError("A001")
                }
            }.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK == resultCode && null != data) {
            val txtPin = data.getStringExtra(PinPadActivity.MPIN)
            if (null != encRetResp) {
                llLayout.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                button_submit.isEnabled = false
                val pubKeyArr = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(getSplKey(this)))
                pubKeyArr?.let {
                    val symmetricKey = PKIEncryptionDecryptionUtils.decodeAndDecrypt(encRetResp.commonResponse?.symmetricKey?.toByteArray(), pubKeyArr)
                    symmetricKey?.let {
                        val responseData = PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(encRetResp.encryptionKeyRetrievalResponsePayloadEnc?.toByteArray(), symmetricKey)
                        responseData.let {
                            val responseObj = Gson().fromJson(responseData.toString(StandardCharsets.UTF_8), EncryptionKeyRetrievalResponsePayload::class.java)
                            Log.e("EncryptionKeyPayload", Gson().toJson(responseObj))
                            responseObj?.let {
                                val ki = responseObj.bankKi
                                val credFormat = responseObj.resetCredentialFormat
                                val bankKey = responseObj.publicKey
                                val sessionKey = responseObj.sessionKey
                                if (null != bankKey && null != ki && null != sessionKey) {
                                    callSetCredentialApi(
                                            bankKey = bankKey, ki = ki, symmetricKey = symmetricKey, sessionKey = sessionKey, mpin = txtPin.toString()
                                    )
                                }

                            }

                        }
                    }
                }
            } else {
                sendError("A154")
            }
        }

    }

    private fun callSetCredentialApi(bankKey: String, ki: String, mpin: String, sessionKey:
    String, symmetricKey: ByteArray) = uiScope.launch {
        val result = withContext(bgDispatcher) {
            loadSetCredentialData(
                    bankKey = bankKey,
                    ki = ki,
                    sessionKey = sessionKey,
                    mpin = mpin,
                    symmetricKey = symmetricKey,
                    mobileNumber = mobileNumber,
                    cardDigits = cardDigits,
                    cardPin = cardPin,
                    cardExpiry = cardExpiry,
                    cardOtp = cardOtp
            )
        }

        if (result is Result.Success) {
            //Return to sdk
            progressBar.visibility = View.GONE
            setResult(Activity.RESULT_OK)
            finish()
        } else if (result is Result.Error) {
            progressBar.visibility = View.GONE
            if (result.exception.cause is ConnectException) {
                showError(resources.getString(R.string.spl_no_internet_connec), resources.getString(R.string.spl_internet_connec))
                sendError("A152")
            } else {
                showError(resources.getString(R.string.spl_server_error), result.toString())
                sendError("A153")
            }
        }
    }

    private suspend fun loadSetCredentialData(bankKey: String, ki: String, sessionKey:
    String, mpin: String, symmetricKey: ByteArray,
                                              mobileNumber: String, cardDigits: String, cardExpiry: String, cardPin: String,cardOtp:String
    ): Result<ResetCredentialResponse> {
        return try {
            val request: ResetCredentialRequest = SplMessageUtils.createSetCredentialRequest(
                    txnId = txnId,
                    symmetricKey = symmetricKey,
                    ki = ki,
                    bankKey = bankKey,
                    sessionKey = sessionKey,
                    appId = appId,
                    deviceId = getDeviceId(this),
                    imei1 = getImei1(this,this@MpinActivity),
                    imei2 = getImei2(this,this@MpinActivity),
                    mobileNumber = mobileNumber,
                    splId = getSplId(this),
                    pspId = getPspId(this),
                    cardDigits = cardDigits,
                    cardExpiry = cardExpiry,
                    cardPin = cardPin,
                    mpin = mpin,
                    bic = bic,
                    credType = credType,
                    cardOtp = cardOtp
            )

            val response = apiService.resetCredentialRequest(request)
            val result = response.await()
            Result.Success(result)

        } catch (e: HttpException) {
            // Catch http errors
            Result.Error(e)
        } catch (e: Throwable) {
            Result.Error(e)
        }
    }

    private fun getTextWatcherInstance(moveFrom: TextView, moveFocusTo: TextView, maxDigits: Int, maxNum: Int): TextWatcher {
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


    private fun timer(millisInFuture:Long,countDownInterval:Long): CountDownTimer {
        return object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val timeRemaining = timeString(millisUntilFinished)
                tvCountDown.text = timeRemaining
            }

            override fun onFinish() {
                tvCountDown.visibility=View.GONE
                tvResendOtp.visibility=View.VISIBLE
                isClicked=true

            }
        }
    }

    private fun timeString(millisUntilFinished:Long):String{
        var millisUntilFinished:Long = millisUntilFinished

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        millisUntilFinished -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)

        // Format the string
        return String.format(
                Locale.getDefault(),
                "%02d : %02d",
                minutes,seconds
        )
    }

    private fun isValidate(): Boolean {
        when {
            TextUtils.isEmpty(edtFirstTwo.text.toString()) || TextUtils.isEmpty(edtLastFour.text.toString()) || TextUtils.isEmpty(edtExpiryMM.text.toString()) || TextUtils.isEmpty(edtExpiryYY.text.toString()) || TextUtils.isEmpty(edtCardPin.text.toString()) ->{
                Snackbar.make(edtFirstTwo, resources.getString(R.string.spl_enter_all_details), Snackbar.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }

    }

}

