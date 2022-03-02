package com.indepay.umps.pspsdk.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.billPayment.BillPaymentActivity
import com.indepay.umps.pspsdk.models.PartnerProductData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bill_payment_account_list_item.view.*

class BillPaymentPartnerProductPostpaidAdapter(
        private val billpaymentList: List<String>,
        private val picassoInstance: Picasso,
        private val listener: BillPaymentActivity)
    : RecyclerView.Adapter<BillPaymentPartnerProductPostpaidAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_payment_account_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return billpaymentList.size
    }

    override fun onBindViewHolder(holder: BillPaymentPartnerProductPostpaidAdapter.ViewHolder, position: Int) {
        holder.accountItem = billpaymentList[position]



        if(holder.accountItem.equals("BPJS")) {
            holder.logo.setImageResource(R.drawable.bpjs)
        }else if(holder.accountItem.equals("Pascabayar")){
            holder.logo.setImageResource(R.drawable.pascabayar)
        }
        else if(holder.accountItem.equals("Internet dan TV Kabel")){
            holder.logo.setImageResource(R.drawable.internet)
        }
        else if(holder.accountItem.equals("PDAM")){
            holder.logo.setImageResource(R.drawable.pdam)
        }
        else if(holder.accountItem.equals("Listrik")){
            holder.logo.setImageResource(R.drawable.pln_prepaid)
        }



//        if(billpaymentList.get(position).type.equals("prepaid")){
//            Log.d("Sudhir","prepaid"+holder.accountItem.name)
//        }else{
//            Log.d("Sudhir","postpaid"+holder.accountItem.name)
//
//        }
        //   picassoInstance.load(holder.accountItem.logo?.let { "getPspBaseUrlForBankLogo(it, accessToken, appName, custPspId)" }).placeholder(R.drawable.pulsa).into(holder.logo)
        holder.name.text = holder.accountItem

        holder.accountView.setOnClickListener {
            listener.onBillPaymentPartnerPostpaidListItemClick(holder.accountItem.toString())


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