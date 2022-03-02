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

import android.os.SystemClock
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.callbacks.OnEcosystemBankItemSlectionListener
import com.indepay.umps.pspsdk.models.EcosystemBankResponse
import com.indepay.umps.pspsdk.utils.getPspBaseUrlForBankLogo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.ecosystem_bank_list_item.view.*

class EcosystemBanksAdapter(private val bankList: ArrayList<EcosystemBankResponse>, private val listener: OnEcosystemBankItemSlectionListener, private val picassoInstance: Picasso, private val accessToken: String, private val appName: String, private val custPspId: String?) : RecyclerView.Adapter<EcosystemBanksAdapter.ViewHolder>() {
    var mLastClickTime:Long = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ecosystem_bank_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bankList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bankItem = bankList[position]
        picassoInstance.load(holder.bankItem.bic?.let { getPspBaseUrlForBankLogo(it, accessToken, appName, custPspId) }).placeholder(R.drawable.ic_bank_place_holder).into(holder.bankLogo)
        holder.bankName.text = holder.bankItem.name
        holder.bankView.setOnClickListener {
//            if (SystemClock.elapsedRealtime() - mLastClickTime > 1000){
                listener.onBankListItemClick(holder.bankItem)
//            }
//            mLastClickTime = SystemClock.elapsedRealtime()

        }
    }

    inner class ViewHolder(val bankView: View) : RecyclerView.ViewHolder(bankView) {
        val bankLogo: ImageView = bankView.img_ec_bank_logo
        val bankName: TextView = bankView.txt_ec_bank_name
        lateinit var bankItem: EcosystemBankResponse
    }

}

