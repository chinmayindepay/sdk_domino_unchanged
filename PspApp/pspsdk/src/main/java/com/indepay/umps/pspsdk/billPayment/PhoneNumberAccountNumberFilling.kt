package com.indepay.umps.pspsdk.billPayment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.Keep
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import com.google.gson.GsonBuilder
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.models.BillCheckResponse
import com.indepay.umps.pspsdk.models.PartnerProductData
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import com.indepay.umps.pspsdk.utils.getStringData
import kotlinx.android.synthetic.main.bill_payment_number_filled.toolbar_text
import kotlinx.android.synthetic.main.phone_number_account_number_layout.*
import kotlinx.android.synthetic.main.your_purchase_custom_dialog.view.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.jetbrains.anko.intentFor
import java.io.IOException

class PhoneNumberAccountNumberFilling : SdkBaseActivity() {


    private lateinit var billerName:String
    private lateinit var billerCode:String
    private lateinit var billerData: ArrayList<PartnerProductData>
    private var billerArrayList: ArrayList<PartnerProductData> = ArrayList()
    private var client = OkHttpClient()

    private lateinit var accountNumber: String


    @Keep
    companion object : SdkCommonMembers() {
        @Keep
        const val BILLERDATA = "biller_data"

        @Keep
        const val BILLERNAME = "biller_name"
        @Keep
        const val BILLERCODE = "biller_code"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_number_account_number_layout)

        billerName = intent.getStringExtra(PhoneNumberAccountNumberFilling.BILLERNAME).toString()
//        billerData = (intent.getSerializableExtra(PhoneNumberAccountNumberFilling.BILLERDATA) as ArrayList<PartnerProductData>?)!!
        billerCode = intent.getStringExtra(PhoneNumberAccountNumberFilling.BILLERCODE).toString()

        toolbar_text.text = billerName

        et_pnumber_account_number.setText("08123000000")
        et_pnumber_account_number.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(str: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(str: CharSequence?, start: Int, before: Int, count: Int) {}
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(str: Editable) {
                if (et_pnumber_account_number.text.length >= 10) {
                    btn_next.setBackgroundColor(ContextCompat.getColor(this@PhoneNumberAccountNumberFilling,R.color.color_7))
                } else {
                    btn_next.setBackgroundColor(Color.GRAY)

                }
            }
        })

        if (et_pnumber_account_number.text.length >= 10) {
            btn_next.setBackgroundColor(ContextCompat.getColor(this@PhoneNumberAccountNumberFilling,R.color.color_7))
        } else {
            btn_next.setBackgroundColor(Color.GRAY)

        }
        btn_next.setOnClickListener {
            accountNumber = et_pnumber_account_number.text.toString()

            Log.d("Sudhir","Next CLicked")
            BillCheck(billerCode,accountNumber)

//            if(et_pnumber_account_number.text.length==12) {
//                YourPurchase(billItem)
//
////                startActivity(intentFor<BillNumberFilled>(BillNumberFilled.BILLERNAME to billerName, BillNumberFilled.BILLERDATA to billerData).newTask().singleTop())
//            }

        }


        back_arrowimage.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }



    fun BillCheck(productcode: String, accountNumber: String) {
        val  url:String = "https://dev.tara.app/v0.1/tara/erp/biller/ayopop/bill/check"
//        val  url:String = "https://qa.tara.app/v0.1/tara/erp/biller/ayopop/bill/check"

        val json = "{\"partnerId\":\"null\",\"accountNumber\":\"${accountNumber}\",\"productCode\":\"${productcode}\"}}"

        Log.d("Sudhir","URL ::"+url)
        Log.d("Sudhir","BillCheck json body::"+json)

        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
                .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
                .header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJJbmRlcGF5Iiwic3ViIjoiVGFyYS1Ub2tlbiIsImN1c3RvbWVySWQiOjIsImp0aSI6ImJhZWE4ZTUzLWViMzUtNGJmYy05MjQ1LTRhMTU4MTI3MDg5MyIsImlhdCI6MTYxOTQyMjAyMSwiZXhwIjoxNjE5NTA4NDIxfQ.vX6jZEUkfIY2F88_2gi2nVnhzEf_VZrOKXZwtzeLr3s")
                .url(url)
                .post(body)
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {}
            override fun onResponse(call: okhttp3.Call, response: Response) {
//                println(response.body()?.string())
                Log.d("Sudhir", "status code::" + response.code)
                val jsonData: String? = response.body?.string()
                val gson = GsonBuilder().create()
                val billCheckModel = gson.fromJson(jsonData, BillCheckResponse::class.java)

                Log.d("Sudhir", "BillCHeck inquiryId ::" + billCheckModel.data.inquiryId)
                Log.d("Sudhir", "BillCHeck accountNumber::" + billCheckModel.data.accountNumber)
                Log.d("Sudhir", "BillCHeck amount::" + billCheckModel.data.amount)
                Log.d("Sudhir", "BillCHeck category::" + billCheckModel.data.category)

                Log.d("Sudhir", "BillCHeck customerName ::" + billCheckModel.data.customerName)
                Log.d("Sudhir", "BillCHeck denom::" + billCheckModel.data.denom)
                Log.d("Sudhir", "BillCHeck processingFee::" + billCheckModel.data.processingFee)
                Log.d("Sudhir", "BillCHeck productCode::" + billCheckModel.data.productCode)

                Log.d("Sudhir", "BillCHeck productName ::" + billCheckModel.data.productName)
                Log.d("Sudhir", "BillCHeck totalAdmin::" + billCheckModel.data.totalAdmin)
                Log.d("Sudhir", "BillCHeck validity::" + billCheckModel.data.validity)

//                BillPayment(billCheckModel.data.inquiryId!!, billCheckModel.data.accountNumber!!, billCheckModel.data.productCode!!,
//                        billCheckModel.data.amount!!)

                if (et_pnumber_account_number.text.length >= 10) {
                    Handler(Looper.getMainLooper()).post {

                        YourPurchase(billCheckModel)
                    }
//                startActivity(intentFor<BillNumberFilled>(BillNumberFilled.BILLERNAME to billerName, BillNumberFilled.BILLERDATA to billerData).newTask().singleTop())
                }

            }
        })


    }


    fun YourPurchase(billCheckModel: BillCheckResponse) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.your_purchase_custom_dialog, null)
        //AlertDialogBuilder
//         mDialogView.dialog_title.setText(billItem.biller)
        mDialogView.card_header.setText(billCheckModel.data.productName)
        mDialogView.txt_mynumber.setText("Rp "+billCheckModel.data.amount)
        mDialogView.txt_price.setText("Rp "+billCheckModel.data.amount)
        mDialogView.txt_admin_fee.setText("Rp "+billCheckModel.data.totalAdmin)
        mDialogView.txt_service_fee.setText("Rp "+billCheckModel.data.processingFee)
//         if(billItem.amount!!)
       val total:Long = billCheckModel.data.amount!! + billCheckModel.data.totalAdmin!! + billCheckModel.data.processingFee!!
        mDialogView.txt_total.setText(total.toString())
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
//                .setTitle("Your Purchase")
        //show dialog
        val  mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.btn_paynow.setOnClickListener {
            //dismiss dialog

                Log.d("Sudhir","Total:"+total)
            startActivityForResult(intentFor<PaymentAccountActivity>(PaymentAccountActivity.AMOUNT to total.toString(),
                    PaymentAccountActivity.ORDER_ID to billCheckModel.data.inquiryId,
                    PaymentAccountActivity.REMARKS to "", PaymentAccountActivity.MERCHANT_NAME to billCheckModel.data.category,
                    RegistrationActivity.USER_NAME to getStringData(this, RegistrationActivity.USER_NAME),
                    RegistrationActivity.APP_ID to BuildConfig.APP_NAME, RegistrationActivity._LOCALE to "en",
                    RegistrationActivity.USER_MOBILE to getStringData(this, RegistrationActivity.USER_MOBILE)),   1002)





            mAlertDialog.dismiss()
//            //get text from EditTexts of custom layout
//            val name = mDialogView.dialogNameEt.text.toString()
//            val email = mDialogView.dialogEmailEt.text.toString()
//            val password = mDialogView.dialogPasswEt.text.toString()
//            //set the input text in TextView
//            mainInfoTv.setText("Name:"+ name +"\nEmail: "+ email +"\nPassword: "+ password)
        }
        //cancel button click of custom layout
//        mDialogView.dialogCancelBtn.setOnClickListener {
//            //dismiss dialog
//            mAlertDialog.dismiss()
//        }
    }


}