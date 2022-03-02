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

package com.indepay.umps.pspsdk.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.accountSetup.BankAccountListActivity
import com.indepay.umps.pspsdk.models.BankAccount
import com.indepay.umps.pspsdk.utils.getPspBaseUrlForBankLogo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bank_account_list_item.view.*

class BankAccountListAdapter(private val bankAcList: ArrayList<BankAccount>, private val listener: BankAccountListActivity, private val picassoInstance: Picasso, private val accessToken: String, private val appName: String, private val custPspId: String?) : RecyclerView.Adapter<BankAccountListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.bank_account_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bankAcList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.accountItem = bankAcList[position]
        picassoInstance.load(getPspBaseUrlForBankLogo(holder.accountItem.bic, accessToken, appName, custPspId)).placeholder(R.drawable.ic_bank_place_holder).into(holder.bankAccLogo)
        holder.bankName.text = holder.accountItem.bankName
        holder.accountType.text = holder.accountItem.accountType
        holder.bankBic.text = holder.accountItem.bic
        holder.accountNo.text = holder.accountItem.accountNumber
        if(holder.accountItem.mpinAvailable){
            holder.mpinStatus.text = "Mpin already exists!"
            holder.mpinStatus.setTextColor(Color.GREEN)
        }else{
            holder.mpinStatus.text = "Mpin not available."
            holder.mpinStatus.setTextColor(Color.GRAY)
        }
        if(holder.accountItem.alreadyMappedAccount){
            holder.accMappingStatus.text = "Account already mapped!"
            holder.accMappingStatus.setTextColor(Color.GREEN)
        }else{
            holder.accMappingStatus.text = "Account not mapped."
            holder.accMappingStatus.setTextColor(Color.GRAY)
            holder.accountView.setOnClickListener {
                listener.onBankAccountListInteraction(holder.accountItem)
            }
        }

    }


    inner class ViewHolder(val accountView: View) : RecyclerView.ViewHolder(accountView) {
        val bankAccLogo: ImageView = accountView.img_bank_acc_logo
        val accountType: TextView = accountView.txt_bank_acc_type
        val bankBic: TextView = accountView.txt_bank_acc_bic
        val bankName: TextView = accountView.txt_bank_name
        val accountNo: TextView = accountView.txt_bank_acc_no
        val accMappingStatus: TextView = accountView.lbl_account_mapping_status
        val mpinStatus: TextView = accountView.lbl_mpin_status
        lateinit var accountItem: BankAccount
    }
}