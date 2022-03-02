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
 *  * 16-10-2019     Mayank D   Jira Ticket RR-102
 *  *
 *  *****************************************************************************
 */

package com.indepay.umps.pspsdk.transaction.history

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.gson.Gson
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.models.Transaction
import com.indepay.umps.pspsdk.transaction.collectApprove.TxnCollectApproveActivity
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_txn_details.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop

class TxnDetailsActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_txn_details)

        val txnDetails = Gson().fromJson(intent.getStringExtra(HISTORY_DETAILS), Transaction::class.java)
        txt_txn_id.text = txnDetails.transactionId
        when {
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
        }

        var billerName = txnDetails.counterpartName// For demo only.
        //Mayank: To make it run for current configurations- from "ayopop-merchant" to "AYOPOPMERCH"
        if (billerName.equals("AYOPOPMERCH", ignoreCase = true)) {
            billerName = "GAS"
        } else if (billerName.isNullOrBlank()) {
            //History details for Unregistered users
            if(txnDetails.errorReason == resources.getString(R.string.invalid_user)){
                billerName = txnDetails.counterpartMobile
            }else
            billerName = txnDetails.counterpartContactMobile
        }

        txt_mobile.text = txnDetails.counterpartMobile
        txt_name.text = billerName
        txt_amount.text = "Invoice. " + txnDetails.amount
        txt_txn_msg.text = txnDetails.remarks
        if (txnDetails.remarks.isNullOrBlank() || txnDetails.remarks.equals("null", ignoreCase = true)) {
            txn_message_container.visibility = View.GONE
        } else {
            txn_message_container.visibility = View.VISIBLE
        }
        if(txnDetails.success){
            txt_txn_status.text = resources.getString(R.string.transaction_succ)
            txt_txn_status.setTextColor(Color.GREEN)
        }else{
            if(txnDetails.status == "P" && txnDetails.waitingForApproval){
                txt_txn_status.text = resources.getString(R.string.transaction_pend)
                txt_txn_status.setTextColor(Color.GRAY)
                txn_receiver_container.visibility = View.GONE
            }else if(txnDetails.status == "P" && !txnDetails.waitingForApproval){
                txt_txn_status.text = resources.getString(R.string.transaction_in_pro)
                txt_txn_status.setTextColor(Color.GRAY)
            } else if(txnDetails.errorReason == "Request Declined") {
                txt_txn_status.text = resources.getString(R.string.transaction_dec)
                txt_txn_msg.text = txnDetails.errorReason
                lbl_txn_msg.text = resources.getString(R.string.failure_reason)
                txt_txn_status.setTextColor(Color.RED)
            }else if(txnDetails.errorReason == "SPL Timeout"){
                txt_txn_status.text = resources.getString(R.string.transaction_failed)
                txt_txn_msg.text = "Failed"
                lbl_txn_msg.text = resources.getString(R.string.failure_reason)
                txt_txn_status.setTextColor(Color.RED)
            }else{
                txt_txn_status.text = resources.getString(R.string.transaction_failed)
                txt_txn_msg.text = txnDetails.errorReason
                lbl_txn_msg.text = resources.getString(R.string.failure_reason)
                txt_txn_status.setTextColor(Color.RED)
            }
        }
        txt_txn_time.text = getFormattedDate(this, txnDetails.timestamp)
        val picasso = getPicassoInstance(this, getPspSslConfig(this))
        if(txnDetails.selfBIC != null) {
            val bic = txnDetails.selfBIC
            picasso.load(getPspBaseUrlForBankLogo(bic, getAccessToken(this), getAppName(this), getPspId(this))).placeholder(R.drawable.ic_bank_place_holder).into(img_bank_logo)
            txt_acc_number.text = txnDetails.selfAccountNumber
            txt_bic.text = txnDetails.selfBIC
        }else{
            txn_receiver_container.visibility = View.GONE
        }


        if(txnDetails.counterpartMobile!=null) {
            if (txnDetails.txnType == TXN_COLLECT && txnDetails.status == "P" && txnDetails.waitingForApproval) {
                collect_approval_container.visibility = View.VISIBLE
            }
        }

        btn_respond.setOnClickListener {
            startActivity(intentFor<TxnCollectApproveActivity>(
                    TxnCollectApproveActivity.COLLECT_DETAILS to intent.getStringExtra(HISTORY_DETAILS))
                    .singleTop().clearTop())
            finish()
        }

        btn_postpone.setOnClickListener {
            finish()
        }
    }
}
