package com.indepay.umps.pspsdk.adapter

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import com.squareup.picasso.Picasso

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.billPayment.BillPaymentActivity
import com.indepay.umps.pspsdk.billPayment.BillerInfoActivity
import io.fabric.sdk.android.Fabric.with
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.bill_payment_account_list_item.view.*

class BillPaymentPartnerProductPrepaidAdapter(
        private val billpaymentList: List<String>,
        private val arrayofImages: Array<Int>,
        private val picassoInstance: Picasso,
        private val listener: BillPaymentActivity)
    : RecyclerView.Adapter<BillPaymentPartnerProductPrepaidAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_payment_account_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return billpaymentList.size
    }

    override fun onBindViewHolder(holder: BillPaymentPartnerProductPrepaidAdapter.ViewHolder, position: Int) {
        holder.accountItem = billpaymentList[position]


Log.d("Sudhir","LOGO::"+arrayofImages.size)
        if(holder.accountItem.equals("Pulsa/Paket Data")) {
            holder.logo.setImageResource(R.drawable.pulsa)
        }else if(holder.accountItem.equals("eMoney")){
            holder.logo.setImageResource(R.drawable.e_money)
        }
        else if(holder.accountItem.equals("Voucher Game")){
            holder.logo.setImageResource(R.drawable.g_game)
        }
        else if(holder.accountItem.equals("Listrik")){
            holder.logo.setImageResource(R.drawable.pln_prepaid)
        }
        else if(holder.accountItem.equals("Internet")){
            holder.logo.setImageResource(R.drawable.g_game)
        }
        else{
            holder.logo.setImageResource(R.drawable.pulsa)
        }


        holder.name.text = holder.accountItem

        holder.accountView.setOnClickListener {
            listener.onBillPaymentPartnerPrepaidListItemClick(holder.accountItem)


        }
//        holder.accountType.text = holder.accountItem.accountType
//        holder.bankBic.text = holder.accountItem.bic
//        holder.accountNo.text = holder.accountItem.accountNumber

    }


    inner class ViewHolder(val accountView: View) : RecyclerView.ViewHolder(accountView) {
        val logo: ImageView = accountView.img_pulsa

        //        val accountType: TextView = accountView.img_pulsa
//        val bankBic: TextView = accountView.txt_bank_acc_bic
        val name: TextView = accountView.txt_pulsa

        //        val accountNo: TextView = accountView.txt_bank_acc_no
//        val accMappingStatus: TextView = accountView.lbl_account_mapping_status
//        val mpinStatus: TextView = accountView.lbl_mpin_status
        lateinit var accountItem: String
    }
}