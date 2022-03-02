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
 *  * 12-12-2019     Mayank D   Fixed issue- RR-105
 *  *****************************************************************************
 */

package com.indepay.umps.pspsdk.baseActivity

import android.app.Activity
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.annotation.Keep
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.transaction.collectApprove.TxnCollectApproveActivity
import com.indepay.umps.pspsdk.utils.*
import com.indepay.umps.pspsdk.utils.Base64
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.jetbrains.anko.*
import retrofit2.HttpException
import java.net.ConnectException
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.nio.charset.StandardCharsets
import java.util.*


open class SdkBaseActivity : AppCompatActivity() {

//    val dialog = CustomProgressDialog()
    protected lateinit var dialog: ProgressDialog
    val uiScope = CoroutineScope(Dispatchers.Main)
    val bgDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val REQUEST_CODE_REGISTRATION = 10100
    internal var callDialog:Boolean = true
    private lateinit var error:String
    private val REQ_TYPE_COLLECT = "COLLECT"
    internal var localPspId:String? = null
    internal val THIS = this

    @Keep
    companion object : SdkCommonMembers() {

        lateinit var  paymentActivity: Activity;

        var complete: Boolean = false;

        fun fetchPaymentActivity() : Activity {
           return  paymentActivity;
        }

        fun  putPaymentActivity(pa : Activity) {
            paymentActivity = pa;
        }

        fun isComplete(): Boolean {

            return complete
        }

        fun setComplete()  {

            complete = true
        }

        const val IS_REGISTERED = "is_registered"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        FirebaseApp.initializeApp(this)
        setLocale(this)
        dialog = indeterminateProgressDialog(message = getString(R.string.request_loading_message), title = getString(R.string.request_loading_title)) {
            isIndeterminate = true
            setCancelable(false)
        }
        if (dialog.isShowing) {
            dialog.cancel()
        }
        localPspId = getPspId(this)
    }

    fun showDialog() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

     fun hideDialog() {
        if (dialog.isShowing) {
            dialog.cancel()
        }
    }

    internal fun setLocale (activity: AppCompatActivity) {
        val config = activity.resources.configuration;

        val lang = getCurrentLocale(this);

        val locale:Locale
        //If user has selected some other language update that
        if (lang != null && !lang.isEmpty()) {
            locale = Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            config.setLayoutDirection(locale);
            config.locale = locale;
            activity.resources.updateConfiguration(config, activity.resources.getDisplayMetrics());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                activity.createConfigurationContext(config);
            } else {
                activity.resources.updateConfiguration(config, activity.getResources().getDisplayMetrics());
            }
        }
        else locale =  Locale("en");//else keep the default language
        Locale.setDefault(locale);
        config.locale = locale;
        config.setLayoutDirection(locale);
        activity.resources.updateConfiguration(config, activity.resources.getDisplayMetrics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.createConfigurationContext(config);
        } else {
            activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
        }
    }

     fun showError(errorCode: String? = null, errorReason: String? = null) {

        val builder = StringBuilder(
                getString(R.string.server_error)
        )
        if (null != errorReason && !errorReason.trim().equals("")) {
            builder.append(" | ")
            builder.append(errorReason)
        }else{
            throw java.lang.Exception("Blank errorReason");
        }

        if (null != errorCode) {
            //builder.append(" | ")
            //builder.append(errorCode)
        }
        longToast(builder.toString()).show()
    }

    private fun getErrorMsg(errorCode: String): String {

        return try {
            getString(resources.getIdentifier(errorCode.trim(), "string", packageName))
        } catch (e: Resources.NotFoundException) {
            ""
        }

    }

    protected fun sendError(error_code: String) {
        val returnIntent = Intent()
        returnIntent.putExtra(ERROR_CODE, error_code)
        returnIntent.putExtra(ERROR_REASON, getErrorMsg(error_code))
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    internal fun callTxnTrackerApi(
            trackerApi: (SdkApiService) -> Deferred<TrackerResponse>,
            successCallback: (CommonResponse) -> Unit) {
        callApi(
                accessToken = getAccessToken(this),
                appName = getAppName(this),
                custPSPId = getPspId(this),
                apiToCall = trackerApi,
                successCallback = { data -> successCallback(data) },
                errorCallback = { data -> onTxnTrackingFail(data) }
        )

    }

    internal fun fetchAppToken(actionCallback: (token: String) -> Unit) = uiScope.launch {
//        if ((getUserTokenExpireTime(this@SdkBaseActivity) - System.currentTimeMillis()+60000) < DateUtils.MINUTE_IN_MILLIS) {
           // showDialog()

            val result = withContext(bgDispatcher) {
                loadAppTokenRequestDataSync()
            }

            //hideDialog()
//            if (result == null) {
//                showError(error)
//                finish()
//            } else {
                result?.let { respStr ->
                    val tokenRespStr = String(PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(respStr.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)
                    val accessToken = Gson().fromJson(tokenRespStr, AccessToken::class.java)
                    accessToken?.let { tkn ->
                        saveUserToken(this@SdkBaseActivity, tkn.token)
                        saveAccessToken(this@SdkBaseActivity, tkn.token)
                        saveLoginToken(this@SdkBaseActivity, tkn.token)
                        saveUserTokenIssueTime(this@SdkBaseActivity, tkn.issuedAtMillis)
                        saveUserTokenExpireTime(this@SdkBaseActivity, tkn.validTillMillis)
                        actionCallback(tkn.token)
                    }
                    Log.d("Login","Sudhir access token response::"+accessToken)
                }
//            }
//        }else {
//            actionCallback(getAccessToken(this@SdkBaseActivity))
//            Log.d("SdkBaseActivity","Sudhir else block::"+ getAccessToken(this@SdkBaseActivity))
//        }
    //    Log.d("Tokenexptime","Sudhir access token::"+getAccessToken(this@SdkBaseActivity))
    //    Log.d("Tokenexptime","Sudhir usertoken::"+ getUserToken(this@SdkBaseActivity))

    }

    internal suspend fun loadAppTokenRequestData(): String? {
        Log.d("Sudhir","Sudhir loadAppTokenRequestData")
        return try {

            val userToken = UserToken(
                    acquiringSource = AcquiringSource(
                            appName = BuildConfig.APP_NAME
                    )
            )
            val reqJson = Gson().toJson(userToken)
            val requestString = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(reqJson.toByteArray(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)

            val sslData = getPspSslConfig(this)
            val apiService = SdkApiService.create(sslData, "")
            val response = apiService.fetchAppTokenAsync(
                    ki = BuildConfig.MERCHANT_KI,
                    request = RequestBody.create(("text/plain").toMediaType(), requestString)
            )
            val result = response.await()
//            Log.e("<<<AppToken>>>", Gson().toJson(response))
            if (result.isSuccessful) {
                result.body()
            } else {
                val errCode = result.raw().code
                val errReason = result.raw().message
                error = errCode.toString() + " | " + errReason
                null
            }
        }
       catch (e: Throwable) {
           error = resources.getString(R.string.something_wrong)
            null
        }
    }
    internal fun loadAppTokenRequestDataSync(): String? {
        Log.d("Ajay","Sudhir loadAppTokenRequestDataSync")
        return try {

            val userToken = UserToken(
                    acquiringSource = AcquiringSource(
                            appName = BuildConfig.APP_NAME
                    )
            )
            val reqJson = Gson().toJson(userToken)
            val requestString = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(reqJson.toByteArray(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)

            val sslData = getPspSslConfig(this)
            val apiService = SdkApiService.create(sslData, "")
            val response = apiService.fetchAppTokenAsync(
                    ki = BuildConfig.MERCHANT_KI,
                    request = RequestBody.create(("text/plain").toMediaType(), requestString)
            )
            Thread.sleep(1000)
            val result = response.getCompleted()
//            Log.e("<<<AppToken>>>", Gson().toJson(response))
            if (result.isSuccessful) {
                val tokenRespStr = String(PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(result.body()?.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)
                val accessToken = Gson().fromJson(tokenRespStr, AccessToken::class.java)
                saveStringData(this, "accessToken", accessToken.token)
                result.body()//accessToken.token
            } else {
                val errCode = result.raw().code
                val errReason = result.raw().message
                error = errCode.toString() + " | " + errReason
                null
            }
        }

        catch (e: Throwable) {
            error = resources.getString(R.string.something_wrong)
            null
        }
    }


    internal fun callApi(
            accessToken: String, custPSPId: String?, appName: String,
            apiToCall: ((SdkApiService) -> Deferred<CommonResponse>)? = null,
            arrayApiToCall: ((SdkApiService) -> Deferred<ArrayList<out CommonResponse>>)? = null,
            successCallback: ((CommonResponse) -> Unit)? = null,
            successArrayCallback: ((ArrayList<out CommonResponse>) -> Unit)? = null,
            errorCallback: (CommonResponse) -> Unit
    ) = uiScope.launch {

       // showDialog()

//        if (custPSPId.isNullOrBlank() || (getAccessTokenExpireTime(this@SdkBaseActivity) - System.currentTimeMillis()) < DateUtils.MINUTE_IN_MILLIS) {

                //showError("","1.AccessToken:" + getStringData(THIS, "accessToken"))
                val refreshTokenResult = withContext(bgDispatcher) {
                    loadData(
                            apiToCall = { apiService ->
                                apiService.fetchAccessTokenAsync(
                                        TrackingRequest(
                                                custPSPId = custPSPId,
                                                accessToken = getStringData(THIS, "accessToken"),//accessToken,
                                                acquiringSource = AcquiringSource(
                                                        appName = appName
                                                )
                                        )
                                )
                            }
                    )
                }
                //showError("","2.AccessToken:" + getStringData(THIS, "accessToken"))
                if (refreshTokenResult is Result.Success && refreshTokenResult.data is AccessToken) {
                    saveAccessToken(this@SdkBaseActivity, refreshTokenResult.data.token.orEmpty())
                    saveAccessTokenIssueTime(this@SdkBaseActivity, refreshTokenResult.data.issuedAtMillis.orZero())
                    saveAccessTokenExpireTime(this@SdkBaseActivity, refreshTokenResult.data.validTillMillis.orZero())

                    executeApiCall(arrayApiToCall, apiToCall, successArrayCallback, successCallback, errorCallback)
                    //showError("","3.AccessToken:" + getStringData(THIS, "accessToken"))
                } else if (refreshTokenResult is Result.Error) {
                    if (refreshTokenResult.exception is ConnectException) {
                        showError(resources.getString(R.string.internet_connec),"DDD")
                        sendError("A152")
                    } else if (refreshTokenResult.exception is HttpException) {
                        val code = refreshTokenResult.exception.code()
                        if (code == HTTP_UNAUTHORIZED) {
                            showError(resources.getString(R.string.server_error) , code.toString())

//                        alert(resources.getString(R.string.login_again), resources.getString(R.string.timed_out)) {
//                            okButton { finish() }
//                            isCancelable = false
//                        }.show()
                        }
                    } else {
                        showError(resources.getString(R.string.server_error),refreshTokenResult.exception.message)
                        sendError("A153")
                    }
                }
//            } else {
//                executeApiCall(arrayApiToCall, apiToCall, successArrayCallback, successCallback, errorCallback)
//            }
    }

    private suspend fun executeApiCall(
            arrayApiToCall: ((SdkApiService) -> Deferred<ArrayList<out CommonResponse>>)?,
            apiToCall: ((SdkApiService) -> Deferred<CommonResponse>)?,
            successArrayCallback: ((ArrayList<out CommonResponse>) -> Unit)?,
            successCallback: ((CommonResponse) -> Unit)?,
            errorCallback: (CommonResponse) -> Unit) {

        val result = if (arrayApiToCall != null) {
            withContext(bgDispatcher) {
                loadArrayData(arrayApiToCall)
            }
        } else {
            withContext(bgDispatcher) {
                loadData(apiToCall!!)//must throw null
            }
        }

       // hideDialog()

        if (result is Result.Success) {
            if (successArrayCallback != null) {
                successArrayCallback(result.data as ArrayList<out CommonResponse>)
            } else {
                if (result.data is CommonResponse) {
                    if (result.data.success || (result.data.errorCode == null && result.data.errorReason == null)) {
                        successCallback?.invoke(result.data)
                    } else {
                        errorCallback(result.data)
                    }
                }
            }
        } else if (result is Result.Error) {
            if (result.exception is ConnectException) {
                showError(resources.getString(R.string.internet_connec))
                sendError("A152")
            } else if (result.exception is HttpException) {
                if (result.exception.code() == HTTP_NOT_FOUND) {
                    alert(resources.getString(R.string.send_invitation), resources.getString(R.string.mobile_not_registered)) {
                        okButton { }
                        cancelButton { }
                    }.show()
                } else {
                    showError(errorCode = result.exception.code().toString(), errorReason = "Other Error:"+result.exception.message())
                    sendError("A152")
                }
           }
            //else {
//                showError(resources.getString(R.string.server_error))
//                sendError("A153")
//            }
        }
    }

    private suspend fun loadData(apiToCall: (SdkApiService) -> Deferred<CommonResponse>): Result<CommonResponse> {
        return try {
            val sslData = getPspSslConfig(this)
            val apiService = SdkApiService.create(sslData, getUserName(this))
            val response = apiToCall(apiService)
            val result = response.await()
            Result.Success(result)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun loadArrayData(apiToCall: (SdkApiService) -> Deferred<ArrayList<out CommonResponse>>): Result<ArrayList<out CommonResponse>> {
        return try {
            val sslData = getPspSslConfig(this)
            val apiService = SdkApiService.create(sslData, getUserName(this@SdkBaseActivity))
            val response = apiToCall(apiService)
            val result = response.await()
            Result.Success(result)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    internal fun commonErrorCallback(data: CommonResponse) {
        hideDialog()
        val returnIntent = Intent()
        val errCode = data.errorCode.orEmpty()
        var errReason = data.errorReason.orEmpty()
        returnIntent.putExtra(ERROR_CODE, errCode)
        returnIntent.putExtra(ERROR_REASON, errReason)
        setResult(Activity.RESULT_CANCELED, returnIntent)
        if(errReason.isNullOrBlank()){
            errReason = errCode
        }
        alert(errReason, errReason) {
            okButton { finish() }
            isCancelable = false
        }.show()
    }

    internal fun createTxnTrackerRequest(txnId: String): TrackingRequest {
        return TrackingRequest(
                custPSPId = getPspId(this),
                accessToken = getAccessToken(this),
                transactionId = txnId,
                requestedLocale = getCurrentLocale(this),
                waitForCompletion = true,
                merchantId = BuildConfig.MERCHANT_ID,
                acquiringSource = AcquiringSource(
                        appName = getAppName(this),
                        mobileNumber = getMobileNo(this)
                )
        )
    }

    private fun onTxnTrackingFail(data: CommonResponse) {
        if (data is TrackerResponse) {
            val returnIntent = Intent()
            val errCode = data.errorCode.orEmpty()
            var errReason = data.errorReason.orEmpty()

            if (errCode.contains("AUTH_FAILURE")) {
                alert(resources.getString(R.string.invalid_mpin), resources.getString(R.string.tran_status)) {
                    okButton { finish() }
                    isCancelable = false
                }.show()
            } else {
                returnIntent.putExtra(ERROR_CODE, errCode)
                returnIntent.putExtra(ERROR_REASON, errReason)
                setResult(Activity.RESULT_CANCELED, returnIntent)
                if(errReason.isNullOrBlank())
                {
                    errReason = errCode
                }
                alert(errReason, resources.getString(R.string.tran_status)) {
                    okButton { finish() }
                    isCancelable = false
                }.show()
            }
        }
    }


    internal fun showRegistrationDialog(token: String) {
        setLocale(this)
        hideDialog()
            alert(message = resources.getString(R.string.register_user), title = resources.getString(R.string.register_user_title)) {
                yesButton {
                    startActivityForResult<RegistrationActivity>(REQUEST_CODE_REGISTRATION,
                            RegistrationActivity.USER_NAME to getStringData(this@SdkBaseActivity, RegistrationActivity.USER_NAME),
                            RegistrationActivity.APP_ID to BuildConfig.APP_NAME,
                            RegistrationActivity.USER_MOBILE to getMobileNo(this@SdkBaseActivity),
                            RegistrationActivity.USER_TOKEN to token)
                }
                noButton {
                    finish()
                }
                isCancelable = false
            }.show()

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

            val sslData = getPspSslConfig(this)
            val apiService = SdkApiService.create(sslData,"")

            Log.e("request",reqJson+" "+requestString + " "+sslData+" "+apiService)
            Log.e("requestString"," "+requestString)

            val response = apiService.fetchMerchantTokenAsync(
                    ki = BuildConfig.MERCHANT_KI,
                    request = RequestBody.create(("text/plain").toMediaType(), requestString)
            )

            val result = response.await()

            Log.e("<<<ResponseApi>>>",""+result.body())

            if (result.isSuccessful) {
                result.body()
            } else {
                Log.e("<<<ResponseFailure>>>", Gson().toJson(result))
                val errorCode = result.raw().code
                val errorReason = result.raw().message
                error = errorCode.toString() + " | " + errorReason
                null
            }
        } catch (e: Throwable) {
            Log.e("EXP",""+e)
            error = resources.getString(R.string.something_wrong)
            null
        }
    }




    internal fun callMerchantApi(
            merchantId: String,
            apiToCall: ((SdkApiService) -> Deferred<MerchantCollectResponse>)? = null,
            arrayApiToCall: ((SdkApiService) -> Deferred<ArrayList<out CommonResponse>>)? = null,
            successCallback: ((MerchantCollectResponse) -> Unit)? = null,
            successArrayCallback: ((ArrayList<out MerchantCollectResponse>) -> Unit)? = null,
            errorCallback: (MerchantCollectResponse) -> Unit
    ) = uiScope.launch {

       // showDialog()

       // if ((getUserTokenExpireTime(this@SdkBaseActivity) - System.currentTimeMillis()) < DateUtils.MINUTE_IN_MILLIS) {
            val result = withContext(bgDispatcher) {
                loadMerchantTokenRequestData(merchantId)
            }
        if(result == null)
        {
            finish()
        }else{
            result?.let { respStr ->
                val tokenRespStr = String(PKIEncryptionDecryptionUtils.decodeAndDecryptByAes(respStr.toByteArray(StandardCharsets.UTF_8), Base64.getDecoder().decode(BuildConfig.MERCHANT_KEY)), StandardCharsets.UTF_8)
                val accessToken = Gson().fromJson(tokenRespStr, AccessToken::class.java)
                accessToken?.let { tkn ->
                    saveStringData(this@SdkBaseActivity,TxnCollectApproveActivity.MRCH_TXN_ID,tkn.token)
                    executeMerchantApiCall(arrayApiToCall, apiToCall, successArrayCallback, successCallback, errorCallback)
                }
            }
        }
    }

        private suspend fun executeMerchantApiCall(
                arrayApiToCall: ((SdkApiService) -> Deferred<ArrayList<out CommonResponse>>)?,
                apiToCall: ((SdkApiService) -> Deferred<MerchantCollectResponse>)?,
                successArrayCallback: ((ArrayList<out MerchantCollectResponse>) -> Unit)?,
                successCallback: ((MerchantCollectResponse) -> Unit)?,
                errorCallback: (MerchantCollectResponse) -> Unit) {

            val result = if (arrayApiToCall != null) {
                withContext(bgDispatcher) {
                    loadArrayData(arrayApiToCall)
                }
            } else {
                withContext(bgDispatcher) {
                    loadMerchantData(apiToCall!!)//must throw null
                }
            }

            //.ialog()

            if (result is Result.Success) {
                if (successArrayCallback != null) {
                    successArrayCallback(result.data as ArrayList<out MerchantCollectResponse>)
                } else {
                    if (result.data is MerchantCollectResponse) {
                        if (result.data.success || (result.data.errorCode == null && result.data.errorReason == null)) {
                            successCallback?.invoke(result.data)
                        } else {
                            errorCallback(result.data)
                        }
                    }
                }
            } else if (result is Result.Error) {
                if (result.exception is ConnectException) {
                    showError(resources.getString(R.string.internet_connec))
                    sendError("A152")
                } else if (result.exception is HttpException) {
                    if (result.exception.code() == HTTP_NOT_FOUND) {
                        alert(resources.getString(R.string.send_invitation), resources.getString(R.string.mobile_not_registered)) {
                            okButton { }
                            cancelButton { }
                        }.show()
                    } else {
                        showError(errorCode = result.exception.code().toString(), errorReason = result.exception.message())
                        sendError("A152")
                    }
                } else {
                    showError(resources.getString(R.string.server_error))
                    sendError("A153")
                }
            }
        }

        private suspend fun loadMerchantData(apiToCall: (SdkApiService) -> Deferred<MerchantCollectResponse>): Result<MerchantCollectResponse> {
            return try {
                val sslData = getPspSslConfig(this)
                val apiService = SdkApiService.create(sslData, getUserName(this))
                val response = apiToCall(apiService)
                val result = response.await()
                Result.Success(result)
            } catch (e: HttpException) {
                Result.Error(e)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
}