package com.indepay.umps.pspsdk.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.models.BeneDetail
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.add_beneficiary_row.*
import java.util.ArrayList


class BeneficiaryAdapter(val context:Context) :RecyclerView.Adapter<BeneficiaryAdapter.ViewHolder>(){
    private val mlayoutInflater:LayoutInflater= LayoutInflater.from(context)
    private var beneficiaryList = ArrayList<BeneDetail>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficiaryAdapter.ViewHolder {
       val view=mlayoutInflater.inflate(R.layout.add_beneficiary_row,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return beneficiaryList.size
    }

    override fun onBindViewHolder(holder: BeneficiaryAdapter.ViewHolder, position: Int) {
        val modal=beneficiaryList[position]
        holder.bindView(modal)
    }

    fun setData(beneDetailsList: ArrayList<BeneDetail>) {
        this.beneficiaryList=beneDetailsList
    }


    inner class ViewHolder(override val containerView: View?):RecyclerView.ViewHolder(containerView!!), LayoutContainer {
        fun bindView(modal: BeneDetail) {
            tvName.text=modal.beneName
            tvMobileNo.text=modal.beneMobile
            tvAppName.text=modal.beneAppName

        }
    }
}