package com.indepay.umps.pspsdk.transaction.history

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.gson.Gson
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.models.Transaction
import kotlinx.android.synthetic.main.activity_transaction_details.*
import java.text.SimpleDateFormat
import java.util.*

class TxnDetailsActivityUpdated : AppCompatActivity() {

        private val TXN_COLLECT = "COLLECT"
        private val TXN_PAY = "PAY"

        companion object {
            const val HISTORY_DETAILS = "history_details"
        }

        /**
         * This method is called by SDK itself to see detailed view of any transaction from the history list.
         *                  This activity provides the option to respond and postpone to pending transactions.
         * @param
         * @return TxnHistoryResponse
         */

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_transaction_details)//activity_txn_details

            val txnDetails = Gson().fromJson(intent.getStringExtra(HISTORY_DETAILS), Transaction::class.java)

            Log.d("sudhir","txnDetails::"+txnDetails)

            val sdfHeadertimestamp = SimpleDateFormat("EEEE, dd MMM HH:mm a")
            val date1timestamp = txnDetails.timestamp?.let { Date(it) }

            val date1:String = sdfHeadertimestamp.format(date1timestamp)

//                }


            Log.d("Sudhir","date1:"+date1)

            txt_date_time_txn_detail.text = date1

            txt_order_id.text = "Order Id: "+"123456"//txnDetails.orderid

            back_arrowimage.setOnClickListener {
                onBackPressed()
            }
            button_share.setOnClickListener {

                Log.d("Sudhir","Share button clicked:")

                val intent= Intent()
                intent.action= Intent.ACTION_SEND
                intent.putExtra(Intent.EXTRA_TEXT,"Merchant Name:"+txnDetails.counterpartName +"\n Status:"+txt_payment_success_txn_detail.text
                + "\n Order Id:")
                intent.type="text/plain"
                startActivity(Intent.createChooser(intent,"Share To:"))
            }
            val number = txnDetails.transactionId
            val mask = number!!.replace("\\w(?=\\w{4})".toRegex(), "*")
                Log.d("sudhir","transaction id:"+txnDetails.transactionId)


            val input = txnDetails.transactionId //input string

            var lastFourDigits = "" //substring containing last 4 characters
            var firstFourDigits = ""

            firstFourDigits = if (input.length>4){
                input.substring(0,4)
            }else{
                input
            }
            lastFourDigits = if (input.length > 4) {
                input.substring(input.length - 4)
            } else {
                input
            }
            Log.d("sudhir","firstFourDigits:"+firstFourDigits)

            Log.d("sudhir","lastFourDigits:"+lastFourDigits)

            Log.d("sudhir","mask id:"+mask)
            txt_id_txn_detail.text = "Transaction Id: "+firstFourDigits+"*************"+lastFourDigits
         /*     when {
                  txnDetails.selfInitiated -> if(txnDetails.txnType == TXN_PAY){
                      if(txnDetails.status == "P"){
                          lbl_paid_received.text = resources.getString(R.string.pay_to)
                          lbl_credited.text = resources.getString(R.string.debit_from)
                      }else{
                          lbl_paid_received.text = resources.getString(R.string.paid_to)
                          lbl_credited.text = resources.getString(R.string.debited_from)
                      }
                      img_txn_logo.setImageResource(R.drawable.ic_txn_pay_to)
                  }else{
                      if(txnDetails.status == "P"){
                          lbl_paid_received.text = resources.getString(R.string.receive_from)
                      }else{
                          lbl_paid_received.text = resources.getString(R.string.received_from)
                      }
                      img_txn_logo.setImageResource(R.drawable.ic_txn_received_from)
                  }
                  else -> if(txnDetails.txnType == TXN_COLLECT){
                      if(txnDetails.status == "P"){
                          lbl_paid_received.text = resources.getString(R.string.pay_to)
                          lbl_credited.text = resources.getString(R.string.debit_from)
                      }else{
                          lbl_paid_received.text = resources.getString(R.string.paid_to)
                          lbl_credited.text = resources.getString(R.string.debited_from)
                      }
                      img_txn_logo.setImageResource(R.drawable.ic_txn_pay_to)
                  }else{
                      if(txnDetails.status == "P"){
                          lbl_paid_received.text = resources.getString(R.string.receive_from)
                      }else{
                          lbl_paid_received.text = resources.getString(R.string.received_from)
                      }
                      img_txn_logo.setImageResource(R.drawable.ic_txn_received_from)
                  }
              }*/

              var billerName = "Merchant Name: "+txnDetails.counterpartName
//              //Mayank: To make it run for current configurations- from "ayopop-merchant" to "AYOPOPMERCH"
//              if (billerName.equals("AYOPOPMERCH", ignoreCase = true)) {
//                  billerName = "GAS"
//              } else if (billerName.isNullOrBlank()) {
//                  //History details for Unregistered users
//                  if(txnDetails.errorReason == resources.getString(R.string.invalid_user)){
//                      billerName = txnDetails.counterpartMobile
//                  }else
//                  billerName = txnDetails.counterpartContactMobile
//              }

//              txt_mobile.text = txnDetails.counterpartMobile
            txt_product.text = billerName
            txt_amt_value.text = "- Rp. " + txnDetails.amount
          //    txt_txn_msg.text = txnDetails.remarks
//              if (txnDetails.remarks.isNullOrBlank() || txnDetails.remarks.equals("null", ignoreCase = true)) {
//                  txn_message_container.visibility = View.GONE
//              } else {
//                  txn_message_container.visibility = View.VISIBLE
//              }
              if(txnDetails.success){
                  txt_payment_success_txn_detail.text = resources.getString(R.string.transaction_succ)
                  txt_payment_success_txn_detail.setTextColor(Color.GREEN)
              }else{
                  if(txnDetails.status == "P" && txnDetails.waitingForApproval){
                      txt_payment_success_txn_detail.text = resources.getString(R.string.transaction_pend)
                      txt_payment_success_txn_detail.setTextColor(Color.GRAY)
                    //  txn_receiver_container.visibility = View.GONE
                  }else if(txnDetails.status == "P" && !txnDetails.waitingForApproval){
                      txt_payment_success_txn_detail.text = resources.getString(R.string.transaction_in_pro)
                      txt_payment_success_txn_detail.setTextColor(Color.GRAY)
                  } else if(txnDetails.errorReason == "Request Declined") {
                      txt_payment_success_txn_detail.text = resources.getString(R.string.transaction_dec)
//                      txt_txn_msg.text = txnDetails.errorReason
//                      lbl_txn_msg.text = resources.getString(R.string.failure_reason)
                      txt_payment_success_txn_detail.setTextColor(Color.RED)
                  }else if(txnDetails.errorReason == "SPL Timeout"){
                      txt_payment_success_txn_detail.text = resources.getString(R.string.transaction_failed)
//                      txt_txn_msg.text = "Failed"
//                      lbl_txn_msg.text = resources.getString(R.string.failure_reason)
                      txt_payment_success_txn_detail.setTextColor(Color.RED)
                  }else{
                      txt_payment_success_txn_detail.text = resources.getString(R.string.transaction_failed)
//                      txt_txn_msg.text = txnDetails.errorReason
//                      lbl_txn_msg.text = resources.getString(R.string.failure_reason)
                      txt_payment_success_txn_detail.setTextColor(Color.RED)
                  }
              }
//              txt_txn_time.text = getFormattedDate(this, txnDetails.timestamp)
//              val picasso = getPicassoInstance(this, getPspSslConfig(this))
//              if(txnDetails.selfBIC != null) {
//                  val bic = txnDetails.selfBIC
//                  picasso.load(getPspBaseUrlForBankLogo(bic, getAccessToken(this), getAppName(this), getPspId(this))).placeholder(R.drawable.ic_bank_place_holder).into(img_bank_logo)
//                  txt_acc_number.text = txnDetails.selfAccountNumber
//                  txt_bic.text = txnDetails.selfBIC
//              }else{
//                  txn_receiver_container.visibility = View.GONE
//              }


//              if(txnDetails.counterpartMobile!=null) {
//                  if (txnDetails.txnType == TXN_COLLECT && txnDetails.status == "P" && txnDetails.waitingForApproval) {
//                      collect_approval_container.visibility = View.VISIBLE
//                  }
//              }

//              btn_respond.setOnClickListener {
//                  startActivity(intentFor<TxnCollectApproveActivity>(
//                          TxnCollectApproveActivity.COLLECT_DETAILS to intent.getStringExtra(HISTORY_DETAILS))
//                          .singleTop().clearTop())
//                  finish()
//              }

//              btn_postpone.setOnClickListener {
//                  finish()
//              }*/
        }

    fun maskCardNumber(cardNumber: String, mask: String): String {

        // format the number
        var index = 0
        val maskedNumber = StringBuilder()
        for (i in 0 until mask.length) {
            val c = mask[i]
            if (c == '#') {
                maskedNumber.append(cardNumber[index])
                index++
            } else if (c == 'x') {
                maskedNumber.append(c)
                index++
            } else {
                maskedNumber.append(c)
            }
        }

        // return the masked number
        return maskedNumber.toString()
    }
    }