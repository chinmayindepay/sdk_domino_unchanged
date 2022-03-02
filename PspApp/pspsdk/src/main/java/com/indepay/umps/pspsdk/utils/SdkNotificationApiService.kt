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

import com.indepay.umps.pspsdk.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * This interface to be used to call http notification id fetching
 */
interface SdkNotificationApiService {

    @GET("notification-service/notification/getMessageIds/{pspId}")
    fun getNotificationMsgIdAsync(@Path("pspId") pspId: String?): Deferred<ArrayList<String>>

    companion object Factory {

        lateinit var retrofitInstance: Retrofit

        fun create(userName: String): SdkNotificationApiService {
            val interceptor = CustomInterceptor(userName)
            interceptor.level = CustomInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()

            val notiBaseUrl: String = when {

                BuildConfig.USE_PUBLIC_IP -> {
                    BuildConfig.PUBLIC_IP_NOTI
                }
                BuildConfig.USE_SANDBOX -> {
                    BuildConfig.SANDBOX_IP_NOTI
                }
                else -> {
                    BuildConfig.NOTI_BASE_URL_GET
                }
            }

            val retrofit = Retrofit.Builder()
                    .baseUrl(notiBaseUrl)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()

            retrofitInstance = retrofit
            return retrofit.create(SdkNotificationApiService::class.java)
        }

    }

}