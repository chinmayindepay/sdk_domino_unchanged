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

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.models.EcosystemBankResponse

class FetchNetworkBankAdapter(context: Context,
                              dataSource: List<EcosystemBankResponse>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var list: ArrayList<EcosystemBankResponse> = dataSource as ArrayList<EcosystemBankResponse>

    //1
    override fun getCount(): Int {
        return list.size
    }

    //2
    override fun getItem(position: Int): Any {
        return list[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        // Get contactView for row item
        val rowView = inflater.inflate(R.layout.fetch_account_adapter, parent, false)
        val txtEcoBankName = rowView.findViewById<TextView>(R.id.txt_eco_bank_name) as TextView
        val txtEcoBankBic = rowView.findViewById<TextView>(R.id.txt_eco_bank_bic) as TextView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            txtEcoBankBic.text = list.get(position).bic
            txtEcoBankName.text = list.get(position).name
        }

        return rowView
    }

}
