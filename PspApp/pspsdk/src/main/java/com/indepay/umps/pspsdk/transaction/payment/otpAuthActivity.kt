package com.indepay.umps.pspsdk.transaction.payment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.annotation.Keep
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import com.indepay.umps.spl.models.CredType
import com.indepay.umps.spl.models.EncryptionFetchOtpRetrievalResponse
import com.indepay.umps.spl.models.EncryptionKeyRetrievalResponse
import com.indepay.umps.spl.models.TransactionType
import com.indepay.umps.spl.pinpad.PinpadView
import com.indepay.umps.spl.utils.SplApiService
import kotlinx.android.synthetic.main.otp_mpin_layout.*


class otpAuthActivity : SdkBaseActivity(), PinpadView.Callback, View.OnClickListener {
    var AUTH_OTP_REQUEST = 2211
    lateinit var otpEnteredText:String

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
    //private lateinit var otpChallengecode: String
    private lateinit var ki: String
    private lateinit var bankkey: String
    private lateinit var sessionkey: String
    private lateinit var accountTokenId: String
    //private lateinit var otpEnteredText: String
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
        //const val TXN_TYPE = "TXN_TYPE"
        const val REFERENCE_ID = "referenceId"
        const val OTP_EXPIRY = "otpExpiry"
        const val OTP_CHALLENGE_CODE = "otpChallengeCode"
        const val BAKKEY = "bankKi"
        //const val KI = "publicKey"
        const val SESSIONKEY = "sessionKey"
        const val ACCOUNT_TOKEN_ID = "accountTokenId"
        const val CHECKBOOL = "checkbool"
        const val BANK_KEY = "bankKey"
        const val KI = "ki"
        const val SESSION_KEY = "sessionKey"
        const val TXN_TYPE= "transactionType"
        const val AMOUNT = "amount"
        const val ORDER_ID = "orderId"

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp_mpin_layout)
        Log.d("OTP Verification","Entered")

        transactiontype = "FINANCIAL_TXN"

        txt_otp_mpin.setOnClickListener{
            txt_otp_mpin.isActivated = true
            view_otppinpad.visibility = View.VISIBLE
            button_continue_mpin.visibility = View.GONE
            view_otppinpad.viewProvider = txt_otp_mpin
        }

        view_otppinpad.callback = this
        context = this


        back_arrowimage.setOnClickListener{
            onBackPressed()
        }

        button_continue_mpin.setOnClickListener{

            otpEnteredText = txt_otp_mpin.text.toString()


            val stringToPassBack: String = otpEnteredText


            if(otpEnteredText.length==6){

                val intentotp = Intent("addAccount")
                intentotp.putExtra("otp", otpEnteredText)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentotp)

                val intentotp_payment = Intent("PaymentAccountActivity")
                intentotp_payment.putExtra("otp", otpEnteredText)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentotp_payment)

                finish()


            }else{


                Log.d("less characters","enter 6 digits")
            }

            Log.d("OTP Verification","Continue Clicked")



        }

    }

    override fun onResume() {
        super.onResume()

    }
    override fun onClick(p0: View?) {
        when(p0?.id){
            txt_otp_mpin.id -> {

            }
        }
    }

    override fun onPressSubmit(passcode: String) {




        view_otppinpad.visibility = View.GONE
        button_continue_mpin.visibility = View.VISIBLE
        Log.d("OtpActivity","OTP entered::"+txt_otp_mpin.text)




    }

    override fun onBackPressed() {
        super.onBackPressed()

    }

}