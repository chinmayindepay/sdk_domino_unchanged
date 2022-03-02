package com.indepay.umps.pspsdk.billPayment

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.annotation.Keep
import android.util.Log
import com.google.gson.GsonBuilder
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.BillPaymentPartnerProductPostpaidAdapter
import com.indepay.umps.pspsdk.adapter.BillPaymentPartnerProductPrepaidAdapter
import com.indepay.umps.pspsdk.adapter.SpaceItemDecoration
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.billPayment.BillerInfoActivity.Companion.BILLERDATA
import com.indepay.umps.pspsdk.callbacks.OnBillPaymentItemSelectionListner
import com.indepay.umps.pspsdk.models.BillCheckResponse
import com.indepay.umps.pspsdk.models.BillPaymentPartnerProductResponse
import com.indepay.umps.pspsdk.models.PartnerProductData
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import com.indepay.umps.pspsdk.utils.getPicassoInstance
import com.indepay.umps.pspsdk.utils.getPspSslConfig
import kotlinx.android.synthetic.main.activity_bill_payments.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.jetbrains.anko.*
import java.io.IOException

class BillPaymentActivity : SdkBaseActivity(),OnBillPaymentItemSelectionListner {

    private val BILL_PAYMENT_TRANSACTION = 1111

    private var client = OkHttpClient()

    private var billpaymentproductdata: ArrayList<PartnerProductData> = ArrayList()
    private var prepaidCategeoryArrayList: ArrayList<String> = ArrayList()
    private var typeArrayList: ArrayList<String> = ArrayList()
    private var postpaidCategeoryArrayList: ArrayList<String> = ArrayList()
    lateinit var BillModelData: BillPaymentPartnerProductResponse
   lateinit var arrayofImages : Array<Int>

    @Keep
    companion object : SdkCommonMembers() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_payments)


        PartnerProducts("https://dev.tara.app/v0.1/tara/erp/biller/ayopop/partner/products")
//        PartnerProducts("https://qa.tara.app/v0.1/tara/erp/biller/ayopop/partner/products")


//        BillCheck("https://dev.tara.app/v0.1/tara/erp/biller/ayopop/bill/check")

//        BillPayment("https://dev.tara.app/v0.1/tara/erp/biller/ayopop/bill/payment")


        val layoutManagerPrepaid = android.support.v7.widget.GridLayoutManager(this, 4)
        val itemDecorationprepaid = android.support.v7.widget.DividerItemDecoration(this, layoutManagerPrepaid.orientation)

        prepaid_acc_list_container.layoutManager = layoutManagerPrepaid
//        prepaid_acc_list_container.addItemDecoration(itemDecorationprepaid)
        prepaid_acc_list_container.addItemDecoration( SpaceItemDecoration(10))

//        }

        val layoutManagerPostpaid = android.support.v7.widget.GridLayoutManager(this, 4)
        val itemDecorationpostpaid = android.support.v7.widget.DividerItemDecoration(this, layoutManagerPrepaid.orientation)

        postpaid_acc_list_container.layoutManager = layoutManagerPostpaid
//        postpaid_acc_list_container.addItemDecoration(itemDecorationpostpaid)
        postpaid_acc_list_container.addItemDecoration( SpaceItemDecoration(10))


         arrayofImages = arrayOf(
                 R.drawable.pulsa,
                R.drawable.internet,
               R.drawable.e_money,
                R.drawable.pascabayar,
                 R.drawable.internet,
                 R.drawable.e_money
        )



//        img_pulsa.setOnClickListener {
//
//            startActivity(intentFor<BankBillPaymentSelection>().newTask().singleTop())
//
//        }

        back_arrowimage.setOnClickListener {
            onBackPressed()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
      }

    fun PartnerProducts(url: String) {
        val json = "{\"partnerId\":\"null\"}}"

        Log.d("Sudhir","URL ::"+url)
        Log.d("Sudhir","PartnerProducts json body::"+json)

        val body = RequestBody.create(("application/json; charset=utf-8").toMediaType(), json)
        val request = Request.Builder()
                .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
                .header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJJbmRlcGF5Iiwic3ViIjoiVGFyYS1Ub2tlbiIsImN1c3RvbWVySWQiOjIsImp0aSI6ImJhZWE4ZTUzLWViMzUtNGJmYy05MjQ1LTRhMTU4MTI3MDg5MyIsImlhdCI6MTYxOTQyMjAyMSwiZXhwIjoxNjE5NTA4NDIxfQ.vX6jZEUkfIY2F88_2gi2nVnhzEf_VZrOKXZwtzeLr3s")
                .url(url)
                .post(body)
                .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                TODO("Not yet implemented")
            }

//            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: okhttp3.Call, response: Response) {
//                println(response.body()?.string())
    Log.d("Sudhir", "status code::" + response.code)


    val jsonData: String? = response.body?.string()
    val gson = GsonBuilder().create()
    val BillModel = gson.fromJson(jsonData, BillPaymentPartnerProductResponse::class.java)

    if (BillModel.data.isNotEmpty()) {
        billpaymentproductdata = BillModel.data
        BillModelData = BillModel

        for (i in 0..BillModel.data.size - 1) {
            typeArrayList.add(BillModel.data.get(i).type.toString())

            if (BillModel.data.get(i).type.equals("prepaid")) {
                if (BillModel.data.get(i).category.equals("Pulsa") || BillModel.data.get(i).category.equals(
                        "Paket Data"
                    )
                ) {
                    prepaidCategeoryArrayList.add("Pulsa/Paket Data")
                } else {
                    prepaidCategeoryArrayList.add(BillModel.data.get(i).category.toString())
                }

            } else {
                postpaidCategeoryArrayList.add(BillModel.data.get(i).category.toString())
            }
        }

        val prepaidCategeorydistinct: List<String> =
            LinkedHashSet(prepaidCategeoryArrayList).toMutableList()
        val postpaidCategeorydistinct: List<String> =
            LinkedHashSet(postpaidCategeoryArrayList).toMutableList()
//                    val distinct: List<String> = LinkedHashSet(typeArrayList).toMutableList()
//                    Log.d("Sudhir", "type distinct::" + distinct.size)

//                    for(i in 0..BillModel.data.size-1) {
//                        if (BillModel.data.get(i).type.equals("prepaid")) {
//                            prepaidArrayList.add(BillModel.data.get(i))
//
//                        }
//                        else{
//                            postpaidArrayList.add(BillModel.data.get(i))
//                        }
//                    }
        Handler(Looper.getMainLooper()).post {
            lbl_bill_payments.setText(R.string.prepaid)
            lbl_postpaid.setText(R.string.postpaid)

            val picasso = getPicassoInstance(
                this@BillPaymentActivity,
                getPspSslConfig(this@BillPaymentActivity)
            )
            val billPaymentPartnerProductPrepaidListAdapter =
                BillPaymentPartnerProductPrepaidAdapter(
                    billpaymentList = prepaidCategeorydistinct,
                    arrayofImages = arrayofImages,
                    picassoInstance = picasso,
                    listener = this@BillPaymentActivity
                )
            prepaid_acc_list_container.adapter = billPaymentPartnerProductPrepaidListAdapter


            val billPaymentPartnerProducPostpaidListAdapter =
                BillPaymentPartnerProductPostpaidAdapter(
                    billpaymentList = postpaidCategeorydistinct,
                    picassoInstance = picasso,
                    listener = this@BillPaymentActivity
                )
            postpaid_acc_list_container.adapter = billPaymentPartnerProducPostpaidListAdapter
        }

    } else {
        alert(
            resources.getString(R.string.failed_to_fetch),
            resources.getString(R.string.no_data)
        ) {
            okButton { finish() }
            isCancelable = false
        }.show()
    }


}


        })


    }


    fun BillCheck(productcode: String) {
      val  url:String = "https://dev.tara.app/v0.1/tara/erp/biller/ayopop/bill/check"
        val json = "{\"partnerId\":\"null\",\"accountNumber\":\"0812300000\",\"productCode\":\"${productcode}\"}}"

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
//                                billCheckModel.data.amount!!)
            }
        })


    }

    fun BillPayment(inquiryId: Long,accountNumber: String, productcode: String,amount: Long) {
       val url: String = "https://dev.tara.app/v0.1/tara/erp/biller/ayopop/bill/payment"
        val json = "{\"inquiryId\":\"${inquiryId}\",\"accountNumber\":\"${accountNumber}\",\"productCode\":\"${productcode}\"," +
                "\"amount\":\"${amount}\",\"refNumber\":\"fa707e6cd83b4c0ca84b13d76e602417\",\"partnerId\":\"null\",\"buyerDetails\":{\"buyerEmail\":\"sudhir.kumar@indepay.com}\"," +
                "\"publicBuyerId\":\"${60}\"},\"optionalData\":{\"taraTransactionId\":\"TRX-4918af440b424a38b83be773bd930fa7\"}}"


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
                println(response.body?.string())
                Log.d("Sudhir", "status code::" + response.code)



//                Response
//                {
//                    "responseCode":4,
//                    "success":true,
//                    "message":{
//                    "ID":"Transaksi Anda Telah Berhasil",
//                    "EN":"Your transaction was successful"
//                },
//                    "data":{
//                    "refNumber":"fa707e6cd83b4c0ca84b13d76e602417",
//                    "transactionId":51883,
//                    "accountNumber":"0812300000",
//                    "amount":10100,
//                    "totalAdmin":0,
//                    "processingFee":0,
//                    "denom":"",
//                    "productCode":"PLTK10",
//                    "productName":"Telkomsel Rp 10.000 TKD...",
//                    "category":"Pulsa",
//                    "token":"01988200012015845492",
//                    "customerDetails":[
//                    {
//                        "key":"Nama Pelanggan",
//                        "value":"N/A"
//                    },
//                    {
//                        "key":"Waktu & Tanggal Pembayaran",
//                        "value":"03 May 2021 | 13:19"
//                    },
//                    {
//                        "key":"Nomor Kontrak",
//                        "value":"0812300000"
//                    }
//                    ],
//                    "billDetails":[
//
//                    ],
//                    "productDetails":[
//                    {
//                        "key":"SN Number",
//                        "value":"01988200012015845492"
//                    }
//                    ],
//                    "extraFields":[
//
//                    ]
//                }
//                }

            }


        })


    }

//    bill payment
//{
//    "inquiryId": 613140,
//    "accountNumber": "0812300000",
//    "productCode": "PLTK10",
//    "amount": 10100,
//    "refNumber": "fa707e6cd83b4c0ca84b13d76e602418",
//    "partnerId": "ALcTM9Yrztyh",
//    "buyerDetails": {
//    "buyerEmail": "kaushal.mewar@indepay.com",
//    "publicBuyerId": "60"
//},
//    "CallbackUrls": [
//    "https://dev.tara.app:80/v0.1/tara/erp/biller/callback"
//    ],
//    "optionalData": {
//    "taraTransactionId": "TRX-4918af440b424a38b83be773bd930fa7"
//}
//}


    fun getrun(url: String) {
        Log.d("Sudhir","getrun url::"+url)
        val request = Request.Builder()
                .header("User-Agent", "SDK/1.0 Dominos OS:Android/iOS")
                .url(url)
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {}
            override fun onResponse(call: okhttp3.Call, response: Response) {
                println(response.body?.string())
                Log.d("Sudhir", "status code::" + response.code)


            }
        })
    }

//    override fun onBillPaymentPartnerListItemClick(billItem: String) {
//
//        Log.d("Sudhir","Selected Name::"+billItem)
////        Log.d("Sudhir","Selected Code::"+billItem)
//
////        BillCheck(billItem.code!!)
//
//    }

    override fun onBillPaymentPartnerPrepaidListItemClick(billItem: String) {
        Log.d("Sudhir","Selected Name::"+billItem)
         val partnerProductList: ArrayList<PartnerProductData> = ArrayList()

        for(i in 0..BillModelData.data.size-1) {
            if(billItem.equals("Pulsa/Paket Data")) {
                if (("Pulsa".equals(BillModelData.data.get(i).category)) || ("Paket Data".equals(BillModelData.data.get(i).category))) {
                    partnerProductList.add(BillModelData.data.get(i))
                }
            }else if(BillModelData.data.get(i).type.equals("prepaid")) {
                 if (billItem.equals(BillModelData.data.get(i).category)) {

                    partnerProductList.add(BillModelData.data.get(i))

                }
            }

       }
        startActivity(intentFor<BillerInfoActivity>(BillerInfoActivity.CATEGEORYNAME to billItem,BILLERDATA to partnerProductList).newTask().singleTop())

    }

    override fun onBillPaymentPartnerPostpaidListItemClick(billItem: String) {
        Log.d("Sudhir","Selected Name::"+billItem)

        val partnerProductList: ArrayList<PartnerProductData> = ArrayList()

        for(i in 0..BillModelData.data.size-1) {
            if(BillModelData.data.get(i).type.equals("postpaid")) {
                if (billItem.equals(BillModelData.data.get(i).category)) {

                    partnerProductList.add(BillModelData.data.get(i))

                }
            }
        }
        startActivity(intentFor<BillerInfoActivity>(BillerInfoActivity.CATEGEORYNAME to billItem,BILLERDATA to partnerProductList).newTask().singleTop())

    }

}

