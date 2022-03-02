package com.indepay.umps.pspsdk.paycollectstep

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.BeneficiaryAddAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.beneficiary.Contacts.BeneContactsActivity
import com.indepay.umps.pspsdk.callbacks.OnContactListInteractionListener
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.setting.AddBeneficiaryActivity
import com.indepay.umps.pspsdk.transaction.payment.TxnPaymentActivity
import com.indepay.umps.pspsdk.utils.getAccessToken
import com.indepay.umps.pspsdk.utils.getAccountTokenId
import com.indepay.umps.pspsdk.utils.getAppName
import com.indepay.umps.pspsdk.utils.getPspId
import com.indepay.umps.pspsdk.utils.getUserName
import com.indepay.umps.pspsdk.utils.retrieveAccessToken
import kotlinx.android.synthetic.main.beneficiary_add_activity.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import org.jetbrains.anko.startActivity
import java.util.ArrayList


class BeneficiaryAddActivity:SdkBaseActivity(), OnContactListInteractionListener {
    private var beneficiaryList = ArrayList<BeneDetail>()
    private lateinit var beneficiaryAdapter: BeneficiaryAddAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.beneficiary_add_activity)
        setAdapter()
        getAllBeneficiaryApi()

        btn_addBene.setOnClickListener{
            startActivity(intentFor<AddBeneficiaryActivity>().singleTop().clearTop())
        }

    }

    private fun setAdapter() {
        val mManager=LinearLayoutManager(this)
        rvBeneficiary.layoutManager=mManager
        beneficiaryAdapter = BeneficiaryAddAdapter(this,this)
        rvBeneficiary.adapter = beneficiaryAdapter
    }


    private fun getAllBeneficiaryApi() {
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.getBeneficiaryListAsync(
                            accessToken = getAccessToken(this),
                            appName = getAppName(this),
                            custPSPId = getPspId(this)
                    )
                },
                successCallback = { commonResponse -> onSuccessBeneListFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }

    private fun onSuccessBeneListFetch(result: CommonResponse) {
        beneficiaryList = ArrayList()
        if (result is BeneficiaryListResponse) {
            if (result.beneDetails != null && result.beneDetails.size > 0) {
                beneficiaryList.addAll(result.beneDetails)
                beneficiaryAdapter.setData(beneficiaryList)
                beneficiaryAdapter.notifyDataSetChanged()

            }else
            {
                btn_addBene.visibility = View.VISIBLE
            }
        }
    }

    override fun onContactListInteraction(phoneNumber: String,contactName:String) {
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.fetchAccessTokenAsync(
                            TrackingRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    )
                            )
                    )
                },

                successCallback = {
                    commonResponse ->
                    startActivity<TxnPaymentActivity>(
                            //TxnPaymentActivity.PAYMENT_ADDRESS to response.pa,
                            TxnPaymentActivity.MOBILE_NO to phoneNumber,
                            TxnPaymentActivity.TRANSACTION_TYPE to intent.getStringExtra(BeneContactsActivity.TRANSACTION_TYPE),
                            TxnPaymentActivity.PAYEE_NAME to getUserName(this),
                            TxnPaymentActivity.APP_ID to contactName,
                            //TxnPaymentActivity.INITIATOR_PA_ACCOUNT_ID to getPaAccountId(this)

                            TxnPaymentActivity.INITIATOR_ACCOUNT_ID to getAccountTokenId(this)
                    )
                    finish()
                },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }
}