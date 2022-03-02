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
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.models.BeneficiaryDetailsView


class BeneficiaryRetrievalAdapter(context: Context,
                                  dataSource: List<BeneficiaryDetailsView>) : BaseAdapter(),Filterable, Parcelable {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var beneFilter: BeneFilter? = null
    private var beneList: ArrayList<BeneficiaryDetailsView>? = dataSource as ArrayList<BeneficiaryDetailsView>
    private var filteredList: ArrayList<BeneficiaryDetailsView>? = dataSource as ArrayList<BeneficiaryDetailsView>

    /*private var list :ArrayList<BeneficiaryDetailsView> = dataSource as ArrayList<BeneficiaryDetailsView>*/

    constructor(parcel: Parcel) : this(
            TODO("context"),
            TODO("dataSource"))

    //1
    override fun getCount(): Int {
        return filteredList?.size!!
    }

    //2
    override fun getItem(position: Int): Any {
        return filteredList!![position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        if (beneFilter == null) {
            beneFilter = BeneFilter()
        }

        return beneFilter as BeneFilter
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get contactView for row item
        val rowView = inflater.inflate(R.layout.bene_retreive_adapter, parent, false)
        val beneName = rowView.findViewById(R.id.txtBeneName) as TextView
        val txtBeneAccountNo = rowView.findViewById(R.id.txtBeneAccountNo) as TextView
        val beneID = rowView.findViewById(R.id.txtBeneID) as TextView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            beneName.text = filteredList?.get(position)?.beneName
            beneID.text = filteredList?.get(position)?.benePaymentAddr
            txtBeneAccountNo.text = filteredList?.get(position)?.beneAccountNo
        }
        return rowView
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BeneficiaryRetrievalAdapter> {
        override fun createFromParcel(parcel: Parcel): BeneficiaryRetrievalAdapter {
            return BeneficiaryRetrievalAdapter(parcel)
        }

        override fun newArray(size: Int): Array<BeneficiaryRetrievalAdapter?> {
            return arrayOfNulls(size)
        }
    }

    private inner class BeneFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (constraint != null && constraint.length > 0) {
                val tempList = ArrayList<BeneficiaryDetailsView>()

                // search content in friend list
                if (beneList != null) {
                    for (user in beneList!!) {
                        if (user.beneName?.toLowerCase()?.contains(constraint.toString().toLowerCase())!!) {
                            tempList.add(user)
                        }
                    }
                }

                filterResults.count = tempList.size
                filterResults.values = tempList
            } else {
                filterResults.count = beneList?.size!!
                filterResults.values = beneList
            }

            return filterResults
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            filteredList = results.values as? ArrayList<BeneficiaryDetailsView>
            notifyDataSetChanged()
        }
    }

}
