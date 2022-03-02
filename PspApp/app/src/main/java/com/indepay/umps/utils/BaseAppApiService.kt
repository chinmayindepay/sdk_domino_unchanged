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

import com.google.gson.GsonBuilder
import com.indepay.umps.models.MerchantCollectRequest
import com.indepay.umps.models.MerchantCollectResponse
import com.indepay.umps.pspsdk.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


interface BaseAppApiService {

    @POST("psp-umps-adaptor/umps-login/app-login")
    fun fetchAppTokenAsync(@Query("ki") ki: String, @Body request: RequestBody): Deferred<Response<String>>

    @POST("psp-umps-adaptor/umps-login/merchant-login")
    fun fetchMerchantTokenAsync(@Query("ki") ki: String, @Body request: RequestBody): Deferred<Response<String>>

    @POST("psp-umps-adaptor/umps-merchant/initiate-transaction-request")
    fun initiateMerchantTransactionAsync(@Body merchantCollectRequest: MerchantCollectRequest): Deferred<MerchantCollectResponse>



    companion object Factory {

        lateinit var retrofitInstance: Retrofit

        internal fun create(sslConfig: SslConfig): BaseAppApiService {

            val (socketFactory, trustManager) = sslConfig

            val interCeptor = HttpLoggingInterceptor()
            interCeptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .sslSocketFactory(socketFactory, trustManager)
                    .hostnameVerifier { hostname, session -> true }
                    .addInterceptor(interCeptor)
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()

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

            val gson = GsonBuilder()
                    .setLenient()
                    .create()

            val retrofit = Retrofit.Builder()
                    .baseUrl(pspBaseUrl)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build()

            retrofitInstance = retrofit
            return retrofit.create(BaseAppApiService::class.java)
        }
    }



}