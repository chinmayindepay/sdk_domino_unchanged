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

package com.indepay.umps.pspsdk.accountSetup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Keep
import android.util.Log
import android.view.View
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.EcosystemBanksAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnEcosystemBankItemSlectionListener
import com.indepay.umps.pspsdk.models.*
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_bank_account_list.*
import kotlinx.android.synthetic.main.activity_ecosystem_banks.*
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.activity_ecosystem_banks.llProgressBar as llProgressBar1


class EcosystemBanksActivity : SdkBaseActivity(), OnEcosystemBankItemSlectionListener {

    private val REQUEST_CODE_SHOW_BANK_LIST = 10300

    private lateinit var txnId: String
        private var bic: String = ""
    private val REQ_CODE_ADD_ACCOUNT = 10700


    private lateinit var amount:String
    private lateinit var order_id:String
    private lateinit var email:String
    private lateinit var merchName:String
    private lateinit var remarks:String

    private lateinit var bankselected: EcosystemBankResponse



    companion object : SdkCommonMembers() {
        const val AMOUNT = "amount"
        @Keep  const val ORDER_ID = "order_id"
        @Keep  const val EMAIL = "email"
        @Keep  const val MERCHANT_NAME = "merchant_name"
        @Keep  const val NOTE = "note"
        @Keep   const val REMARKS = "remarks"

    }

    /**
     * This method is called by SDK itself, from Registration and Manage Account Activities to
     *              Fetch Bank List.
     * @param EcosystemBankRequest
     * @return EcosystemBankResponse
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ecosystem_banks)
        //txt_payment_address.text = getPaymentAddress(this@EcosystemBanksActivity)
        if(intent.getStringExtra(AMOUNT)!=null) {
            amount = intent.getStringExtra(AMOUNT).toString()
            order_id = intent.getStringExtra(ORDER_ID).toString()
            email = intent.getStringExtra(EMAIL).toString()
            remarks = intent.getStringExtra(REMARKS).toString()
            merchName = intent.getStringExtra(MERCHANT_NAME).toString()
        }

        val layoutManager = android.support.v7.widget.LinearLayoutManager(this)
        val itemDecoration = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)
        rv_bank_list.layoutManager = layoutManager
        rv_bank_list.addItemDecoration(itemDecoration)
//        showDialog()
        llProgressBar.visibility = View.VISIBLE

        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                arrayApiToCall = { sdkApiService ->
                    sdkApiService.fetchEcosystemBankListAsync(
                            EcosystemBankRequest(
                                    accessToken = getAccessToken(this),
                                    custPSPId = getPspId(this),
                                    requestedLocale = getCurrentLocale(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    )
                            )
                    )
                },
                successArrayCallback = { commonResponse -> onSuccessBankListLoading(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )

        back_arrowimage.setOnClickListener {
            onBackPressed()
        }
    }

    private fun onSuccessBankListLoading(result: ArrayList<out CommonResponse>) {
//        hideDialog()
        llProgressBar.visibility = View.GONE

        val ecosystemBankList: ArrayList<EcosystemBankResponse> = result as ArrayList<EcosystemBankResponse>
        if (ecosystemBankList.isNotEmpty()) {
//            for (i in 0..ecosystemBankList.size-1){
//                for (j in 0..ecosystemBankList.get(i).data.size-1) {
//                   Log.d("Sudhir","Pos I:"+i +"Pos J:"+j +"param::"+ecosystemBankList.get(i).data.get(j).param)
//                    Log.d("Sudhir","Pos I:"+i +"Pos J:"+j +"required::"+ecosystemBankList.get(i).data.get(j).required)
//                }
//            }
            val picasso = getPicassoInstance(this@EcosystemBanksActivity, getPspSslConfig(this@EcosystemBanksActivity))
            val banksAdapter = EcosystemBanksAdapter(
                    bankList = ecosystemBankList,
                    listener = this@EcosystemBanksActivity,
                    picassoInstance = picasso,
                    accessToken = getAccessToken(this@EcosystemBanksActivity),
                    appName = getAppName(this@EcosystemBanksActivity),
                    custPspId = getPspId(this@EcosystemBanksActivity))
            rv_bank_list.adapter = banksAdapter
        } else {
            alert(resources.getString(R.string.failed_to_fetch), resources.getString(R.string.no_data)) {
                okButton { finish() }
                isCancelable = false
            }.show()
        }
    }

    override fun onDestroy() {
        dialog.dismiss()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SHOW_BANK_LIST && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            sendError(data.getStringExtra(ERROR_CODE).toString())
        }
    }

//    override fun onBankListItemClick(bankItem: EcosystemBankResponse) {
//        val inputTxt = EditText(this)
//        alert(resources.getString(R.string.please_enter_debit_card)) {
//            customView = inputTxt
//            inputTxt.setInputType(InputType.TYPE_CLASS_NUMBER)
//            inputTxt.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(6)))
//            inputTxt.setRawInputType(Configuration.KEYBOARD_12KEY);
//            okButton {
//                if(inputTxt.text.length==6){
//                    startActivityForResult(intentFor<BankAccountListActivity>(
//                            BankAccountListActivity.BANK_BIC to bankItem.bic,
//                            BankAccountListActivity.DEBIT_CARD_NO to inputTxt.text.toString(),
//                            BankAccountListActivity.BANK_NAME to bankItem.name).singleTop().clearTop(), REQUEST_CODE_SHOW_BANK_LIST
//                    )
//                }
//            }
//
//
//            isCancelable = false
//            cancelButton {
//                //finish()
//                it.dismiss()}
//        }.show()
//
//    }

    override fun onBankListItemClick(bankItem: EcosystemBankResponse) {
        llProgressBar.visibility = View.VISIBLE
                bic = bankItem.bic.toString()

//            for (j in 0..bankItem.data.size-1) {
//                Log.d("Sudhir","Pos J:"+j +"param::"+bankItem.data.get(j).param)
//                Log.d("Sudhir","Pos J:"+j +"required::"+bankItem.data.get(j).required)
//            }

        bankselected = bankItem
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.initiateAccDetailAsyncNew(
                            AccountDetailsRequestNewapi(
                                    custPSPId = getPspId(this),
                                    accessToken = getAccessToken(this),
                                    acquiringSource = AcquiringSource(
                                            appName = getAppName(this)
                                    ),
                                    transactionId = null,
                                    merchantId = null,
                                    bic =  bankItem.bic,//intent.getStringExtra(BankAccountListActivity.BANK_BIC),
                                    cardLast6Digits = "XXXXXX",//intent.getStringExtra(BankAccountListActivity.DEBIT_CARD_NO)
                                    email = email
                                    //paId = getPaId(this)
                            )
                    )
                },
                successCallback = { commonResponse -> onSuccessAccDetailsFetch(commonResponse) },
                errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
        )




  //  startActivity(intentFor<ConnectNewAccountActivity>("bankbic" to bankItem.bic,"bankname" to bankItem.name).singleTop().clearTop())


        //startActivity(intentFor<ConnectNewAccountActivity>().singleTop().clearTop())

//        val builder = AlertDialog.Builder(this)
//        val inflater = layoutInflater
//
//        val dialogLayout = inflater.inflate(R.layout.dialog_enter_debit, null)
//        val debitt  = dialogLayout.findViewById<EditText>(R.id.edt_debit_card)
//        val positiveBut = dialogLayout.findViewById<TextView>(R.id.ok_debit_card)
//        val negativeBut = dialogLayout.findViewById<TextView>(R.id.cancel_debit_card)
//        builder.setView(dialogLayout)
//
//        val alertDialog = builder.create()
//        alertDialog.setCanceledOnTouchOutside(false)
//        debitt.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (s != null) {
//                    if(s.length < 6){
//                        positiveBut.isEnabled = false
//                        debitt.setError(resources.getString(R.string.please_enter_debit_card))
//                    }
//                    else if(s.length == 6){
//                        positiveBut.isEnabled = true
//                    }
//                }
//            }
//
//        })
//        positiveBut.setOnClickListener {
//            if(debitt.text.length == 6){
//                alertDialog.dismiss()
//                startActivityForResult(intentFor<BankAccountListActivity>(
//                        BankAccountListActivity.BANK_BIC to bankItem.bic,
//                        BankAccountListActivity.DEBIT_CARD_NO to debitt.text.toString(),
//                        BankAccountListActivity.BANK_NAME to bankItem.name).singleTop().clearTop(), REQUEST_CODE_SHOW_BANK_LIST
//                )
//            }
//        }
//
//        negativeBut.setOnClickListener {
//            alertDialog.dismiss()
//        }
//
//
//        alertDialog.show()
    }

private fun onSuccessAccDetailsFetch(result: CommonResponse) {
//    hideDialog()
    llProgressBar.visibility = View.GONE

    if (result is AccountDetailsResponse) {
        result.transactionId?.let {
            txnId = result.transactionId
            Log.d("Sudhir","after:"+txnId)

            //  result.listOfMappedAccount?.let {
//                if (it.isNotEmpty()) {
//                    val bankAccList = result.listOfMappedAccount
//                    val picasso = getPicassoInstance(this, getPspSslConfig(this))
//                    val banksAdapter = BankAccountListAdapter(
//                            bankAcList = bankAccList,
//                            listener = this,
//                            picassoInstance = picasso,
//                            accessToken = getAccessToken(this),
//                            appName = getAppName(this),
//                            custPspId = getPspId(this))
//                    rv_bank_acc_list.adapter = banksAdapter
//                } else {
//                    alert(message = resources.getString(R.string.no_acc_with_mobile_number), title = intent.getStringExtra(BankAccountListActivity.BANK_NAME)) {
//                        okButton { finish() }
//                        isCancelable = false
//                    }.show()
//                }
           // }


            if(intent.getStringExtra(AMOUNT)!=null) {
                startActivityForResult(intentFor<AddAccount>(
                        AddAccount.TXN_ID to txnId,
                        AddAccount.BIC to bic,
                        AddAccount.AMOUNT to amount,
                        AddAccount.ORDER_ID to order_id,
                        AddAccount.MERCHANT_NAME to merchName,
                        AddAccount.REMARKS to remarks,
                        AddAccount.APP_ID to getAppName(this),
                        AddAccount.MOBILE_NO to getMobileNo(this),
                        AddAccount.PSP_ID to getPspId(this),
                        AddAccount._LOCALE to getCurrentLocale(this),
                        AddAccount.TXN_TYPE to TransactionType.REGISTER_CARD_ACC_DETAIL.name).singleTop().clearTop(),
                        REQ_CODE_ADD_ACCOUNT
                )
            }else{

                startActivityForResult(intentFor<AddAccount>(
                        AddAccount.TXN_ID to txnId,
                        AddAccount.BIC to bic,
                        AddAccount.APP_ID to getAppName(this),
                        AddAccount.MOBILE_NO to getMobileNo(this),
                        AddAccount.PSP_ID to getPspId(this),
                        AddAccount._LOCALE to getCurrentLocale(this),
                        AddAccount.TXN_TYPE to TransactionType.REGISTER_CARD_ACC_DETAIL.name).singleTop().clearTop(),
                        REQ_CODE_ADD_ACCOUNT
                )

            }

//            startActivityForResult(intentFor<ConnectNewAccountActivity>(
//                    AuthenticateTxnActivity.TXN_ID to txnId,
//                    AuthenticateTxnActivity.BIC to bic,
//                    AuthenticateTxnActivity.APP_ID to getAppName(this),
//                    AuthenticateTxnActivity.MOBILE_NO to getMobileNo(this),
//                    AuthenticateTxnActivity.PSP_ID to getPspId(this),
//                    AuthenticateTxnActivity._LOCALE to getCurrentLocale(this),
//                    AuthenticateTxnActivity.TXN_TYPE to TransactionType.REGISTER_CARD_ACC_DETAIL.name).singleTop().clearTop(),
//                    REQ_CODE_ADD_ACCOUNT
//            )

            finish()
        }
    }
}

}
