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

package com.indepay.umps.utils

import android.content.Context
import android.os.Build
import android.support.annotation.Keep
import com.indepay.umps.R
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val ACCESS_TOKEN_ISSUE_TIME = "access_token_issue_time"
private const val ACCESS_TOKEN_EXPIRE_TIME = "access_token_expire_time"
private const val ACCESS_TOKEN = "access_token"
private const val WALLET_BALANCE = "wallet_balance"

sealed class Result<out T : Any> {
    class Success<out T : Any>(val data: T) : Result<T>()
    class Error(val exception: Throwable) : Result<Nothing>()
}

internal data class SslConfig(
        val socketFactory: SSLSocketFactory,
        val trustManager: X509TrustManager
)


/**
 * This method is a part of Base App, called to get the SSL certificate for communication.
 *
 * @param
 * @return
 */


@Throws(CertificateException::class, IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
@Keep
internal fun getBaseAppSslConfig(context: Context): SslConfig {

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
internal fun saveStringData(context: Context, prefKey: String?, data: String?) {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.base_shared_pref), Context.MODE_PRIVATE)
            ?: return
    with(sharedPref.edit()) {
        putString(prefKey, data)
        apply()
    }
}

@Keep
internal fun getStringData(context: Context, prefKey: String?): String {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.base_shared_pref), Context.MODE_PRIVATE)
    return sharedPref.getString(prefKey, "").toString()
}

@Keep
internal fun getCurrentLocale(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales.get(0).toString().take(2)
    } else {
        context.resources.configuration.locale.toString().take(2)
    }
}

@Keep
internal fun getBooleanData(context: Context, prefKey: String?): Boolean {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.base_shared_pref), Context.MODE_PRIVATE)
    return sharedPref.getBoolean(prefKey, false)
}

@Keep
internal fun saveBooleanData(context: Context, prefKey: String, data: Boolean) {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.base_shared_pref), Context.MODE_PRIVATE)
            ?: return
    with(sharedPref.edit()) {
        putBoolean(prefKey, data)
        apply()
    }
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
internal fun saveLoginToken(context: Context, accessToken: String) {
    saveStringData(context, ACCESS_TOKEN, accessToken)
}

@Keep
internal fun getLoginToken(context: Context): String {
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
internal fun saveLongData(context: Context, prefKey: String?, data: Long) {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.base_shared_pref), Context.MODE_PRIVATE)
            ?: return
    with(sharedPref.edit()) {
        putLong(prefKey, data)
        apply()
    }
}

@Keep
internal fun getLongData(context: Context, prefKey: String?): Long {
    val sharedPref = context.getSharedPreferences(context.getString(R.string.base_shared_pref), Context.MODE_PRIVATE)
    return sharedPref.getLong(prefKey, 0)
}

@Keep
internal fun saveWalletBalance(context: Context, walletBal: String) {
    saveStringData(context, WALLET_BALANCE, walletBal)
}

@Keep
internal fun getWalletBalance(context: Context): String {
    return getStringData(context, WALLET_BALANCE)
}