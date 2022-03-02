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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.callbacks.OnListItemInteractionListener
import com.indepay.umps.pspsdk.models.PaAccountModel
import kotlinx.android.synthetic.main.acc_selection_adapter.view.*

class AccountSelectionAdapter(private val accountList: List<PaAccountModel>, private val listener: OnListItemInteractionListener) : RecyclerView.Adapter<AccountSelectionAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.acc_selection_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return accountList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = accountList[position]
        holder.bankName.text = item.bic
        holder.accountNo.text = item.accountNumber
        holder.bankSelectionRadioButton.setOnClickListener { listener.onListItemClick.invoke(item) }
        if(null != item.isDefault && item.isDefault == true){
            holder.bankSelectionRadioButton.isChecked = true
        }
    }


    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val bankName: TextView = mView.bankName
        val accountNo: TextView = mView.accountNo
        val bankSelectionRadioButton : RadioButton = mView.bank_selection_radio_button
    }

}
