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

package com.indepay.umps.spl.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.Keep
import android.support.v4.app.ActivityCompat
import android.telephony.TelephonyManager
import android.widget.Toast
import com.indepay.umps.spl.BuildConfig
import com.indepay.umps.spl.R
import java.io.IOException
import java.nio.ByteBuffer
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val SPL_KEY = "spl_key"
private const val PSP_ID = "psp_id"
private const val SPL_ID = "spl_id"
private const val IMEI_ONE = "imei_one"
private const val IMEI_TWO = "imei_two"
private const val _LOCALE = "locale"

sealed class Result<out T : Any> {

    class Success<out T : Any>(val data: T) : Result<T>()
    class Error(val exception: Throwable) : Result<Nothing>()
}

data class SslConfig(
        val socketFactory: SSLSocketFactory,
        val trustManager: X509TrustManager
)

/**
 * This method is a part of SPL, called to get the SSL certificate for communication.
 *
 * @param
 * @return
 */


@Throws(CertificateException::class, IOException::class, KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
@Keep
 fun getSSLConfig(context: Context/*, certFileResource: Int*/): SslConfig {

    // Loading CAs from an InputStream
    var cf: CertificateFactory? = null
    cf = CertificateFactory.getInstance("X.509")

    var ca: Certificate? = null
    val certFileResource: Int

    //For Private network
    /*if (BuildConfig.USE_SANDBOX) {
        certFileResource = R.raw.core_tls_aws
    } else {
        certFileResource = R.raw.core_tls
    }*/

    //For Public network
    if (BuildConfig.USE_SANDBOX) {
//        certFileResource = R.raw.star_indepay_com
        certFileResource = R.raw.common_crt
    } else {
      //  certFileResource = R.raw.star_indepay_com
        certFileResource = R.raw.common_crt
    }

    context.applicationContext.resources.openRawResource(certFileResource).use { cert -> ca = cf!!.generateCertificate(cert) }

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
internal fun saveStringData(activity: Activity, prefKey: String?, data: String?) {
    val sharedPref = activity.getSharedPreferences(activity.getString(R.string.spl_shared_pref), Context.MODE_PRIVATE)
            ?: return
    with(sharedPref.edit()) {
        putString(prefKey, data)
        apply()
    }
}

@Keep
internal fun getStringData(activity: Activity, prefKey: String?): String {
    val sharedPref = activity.getSharedPreferences(activity.getString(R.string.spl_shared_pref), Context.MODE_PRIVATE)
    return sharedPref.getString(prefKey, "").toString()
}

@Keep
fun saveLocale(activity: Activity, locale: String) {
    saveStringData(activity, _LOCALE, locale)
}

@Keep
fun getPspId(activity: Activity): String {
    return getStringData(activity, PSP_ID)
}

@Keep
 fun getSplKey(activity: Activity): String {
    return getStringData(activity, SPL_KEY)
}

@Keep
fun getSplId(activity: Activity): String {
    //    if (TextUtils.isEmpty(splId)) {
//        splId = UUID.randomUUID().toString()
//    }
    return getStringData(activity, SPL_ID)
}

@Keep
internal fun saveSplData(activity: Activity, splKey: String, pspId: String, splId: String) {
    saveStringData(activity, SPL_KEY, splKey)
    saveStringData(activity, PSP_ID, pspId)
    saveStringData(activity, SPL_ID, splId)
}

@SuppressLint("MissingPermission")
@Keep
fun getDeviceId(context: Context): String {
    val telephonyManager = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> android.provider.Settings.Secure.getString(
                context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        else -> telephonyManager.deviceId
    }
}

@SuppressLint("MissingPermission")
@Keep
fun getImei1(context: Context, activity: Activity): String {
    var imei1 = ""
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        if(getStringData(activity, IMEI_ONE).isNullOrBlank()) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            imei1 = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> UUID.randomUUID().toString()
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> telephonyManager.getDeviceId(0)
                else -> telephonyManager.deviceId
            }
            saveImei1(activity,imei1)
        }else{
            imei1 = getStringData(activity, IMEI_ONE)
        }
    } else {
        Toast.makeText(context, "Unable to retrieve IMEI1", Toast.LENGTH_SHORT).show()
    }

    return imei1
}

@SuppressLint("MissingPermission")
@Keep
fun getImei2(context: Context, activity: Activity): String {
    var imei2 = ""
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        if(getStringData(activity, IMEI_TWO).isNullOrBlank()) {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            imei2 = when {
                android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> UUID.randomUUID().toString()
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> telephonyManager.getDeviceId(1)
                else -> telephonyManager.deviceId
            }
            saveImei2(activity,imei2)
        }else {
            imei2 = getStringData(activity, IMEI_TWO)
        }
    } else {
        Toast.makeText(context, "Unable to retrieve IMEI2", Toast.LENGTH_SHORT).show()
    }
    return imei2
}

@Keep
internal fun saveImei1(activity: Activity, pspId: String) {
    saveStringData(activity, IMEI_ONE, pspId)
}

@Keep
internal fun saveImei2(activity: Activity, pspId: String) {
    saveStringData(activity, IMEI_TWO, pspId)
}

@Keep
internal fun getCurrentLocale(activity: Activity): String {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//        context.resources.configuration.locales.get(0).toString().take(2)
//    } else {
//        context.resources.configuration.locale.toString().take(2)
//    }
    return getStringData(activity, _LOCALE)
}