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

import com.indepay.umps.spl.BuildConfig
import com.indepay.umps.spl.models.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface SplApiService {

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/user-registration")
    fun registerUserAsync(@Body request: SplRegistrationRequest): Deferred<SplRegistrationResponse>

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/authorize")
    fun createCredentialSubmissionRequest(@Body request: CredentialSubmissionRequest): Deferred<CredentialSubmissionResponse>

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/retrieve-key")
    fun createRetrieveKeysRequest(@Body request: EncryptionKeyRetrievalRequest): Deferred<EncryptionKeyRetrievalResponse>

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/fetch-otp-challenge-code")
    fun createfetchOtpChallengeRequest(@Body request: EncryotionOtpFetchRetrievalRequest): Deferred<EncryptionFetchOtpRetrievalResponse>

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/refresh-otp-api")
    fun createrefreshOtpRequest(@Body request: EncryptionRefreshOtpRequest): Deferred<EncryptionFetchOtpRetrievalResponse>


    @POST("/umps-core/spl-sdk/urn:apiver:1.0/validate-otp-api")
    fun createValidateOtpRequest(@Body request: EncryptionValidateOtpRetrievalRequest): Deferred<EncryptionFetchOtpRetrievalResponse>


    @POST("/umps-core/spl-sdk/urn:apiver:1.0/resend-otp-request")
    fun resendOtpApi(@Body request: EncryptionKeyRetrievalRequest): Deferred<EncryptionKeyRetrievalResponse>

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/reset-creds-request")
    fun resetCredentialRequest(@Body request: ResetCredentialRequest): Deferred<ResetCredentialResponse>

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/register-card-detail")
    fun registerCardDetailRequest(@Body request: RegisterCardDetailRequest): Deferred<RegisterCardDetailResponse>

    @POST("/umps-core/spl-sdk/urn:apiver:1.0/confirm-account-registration")
    fun confirmRegisterCardDetailRequest(@Body request: ConfirmRegisterCardDetailRequest): Deferred<ConfirmRegisterCardDetailResponse>

    //Mock SMS sending
    @POST("umps-core/umps-sms/urn:apiver:1.0/user-registration-txn")
    fun mockSmsRequestAsync(@Body request: UserRegistrationTxnSms): Deferred<Response<Void>>

    companion object Factory {

        fun create(sslConfig: SslConfig): SplApiService {

            val (socketFactory, trustManager) = sslConfig

            val interCeptor = HttpLoggingInterceptor()
            interCeptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .addInterceptor(interCeptor)
                    .sslSocketFactory(socketFactory, trustManager)
                    .hostnameVerifier { hostname, session -> true }
                    .connectTimeout(120, TimeUnit.SECONDS)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS)
                    .build()

            val coreBaseUrl = when {
                BuildConfig.USE_PUBLIC_IP -> {
                    BuildConfig.PUBLIC_IP_CORE
                }
                BuildConfig.USE_SANDBOX -> {
                    BuildConfig.SANDBOX_IP
                }
                else -> {
                    BuildConfig.CORE_BASE_URL
                }

            }
            val retrofit = Retrofit.Builder()
                    .baseUrl(coreBaseUrl)
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()

            return retrofit.create(SplApiService::class.java)
        }

    }

}