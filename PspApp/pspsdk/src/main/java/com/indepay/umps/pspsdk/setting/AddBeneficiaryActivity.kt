package com.indepay.umps.pspsdk.setting

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.BeneficiaryAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.beneficiary.Contacts.BeneContactsActivity
import kotlinx.android.synthetic.main.add_beneficiary_activity.*

import com.indepay.umps.pspsdk.adapter.AppDetailsAdapter
import java.util.ArrayList
import android.widget.AdapterView
import android.view.View
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.utils.*
import com.indepay.umps.pspsdk.utils.getAccessToken
import com.indepay.umps.pspsdk.utils.getAppName
import com.indepay.umps.pspsdk.utils.getPspId
import com.indepay.umps.pspsdk.utils.retrieveAccessToken
import android.app.Activity as Activity1
import android.support.design.widget.Snackbar
import android.text.Editable
import android.text.TextWatcher
import com.indepay.umps.pspsdk.transaction.payment.TxnPaymentActivity
//import kotlinx.android.synthetic.main.activity_bank_account_list.*

import kotlinx.android.synthetic.main.add_beneficiary_row.*
import org.jetbrains.anko.*


class AddBeneficiaryActivity : SdkBaseActivity() {
    private val REQUEST_CODE_REGISTRATION = 12
    private var appDetailsList = ArrayList<AppDetail>()
    private var beneDetailsList = ArrayList<BeneDetail>()
    private lateinit var appDetailsAdapter: AppDetailsAdapter
    private lateinit var beneficiaryAdapter: BeneficiaryAdapter
    private var selectedApp: AppDetail? = null
    private var comingFrom: Int = 0
    private var mobileNo: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_beneficiary_activity)
        comingFrom = intent.getIntExtra(COMING_FROM, 0)
        setData()
        if (comingFrom != 1) {
            rvBeneficiaryList.visibility = View.VISIBLE
            tvListText.visibility = View.VISIBLE
            getAllBeneficiaryApi()
            setAdapter()
        } else {
            tvLevel.text=""
            mobileNo = intent.getStringExtra(TxnPaymentActivity.MOBILE_NO).toString()
            etMobileNo.setText(mobileNo)
            etMobileNo.isFocusableInTouchMode = false
            etMobileNo.isFocusable = false
            ivContact.isEnabled = false
            if (mobileNo.trim().length >= 10) {
                getAppListApi(mobileNo.trim())
            }
        }
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
        beneDetailsList = ArrayList()
        if (result is BeneficiaryListResponse) {
            if (result.beneDetails != null && result.beneDetails.size > 0) {
                beneDetailsList.addAll(result.beneDetails)
                beneficiaryAdapter.setData(beneDetailsList)
                beneficiaryAdapter.notifyDataSetChanged()

            }
        }
    }


    private fun getAppListApi(mobileNo: String) {
 //      showDialog()

        llProgressBar.visibility = View.VISIBLE
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.getAppDetailsAsync(
                            mobile = mobileNo,
                            accessToken = getAccessToken(this),
                            appName = getAppName(this),
                            custPSPId = getPspId(this)
                    )
                },
                successCallback = { commonResponse -> onSuccessAppDetailsFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

    }

    private fun onSuccessAppDetailsFetch(result: CommonResponse) {
//        hideDialog()
        //llProgressBar.visibility = View.GONE
        appDetailsList = ArrayList()
        if (result is AppDetailsResponse) {
            if (result.appDetailsList != null && result.appDetailsList.size > 0) {
                appDetailsList.addAll(result.appDetailsList)
                appDetailsAdapter.setData(appDetailsList)
                appDetailsAdapter.notifyDataSetChanged()

            }
        }
        llProgressBar.visibility = View.GONE
    }

    private fun setAdapter() {
        val manager = LinearLayoutManager(this)
        rvBeneficiaryList.layoutManager = manager
        beneficiaryAdapter = BeneficiaryAdapter(this)
        rvBeneficiaryList.adapter = beneficiaryAdapter
    }

    private fun setData() {
        appDetailsAdapter = AppDetailsAdapter(this)
        spinner.setAdapter(appDetailsAdapter)
        ivContact.setOnClickListener {
            val intent = Intent(this, BeneContactsActivity::class.java)
            intent.putExtra(COMING_FROM, 1)
            intent.putExtra(BeneContactsActivity.TRANSACTION_TYPE, BeneContactsActivity.TXN_TYPE_PAY)
            startActivityForResult(intent, REQUEST_CODE_REGISTRATION)
        }

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
               // Toast.makeText(this@AddBeneficiaryActivity, "You Select Position: " + position, Toast.LENGTH_SHORT).show()
                if (appDetailsList.isNotEmpty()) {
                    selectedApp = appDetailsList[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        })

        butSubmit.setOnClickListener {
            if (isValidate()) {
                if (comingFrom == 1) {
                    startActivity<TxnPaymentActivity>(
                            //TxnPaymentActivity.PAYMENT_ADDRESS to response.pa,
                            TxnPaymentActivity.MOBILE_NO to mobileNo,
                            TxnPaymentActivity.TRANSACTION_TYPE to intent.getStringExtra(TxnPaymentActivity.TRANSACTION_TYPE),
                            TxnPaymentActivity.PAYEE_NAME to getUserName(this),
                            TxnPaymentActivity.APP_ID to selectedApp?.appId,
                            //TxnPaymentActivity.INITIATOR_PA_ACCOUNT_ID to getPaAccountId(this)
                            TxnPaymentActivity.INITIATOR_ACCOUNT_ID to getAccountTokenId(this)
                    )
                    finish()

                } else {
                    addBeneficiaryApi(etUserName.text.toString().trim(), etMobileNo.text.toString().trim(), selectedApp?.appId!!)
                }
            }

        }
        etMobileNo.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /*if (s.toString().trim().length >= 9) {
                    getAppListApi(s.toString().trim())
                }*/
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().length >= 10) {
                    getAppListApi(s.toString().trim())
                }
            }

        })

    }

    private fun addBeneficiaryApi(beneName: String, beneMobileNo: String, beneAppName: String) {
//        showDialog()
        llProgressBar.visibility = View.VISIBLE
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.addNewBeneficiaryAsync(
                            AddNewBeneRequest(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    requestedLocale = getCurrentLocale(this),
                                    beneName = beneName,
                                    beneMobile = beneMobileNo,
                                    beneAppName = beneAppName,
                                    beneType = "MOBILE"

                            )
                    )
                },
                successCallback = {
//                    hideDialog()
                    llProgressBar.visibility = View.GONE
                    if (it is AddBeneficiaryResponse) {
                        etUserName.text=null
                        etMobileNo.text=null
                        appDetailsList.clear()
                        appDetailsAdapter.notifyDataSetChanged()
                        alert(resources.getString(R.string.name)+": ${it.beneName} \n"+resources.getString(R.string.mobile)+": ${it.beneMobile} \n"+resources.getString(R.string.app_namme)+": ${it.beneAppId}",
                                resources.getString(R.string.bene_add_success)) {
                            okButton {
                                getAllBeneficiaryApi()
                            }
                            isCancelable = false
                        }.show()
                    }
                },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )
    }

    /* private fun onSuccessAddBeneficiary(result: CommonResponse) {
         appDetailsList=ArrayList()
         if (result is AppDetailsResponse) {
             if (result.appDetailsList != null && result.appDetailsList.size > 0) {
                 appDetailsList.addAll(result.appDetailsList)
                 appDetailsAdapter.setData(appDetailsList)
                 appDetailsAdapter.notifyDataSetChanged()

             }
         }
     }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode === REQUEST_CODE_REGISTRATION) {
            if (resultCode === Activity1.RESULT_OK) {
                val result = data!!.getStringExtra(RESULT)
                etMobileNo?.setText(result)
                if (result != null) {
                    getAppListApi(result)
                }
            }
            if (resultCode === Activity1.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private fun isValidate(): Boolean {
        when {
            TextUtils.isEmpty(etUserName.text.toString()) -> {
                Snackbar.make(tvAppName, resources.getString(R.string.name_error), Snackbar.LENGTH_SHORT).show()
                return false
            }
            TextUtils.isEmpty(etMobileNo.text.toString()) -> {
                Snackbar.make(tvAppName, resources.getString(R.string.mobile_error), Snackbar.LENGTH_SHORT).show()
                return false
            }
            etMobileNo.text.toString().length > 13 -> {
                Snackbar.make(tvAppName, resources.getString(R.string.valid_mobile_error), Snackbar.LENGTH_SHORT).show()
                return false
            }
            TextUtils.isEmpty(selectedApp?.name) -> {
                Snackbar.make(tvAppName, resources.getString(R.string.app_error), Snackbar.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }
    }

}