package com.indepay.umps.pspsdk.billPayment

import android.app.AlertDialog
import android.os.Bundle
import android.support.annotation.Keep
import android.util.Log
import android.view.LayoutInflater
import com.indepay.umps.pspsdk.BuildConfig
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.BillerInfoAdapter
import com.indepay.umps.pspsdk.adapter.NumberFilledAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnBillerAmountInteractionListner
import com.indepay.umps.pspsdk.models.PartnerProductData
import com.indepay.umps.pspsdk.registration.RegistrationActivity
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import com.indepay.umps.pspsdk.utils.getPicassoInstance
import com.indepay.umps.pspsdk.utils.getPspSslConfig
import com.indepay.umps.pspsdk.utils.getStringData
import kotlinx.android.synthetic.main.bill_payment_number_filled.*
import kotlinx.android.synthetic.main.biller_layout.*
import kotlinx.android.synthetic.main.your_purchase_custom_dialog.*
import kotlinx.android.synthetic.main.your_purchase_custom_dialog.view.*
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.singleTop

class BillNumberFilled:SdkBaseActivity(),OnBillerAmountInteractionListner {

    private lateinit var billerName:String
    private lateinit var billerData: ArrayList<PartnerProductData>
    private var billerArrayList: ArrayList<PartnerProductData> = ArrayList()


    @Keep
    companion object : SdkCommonMembers() {
        @Keep
        const val BILLERDATA = "biller_data"
        @Keep
        const val BILLERNAME = "biller_name"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bill_payment_number_filled)

        billerName = intent.getStringExtra(BillNumberFilled.BILLERNAME).toString()
        billerData = (intent.getSerializableExtra(BillNumberFilled.BILLERDATA) as ArrayList<PartnerProductData>?)!!

        toolbar_text.text = billerName



        val layoutManagerbiller = android.support.v7.widget.LinearLayoutManager(this)
        val itemDecorationbiller = android.support.v7.widget.DividerItemDecoration(this, layoutManagerbiller.orientation)

        amount_acc_list_container.layoutManager = layoutManagerbiller
        amount_acc_list_container.addItemDecoration(itemDecorationbiller)


        for(i in 0..billerData.size-1){

            Log.d("Sudhir","billerData::"+billerData.get(i).biller)
            billerArrayList.add(billerData.get(i))
        }
//        val biillerdistinct: List<String> = LinkedHashSet(billerArrayList).toMutableList()



        val picasso = getPicassoInstance(this@BillNumberFilled, getPspSslConfig(this@BillNumberFilled))
        val numberfillAdapter = NumberFilledAdapter(
                billpaymentList = billerArrayList,
                picassoInstance = picasso,
                listener = this@BillNumberFilled)
        amount_acc_list_container.adapter = numberfillAdapter

//        YourPurchase()

        back_arrowimage.setOnClickListener {

            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

     fun YourPurchase(billItem: PartnerProductData) {
        //Inflate the dialog with custom view
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.your_purchase_custom_dialog, null)
        //AlertDialogBuilder
//         mDialogView.dialog_title.setText(billItem.biller)
        mDialogView.card_header.setText(billItem.name)
         mDialogView.txt_mynumber.setText("Rp "+billItem.amount)
         mDialogView.txt_price.setText("Rp "+billItem.amount)
         mDialogView.txt_admin_fee.setText("Rp "+billItem.adminFee)
         mDialogView.txt_service_fee.setText("Rp "+0)
//         if(billItem.amount!!)
         mDialogView.txt_total.setText(billItem.amount)
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
//                .setTitle("Your Purchase")
          //show dialog
        val  mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.btn_paynow.setOnClickListener {
            //dismiss dialog


            startActivityForResult(intentFor<PaymentAccountActivity>(PaymentAccountActivity.AMOUNT to billItem.amount,
                    PaymentAccountActivity.ORDER_ID to "1012345",
                    PaymentAccountActivity.REMARKS to billItem.description, PaymentAccountActivity.MERCHANT_NAME to billItem.category,
                    RegistrationActivity.USER_NAME to getStringData(this, RegistrationActivity.USER_NAME),
                    RegistrationActivity.APP_ID to BuildConfig.APP_NAME,RegistrationActivity._LOCALE to "en",
                    RegistrationActivity.USER_MOBILE to getStringData(this,RegistrationActivity.USER_MOBILE)),   1002)





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

    override fun onBilleramountListItemClick(billItem: PartnerProductData) {
        Log.d("Sudhir","Bill Item name::"+billItem.name)
        Log.d("Sudhir","Bill Item amount::"+billItem.amount)
//        YourPurchase( billItem)
        startActivity(intentFor<PhoneNumberAccountNumberFilling>(PhoneNumberAccountNumberFilling.BILLERNAME to billItem.biller,PhoneNumberAccountNumberFilling.BILLERCODE to billItem.code).newTask().singleTop())

    }
}