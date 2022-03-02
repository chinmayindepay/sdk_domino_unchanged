package com.indepay.umps.pspsdk.utils

import com.google.gson.GsonBuilder
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.spl.models.EncryptionValidateOtpRetrievalRequest
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


interface   SdkApiService {

    //Initiate Registration Process
    @POST("psp-umps-adaptor/umps-app/register")
    fun initRegistrationAsync(@Body request: RegistrationInitiateRequest): Deferred<RegistrationInitiateResponse>

    //Track Registration Status
    @POST("psp-umps-adaptor/umps-app/track-registration")
    fun trackRegistrationAsync(@Body request: TrackingRequest): Deferred<TrackerResponse>

    @POST("psp-umps-adaptor/umps-app/track-reset-creds-request")
    fun trackResetCredRequestAsync(@Body request: TrackingRequest): Deferred<TrackerResponse>

    //Validate Payment Address
    @POST("psp-umps-adaptor/umps-app/validate-pa-request")
    fun validatePaymentAddressAsync(@Body request: ValidatePaRequest): Deferred<ValidatePaResponse>

    @POST("psp-umps-adaptor/umps-app/initiate-balance-inquiry-request")
    fun initBalanceEnquiryAsync(@Body request: BalanceEnquiryInitiateRequest): Deferred<BalanceEnquiryInitiateResponse>

    @POST("psp-umps-adaptor/umps-app/track-balance-inquiry-request")
    fun trackBalanceEnquiryAsync(@Body request: TrackingRequest): Deferred<TrackerResponse>

    @GET("psp-umps-adaptor/umps-app/get-beneficiaries")
    fun getBeneficiariesAsync(
            @Query("custPSPId") custPSPId: String?,
            @Query("accessToken") accessToken: String,
            @Query("appName") appName: String
    ): Deferred<ArrayList<Beneficiary>>

    @POST("psp-umps-adaptor/umps-app/add-beneficiary")
    fun addBeneficiaryAsync(@Body request: AddBeneficiaryRequest): Deferred<AddBeneficiaryResponse>

    @POST("psp-umps-adaptor/umps-app/initiate-transaction-request")
    fun initTransactionAsync(@Body request: TransactionRequest): Deferred<TransactionResponse>

    @POST("psp-umps-adaptor/umps-app/track-transaction-request")
    fun trackTransactionAsync(@Body request: TrackingRequest): Deferred<TrackerResponse>

    @POST("psp-umps-adaptor/umps-app/fetch-account-list")
    fun fetchAccountByPaIdAsync(@Body fetchAccountByPaIdRequest: FetchAccountByPaIdRequest): Deferred<ArrayList<MappedAccount>>

    @POST("psp-umps-adaptor/umps-app/set-default-account")
    fun setDefaultAccountAsync(@Body setDefaultAccountRequest: SetDefaultAccountRequest): Deferred<TrackerResponse>

    @POST("psp-umps-adaptor/umps-app/payer-collect-response")
    fun collectRejectionRequestAsync(@Body collectRejectionRequest: CollectRejectionRequest): Deferred<TrackerResponse>

    @POST("psp-umps-adaptor/umps-app/payer-collect-response")
    fun collectApprovalRequestAsync(@Body collectApproveRequest: CollectApproveRequest): Deferred<CollectApproveResponse>

    @GET("psp-umps-adaptor/umps-app/txn-history-details")
    fun getTransactionHistoryDetailsAsync(
            @Query("appName") appName: String,
            @Query("accessToken") accessToken: String,
            @Query("custPSPId") custPSPId: String?,
            @Query("requestedLocale") requestedLocale: String,
            @Query("txnId") txnId: String? = null,
            @Query("includeWaitingForApprovalOnly") includeWaitingForApprovalOnly: Boolean
    ): Deferred<TxnHistoryResponse>

    @POST("psp-umps-adaptor/umps-app/fetch-ecosystem-banklist")
    fun fetchEcosystemBankListAsync(@Body ecosystemBankRequest: EcosystemBankRequest): Deferred<ArrayList<EcosystemBankResponse>>

    @POST("psp-umps-adaptor/umps-app/initiate-account-details-request")
    fun initiateAccDetailAsync(@Body accountDetailsRequest: AccountDetailsRequest): Deferred<AccountDetailsResponse>

    @POST("psp-umps-adaptor/umps-app/initiate-account-details-request-api")
    fun initiateAccDetailAsyncNew(@Body accountDetailsRequest: AccountDetailsRequestNewapi): Deferred<AccountDetailsResponse>

    @POST("psp-umps-adaptor/umps-app/track-account-details-request-api")
    fun trackAccountDetailsApi(@Body trackAccountDetailsRequest: TrackAccountDetailsRequest): Deferred<TrackAccountDetailsResponse>

    @POST("psp-umps-adaptor/umps-app/pre-transaction-request")
    fun pretransactionrequest(@Body pretransactionrequest: PreTransactionRequestApi): Deferred<PreTransactionResponse>

    @POST("psp-umps-adaptor/umps-app/initiate-delete-account-request")
    fun deleteAccountrequest(@Body deleteAccountrequest: DeleteAccountRequest): Deferred<DeleteAccountResponse>

    @POST("psp-umps-adaptor/umps-app/track-delete-account-request")
    fun trackDeleteAccountrequest(@Body trackDeleteAccountrequest: TrackDeleteAccountRequest): Deferred<TrackDeleteAccountResponse>


    @POST("psp-umps-adaptor/umps-app/map-account-to-profile")
    fun mapAccountToProfileAsync(@Body mapAccountToProfileRequest: MapAccountToProfileRequest): Deferred<MapAccountToProfileResponse>

    @POST("psp-umps-adaptor/umps-app/initiate-reset-creds-request")
    fun initSetResetMpinReqAsync(@Body setResetMpinInitRequest: SetResetMpinInitRequest): Deferred<SetResetMpinInitResponse>

    @POST("psp-umps-adaptor/umps-app/customer-profile-details")
    fun fetchCustomerDetailsAsync(@Body customerProfileRequest: CustomerProfileRequest): Deferred<CustomerProfileResponse>

    @POST("psp-umps-adaptor/umps-app/fetch-notification-by-msg-ids")
    fun fetchNotificationByMsgIdsAsync(@Body notificationFetchRequest: NotificationFetchRequest): Deferred<ArrayList<NotificationFetchResponse>>

    @GET("psp-umps-adaptor/umps-app/check-customer-pa-by-mobile")
    fun resolvePaByMobileNumberAsync(
            @Query("mobile") mobile: String,
            @Query("accessToken") accessToken: String,
            @Query("appName") appName: String,
            @Query("custPSPId") custPSPId: String?
    ): Deferred<ResolvePaByContactNoResp>

    @POST("psp-umps-adaptor/umps-app/issue-private-access-token")
    fun fetchAccessTokenAsync(@Body trackingRequest: TrackingRequest): Deferred<AccessToken>

    @GET("psp-umps-adaptor/umps-app/fetch-participant-apps")
    fun getAppDetailsAsync(
            @Query("mobile") mobile: String,
            @Query("accessToken") accessToken: String,
            @Query("appName") appName: String,
            @Query("custPSPId") custPSPId: String?
    ): Deferred<AppDetailsResponse>

    @GET("psp-umps-adaptor/umps-app/get-beneficiaries")
    fun getBeneficiaryListAsync(
            @Query("accessToken") accessToken: String,
            @Query("appName") appName: String,
            @Query("custPSPId") custPSPId: String?
    ): Deferred<BeneficiaryListResponse>


    @POST("psp-umps-adaptor/umps-app/add-beneficiary")
    fun addNewBeneficiaryAsync(@Body request: AddNewBeneRequest): Deferred<AddBeneficiaryResponse>

    @POST("psp-umps-adaptor/umps-login/merchant-login")
    fun fetchMerchantTokenAsync(@Query("ki") ki: String, @Body request: RequestBody): Deferred<Response<String>>

    @POST("psp-umps-adaptor/umps-merchant/initiate-transaction-request")
    fun initiateMerchantTransactionAsync(@Body merchantCollectRequest: MerchantCollectRequest): Deferred<MerchantCollectResponse>

    @POST("psp-umps-adaptor/umps-login/app-login")
    fun fetchAppTokenAsync(@Query("ki") ki: String, @Body request: RequestBody): Deferred<Response<String>>

    companion object Factory {

        lateinit var retrofitInstance: Retrofit

        internal fun create(sslConfig: SslConfig, userName: String): SdkApiService {

            val (socketFactory, trustManager) = sslConfig

            if(!userName.isNullOrBlank()) {
                val interceptor = CustomInterceptor(userName)
                interceptor.level = CustomInterceptor.Level.BODY

                val client = OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .sslSocketFactory(socketFactory, trustManager)
                        .hostnameVerifier { hostname, session -> true }
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        .build()

                val pspBaseUrl = when{

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
                val retrofit = Retrofit.Builder()
                        .baseUrl(pspBaseUrl)
                        .addCallAdapterFactory(CoroutineCallAdapterFactory())
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client)
                        .build()

                retrofitInstance = retrofit
                return retrofit.create(SdkApiService::class.java)
            }else{

                val client = OkHttpClient.Builder()
                        .sslSocketFactory(socketFactory, trustManager)
                        .hostnameVerifier { hostname, session -> true }
                        .connectTimeout(120, TimeUnit.SECONDS)
                        .writeTimeout(120, TimeUnit.SECONDS)
                        .readTimeout(120, TimeUnit.SECONDS)
                        .build()

                val pspBaseUrl = when{

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
                return retrofit.create(SdkApiService::class.java)
            }


        }
    }

}