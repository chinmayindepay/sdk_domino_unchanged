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

package com.indepay.umps.pspsdk.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.callbacks.OnTxnHistoryListInteractionListener
import kotlinx.android.synthetic.main.row_header.view.*
import kotlinx.android.synthetic.main.txn_history_list_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import com.indepay.umps.pspsdk.models.Transaction as Transaction

data class TxnHistoryAdapter (private val txnHistoryList: ArrayList<Transaction>, private val listener: OnTxnHistoryListInteractionListener): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TXN_COLLECT = "COLLECT"
    private val TXN_PAY = "PAY"

    private val HEADERVIEW = 0
    private val DATAVIEW = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

     //   Log.d("Sudhir","viewType::"+viewType)
        return if (viewType === DATAVIEW) {

            // view for normal data.
            val view: View = LayoutInflater.from(parent.getContext()).inflate(R.layout.txn_history_list_item, parent, false)
            DataViewHolder(view)
        } else {

            // view type for month or date header
            val view: View = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_header, parent, false)
            HeaderViewHolder(view)
        }
//        val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.txn_history_list_item, parent, false)
//        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val txnItem: Transaction = txnHistoryList [position]
        // holder.txtTxnTime.text = getFormattedDate(holder.historyView.context, holder.txnItem.timestamp)
        if (holder.getItemViewType() == HEADERVIEW) {
            if(position == 0){
                (holder as HeaderViewHolder).headertext.setText("Recent")
            }else {

                (holder as HeaderViewHolder).headertext.setText(txnItem.name)
            }
            //fill data for normal view


        } else {
            //set your date or month header

//            headerView


            val sdf = SimpleDateFormat("dd MMM YYYY HH:mm")
            val netDate = txnItem.timestamp?.let { Date(it) }
            val date = sdf.format(netDate)

//        Log.e("Tag","Formatted Date"+date)
            (holder as DataViewHolder).txtTxnTime.setText(date)

            val sdfHeader = SimpleDateFormat("MMMM")
            val netDateHeader = txnItem.timestamp?.let { Date(it) }
            val dateHeader = sdfHeader.format(netDateHeader)
//        Log.e("Tag"," Date Header:::"+dateHeader)


//        Log.d("SUdhir","Date::"+holder.txtTxnTime.text)
            var billerName = txnItem.counterpartName// For demo only.
            if (billerName.equals("ayopop-merchant", ignoreCase = true)) {
                billerName = "GAS"
            } else if (billerName.isNullOrBlank()) {
                //History for Unregistered users
                if (txnItem.errorReason == "Invalid User") {
                    billerName = txnItem.counterpartMobile
                } else
                    billerName = txnItem.counterpartContactMobile
            }
            (holder as DataViewHolder).txtName.text = "Paid to : " +billerName
            (holder as DataViewHolder).txtTxnAmount.text = "- Rp " + txnItem.amount
            (holder as DataViewHolder).historyView.setOnClickListener {
                listener.onTxnHistoryListInteraction(txnItem)
            }
            when {
                txnItem.success -> {
                    (holder as DataViewHolder).txtTxnStatus.setTextColor(Color.WHITE)
                    (holder as DataViewHolder).txtTxnStatus.text = "Completed"
                }
                else -> {
                    if (txnItem.status == "P") {
                        (holder as DataViewHolder).txtTxnStatus.setTextColor(Color.GRAY)
                        (holder as DataViewHolder).txtTxnStatus.text = "Pending"
                    } else if (txnItem.errorReason == "Request Declined") {
                        (holder as DataViewHolder).txtTxnStatus.setTextColor(Color.RED)
                        (holder as DataViewHolder).txtTxnStatus.text = "Declined"
                    } else {
                        (holder as DataViewHolder).txtTxnStatus.setTextColor(Color.RED)
                        (holder as DataViewHolder).txtTxnStatus.text = "Failed"
                    }
                }
            }
            (holder as DataViewHolder).txnLogo.setImageResource(R.drawable.ic_txn_pay_to)
//            when {
//                txnItem.selfInitiated -> when (TXN_COLLECT) {
//                    txnItem.txnType -> {
//                        (holder as DataViewHolder).txnLogo.setImageResource(R.drawable.ic_txn_received_from)
//                        if (txnItem.status == "P") {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Receive from:"
//                        } else {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Received from:"
//                        }
//                    }
//                    else -> {
//                        (holder as DataViewHolder).txnLogo.setImageResource(R.drawable.ic_txn_pay_to)
//                        if (txnItem.status == "P") {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Pay to:"
//                        } else {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Paid to:"
//                        }
//
//                    }
//                }
//                else -> when (TXN_PAY) {
//                    txnItem.txnType -> {
//                        (holder as DataViewHolder).txnLogo.setImageResource(R.drawable.ic_txn_received_from)
//                        if (txnItem.status == "P") {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Receive from:"
//                        } else {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Received from:"
//                        }
//                    }
//                    else -> {
//                        (holder as DataViewHolder).txnLogo.setImageResource(R.drawable.ic_txn_pay_to)
//                        if (txnItem.status == "P") {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Pay to:"
//                        } else {
//                            (holder as DataViewHolder).txtPaidReceivedLbl.text = "Paid to:"
//                        }
//                    }
//                }
//            }
        }
    }

    override fun getItemCount(): Int = txnHistoryList.size

    override fun getItemViewType(position: Int): Int {
        return if (txnHistoryList.get(position).header == true) {
            HEADERVIEW
        } else {
            DATAVIEW
        }
    }
    inner class DataViewHolder(val historyView: View) : ViewHolder(historyView) {

        val txnLogo: ImageView = historyView.img_txn_logo
//        val txtPaidReceivedLbl: TextView = historyView.lbl_paid_received
        val txtName: TextView = historyView.txt_name
        val txtTxnTime: TextView = historyView.txt_txn_time
        val txtTxnAmount: TextView = historyView.txt_amount
        val txtTxnStatus: TextView = historyView.txt_txn_status
//        lateinit var txnItem: Transaction
    }

    inner class HeaderViewHolder(val headerView: View) : ViewHolder(headerView) {

        val headertext: TextView = headerView.header

    }


}