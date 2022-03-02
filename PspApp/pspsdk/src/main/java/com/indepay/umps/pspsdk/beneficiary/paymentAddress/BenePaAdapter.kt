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

package com.indepay.umps.pspsdk.beneficiary.paymentAddress

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.callbacks.OnBeneficiaryListInteractionListener
import com.indepay.umps.pspsdk.models.Beneficiary
import com.indepay.umps.pspsdk.utils.AvatarImageView
import com.indepay.umps.pspsdk.utils.getNameInitials
import kotlinx.android.synthetic.main.bene_pa_list_item.view.*

class BenePaAdapter(private val benePaList: ArrayList<Beneficiary>, private val listener: OnBeneficiaryListInteractionListener) : RecyclerView.Adapter<BenePaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.bene_pa_list_item, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int = benePaList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = benePaList[position]
        holder.imgLogo.setText(getNameInitials(item.beneName.orEmpty()))
        holder.txtBaneName.text = item.beneName
        holder.txtBenePa.text = item.benePaymentAddr
        holder.bnePaView.setOnClickListener {
            listener.onBeneficiaryListInteraction(item)
        }
    }

    inner class ViewHolder(val bnePaView: View) : RecyclerView.ViewHolder(bnePaView) {
        val txtBaneName: TextView = bnePaView.txt_bene_name
        val txtBenePa: TextView = bnePaView.txt_bene_pa
        val imgLogo: AvatarImageView = bnePaView.bene_icon
    }
}