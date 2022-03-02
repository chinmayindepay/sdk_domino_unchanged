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

package com.indepay.umps.pspsdk.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.annotation.Keep
import android.text.TextUtils
import android.text.format.DateUtils
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val PSP_ID = "psp_id"
private const val MOBILE_NO = "mobile_no"
private const val PA_ACCOUNT_ID = "pa_account_id"
private const val PA_ID = "pa_id"
private const val PAYMENT_ADDRESS = "payment_address"
private const val APP_NAME = "app_name"
private const val USER_TOKEN_ISSUE_TIME = "user_token_issue_time"
private const val USER_TOKEN_EXPIRE_TIME = "user_token_expire_time"
private const val USER_TOKEN = "user_token"


private const val ACCESS_TOKEN_ISSUE_TIME = "access_token_issue_time"
private const val ACCESS_TOKEN_EXPIRE_TIME = "access_token_expire_time"
private const val ACCESS_TOKEN = "access_token"
private const val BIC_MAPPING_TO_BANK = "bicMappingToBank"
private const val USER_NAME = "user_name"
private const val ACCOUNT_TOKEN_ID = "account_token_id"
private const val _LOCALE = "locale"


internal sealed class Result<out T : Any> {

    class Success<out T : Any>(val data: T) : Result<T>()
    class Error(val exception: Throwable) : Result<Nothing>()
}

internal data class SslConfig(
        val socketFactory: SSLSocketFactory,
        val trustManager: X509TrustManager
)


/**
 * This method is a part of SDK, called to get the SSL certificate for communication.
 *
 * @param
 * @return
 */

@Throws(CertificateException::class, IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
@Keep
internal fun getPspSslConfig(context: Context): SslConfig {

    // Loading CAs from an InputStream
    var cf: CertificateFactory? = null
    cf = CertificateFactory.getInstance("X.509")

    var ca: Certificate? = null

    //For Private network
    //context.applicationContext.resources.openRawResource(R.raw.bank_psp_tls).use { cert -> ca = cf!!.generateCertificate(cert) }
    //For Public network
//    context.applicationContext.resources.openRawResource(R.raw.star_indepay_com).use { cert -> ca = cf!!.generateCertificate(cert) }
    context.applicationContext.resources.openRawResource(R.raw.common_crt).use { cert -> ca = cf!!.generateCertificate(cert) }

    // Creating a KeyStore containing our trusted CAs
    val keyStoreType = KeyStore.getDefaultType()
    val keyStore = KeyStore.getInstance(keyStoreType)
    keyStore.load(null, null)
    keyStore.setCertificateEntry("ca", ca)

    // Creating a TrustManager that trusts the CAs in our KeyStore.
    val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
    val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
    tmf.init(keyStore)

    // Creating an SSLSocketFactory that uses our TrustManager
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, tmf.trustManagers, null)

    return SslConfig(sslContext.socketFactory, tmf.trustManagers[0] as X509TrustManager)
}

@Keep
internal fun saveAppName(context: Context, appName: String) {
    saveStringData(context, APP_NAME, appName)
}

@Keep
internal fun getAppName(context: Context): String {
    return getStringData(context, APP_NAME)
}

@Keep
internal fun saveStringData(context: Context, prefKey: String?, data: String?) {
    val sharedPref = getDefaultSharedPreferences(context)
            ?: return
    with(sharedPref.edit()) {
        putString(prefKey, data)
        apply()
    }
}

@Keep
internal fun getStringData(context: Context, prefKey: String?): String {
    val sharedPref = getDefaultSharedPreferences(context)
    return sharedPref.getString(prefKey, "").toString()
}

@Keep
internal fun saveLongData(context: Context, prefKey: String?, data: Long) {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.sdk_shared_pref), Context.MODE_PRIVATE)
            ?: return
    with(sharedPref.edit()) {
        putLong(prefKey, data)
        apply()
    }
}

@Keep
internal fun getLongData(context: Context, prefKey: String?): Long {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.sdk_shared_pref), Context.MODE_PRIVATE)
    return sharedPref.getLong(prefKey, 0)
}

@Keep
internal fun getMerchantTxnId(): String {
    return UUID.randomUUID().toString()
}

@Keep
internal fun getPspId(context: Context): String? {
    return getStringData(context, PSP_ID).orNull()
}

@Keep
internal fun getPaAccountId(activity: Activity): String {
    return getStringData(activity, PA_ACCOUNT_ID)
}

@Keep
internal fun getPaId(activity: Activity): String {
    return getStringData(activity, PA_ID)
}

@Keep
internal fun savePspId(activity: Activity, pspId: String) {
    saveStringData(activity, PSP_ID, pspId)
}

@Keep
internal fun saveLocale(activity: Activity, locale: String) {
    saveStringData(activity, _LOCALE, locale)
}

@Keep
internal fun savePaAccountId(activity: Activity, paAccountId: String) {
    saveStringData(activity, PA_ACCOUNT_ID, paAccountId)
}

@Keep
internal fun savePaId(activity: Activity, paId: String) {
    saveStringData(activity, PA_ID, paId)
}

@Keep
internal fun saveMobileNo(activity: Activity, mobileNo: String) {
    saveStringData(activity, MOBILE_NO, mobileNo)
}

@Keep
internal fun getMobileNo(activity: Activity): String {
    return getStringData(activity, MOBILE_NO)
}

@Keep
internal fun savePaymentAddress(activity: Activity, pa: String) {
    saveStringData(activity, PAYMENT_ADDRESS, pa)
}

@Keep
internal fun getPaymentAddress(activity: Activity): String {
    return getStringData(activity, PAYMENT_ADDRESS)
}

@Keep
internal fun saveAccessToken(context: Context, accessToken: String) {
    saveStringData(context, ACCESS_TOKEN, accessToken)
}

@Keep
internal fun getAccessToken(context: Context): String {
    return getStringData(context, ACCESS_TOKEN)
}

@Keep
internal fun saveAccessTokenIssueTime(context: Context, issueTime: Long) {
    saveLongData(context, ACCESS_TOKEN_ISSUE_TIME, issueTime)
}

@Keep
internal fun getAccessTokenIssueTime(context: Context): Long {
    return getLongData(context, ACCESS_TOKEN_ISSUE_TIME)
}

@Keep
internal fun saveAccessTokenExpireTime(context: Context, expireTime: Long) {
    saveLongData(context, ACCESS_TOKEN_EXPIRE_TIME, expireTime)
}

@Keep
internal fun getAccessTokenExpireTime(context: Context): Long {
    return getLongData(context, ACCESS_TOKEN_EXPIRE_TIME)
}

@Keep
internal fun saveUserToken(context: Context, accessToken: String) {
    saveStringData(context, USER_TOKEN, accessToken)
}

@Keep
internal fun getUserToken(context: Context): String {
    return getStringData(context, USER_TOKEN)
}

@Keep
internal fun saveLoginToken(context: Context, accessToken: String) {
    saveStringData(context, USER_TOKEN, accessToken)
}

@Keep
internal fun getLoginToken(context: Context): String {
    return getStringData(context, USER_TOKEN)
}



@Keep
internal fun saveUserTokenIssueTime(context: Context, issueTime: Long) {
    saveLongData(context, USER_TOKEN_ISSUE_TIME, issueTime)
}

@Keep
internal fun getUserTokenIssueTime(context: Context): Long {
    return getLongData(context, USER_TOKEN_ISSUE_TIME)
}

@Keep
internal fun saveUserTokenExpireTime(context: Context, expireTime: Long) {
    saveLongData(context, USER_TOKEN_EXPIRE_TIME, expireTime)
}

@Keep
internal fun getUserTokenExpireTime(context: Context): Long {
    return getLongData(context, USER_TOKEN_EXPIRE_TIME)
}

@Keep
internal fun getBankNameFromBic(context: Context, bic: String): String {
    return getStringData(context, BIC_MAPPING_TO_BANK + bic)
}

@Keep
internal fun saveBankNameFromBic(context: Context, bic: String, bankName: String) {
    saveStringData(context, BIC_MAPPING_TO_BANK + bic, bankName)
}

@Keep
internal fun saveUserName(context: Context, userName: String) {
    saveStringData(context, USER_NAME, userName)
}

@Keep
internal fun getUserName(context: Context): String {
    return getStringData(context, USER_NAME)
}

@Keep
internal fun getBooleanData(context: Context, prefKey: String?): Boolean {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.sdk_shared_pref), Context.MODE_PRIVATE)
    return sharedPref.getBoolean(prefKey, false)
}

@Keep
internal fun saveBooleanData(activity: Activity, prefKey: String, data: Boolean) {
    val sharedPref = activity.getSharedPreferences(activity.getString(R.string.sdk_shared_pref), Context.MODE_PRIVATE)
            ?: return
    with(sharedPref.edit()) {
        putBoolean(prefKey, data)
        apply()
    }
}

@Keep
internal fun getCurrentLocale(context: Context): String {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//        context.resources.configuration.locales.get(0).toString().take(2)
//    } else {
//        context.resources.configuration.locale.toString().take(2)
//    }
    return getStringData(context, _LOCALE)
}

@Keep
internal fun getPicassoInstance(context: Context, sslConfig: SslConfig): Picasso {

    val (socketFactory, trustManager) = sslConfig

    val client = OkHttpClient.Builder()
            .sslSocketFactory(socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

    val picassoBuilder: Picasso.Builder = Picasso.Builder(context)
    picassoBuilder.downloader(OkHttp3Downloader(client))
    return picassoBuilder.build()
}

@Keep
internal fun getPspBaseUrlForBankLogo(bic: String, accessToken: String, appName: String, custPspId: String?): String {
    val pspBaseUrl = when {

        BuildConfig.USE_PUBLIC_IP -> {
            BuildConfig.PUBLIC_IP_PSP
        }
        BuildConfig.USE_SANDBOX -> {
            BuildConfig.SANDBOX_IP
        }
        else -> {
            BuildConfig.PSP_BASE_URL
        }
    }

    return "$pspBaseUrl/psp-umps-adaptor/umps-app/bank-logo?bic=$bic&accessToken=$accessToken&appName=$appName&custPSPId=$custPspId"
}

@Keep
internal fun getNameInitials(name: String): String {
    if (TextUtils.isEmpty(name)) return "?"
    val nameSegments = name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    var nameInitials = ""
    if (nameSegments.isNotEmpty()) {
        nameInitials = nameSegments[0].substring(0, 1)
    }
    return nameInitials
}

@Keep
internal fun getFormattedDate(context: Context?, dateLong: Long?): CharSequence? {
    return if (dateLong != null && context != null) {
        DateUtils.getRelativeTimeSpanString(context, dateLong, true)
    } else {
        null
    }
}

@Keep
internal fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo?.isConnectedOrConnecting ?: false
}

internal fun retrieveAccessToken(activity: Activity): String {
    return if ((getUserTokenExpireTime(activity) - System.currentTimeMillis()) < DateUtils.MINUTE_IN_MILLIS) {
       // if (activity.intent.hasExtra(SdkBaseActivity.USER_TOKEN)) {
         //   activity.intent.getStringExtra(SdkBaseActivity.USER_TOKEN)
       // } else {
            getUserToken(activity)
       // }
    } else {
        getUserToken(activity)
    }
}

@Keep
internal fun saveAccountTokenId(context: Context, accountTokenId: Long) {
    saveLongData(context, ACCOUNT_TOKEN_ID, accountTokenId)
}

@Keep
internal fun savePaymentAccountTokenId(context: Context, accountTokenId: Long) {
    saveLongData(context, ACCOUNT_TOKEN_ID, accountTokenId)
}

@Keep
internal fun getAccountTokenId(context: Context): Long {
    return getLongData(context, ACCOUNT_TOKEN_ID)
}