package com.indepay.umps.pspsdk.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.billPayment.BillNumberFilled
import com.indepay.umps.pspsdk.billPayment.BillerInfoActivity
import com.indepay.umps.pspsdk.models.PartnerProductData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.biller_list_item.view.*
import kotlinx.android.synthetic.main.number_filled_item.view.*

class NumberFilledAdapter (
        private val billpaymentList: ArrayList<PartnerProductData>,
        private val picassoInstance: Picasso,
        private val listener: BillNumberFilled)
    : RecyclerView.Adapter<NumberFilledAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.number_filled_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return billpaymentList.size
    }

    override fun onBindViewHolder(holder: NumberFilledAdapter.ViewHolder, position: Int) {
        holder.accountItem = billpaymentList[position]


        //   picassoInstance.load(holder.accountItem.logo?.let { "getPspBaseUrlForBankLogo(it, accessToken, appName, custPspId)" }).placeholder(R.drawable.pulsa).into(holder.logo)
        holder.amountmain.text = holder.accountItem.amount
        holder.name.text = holder.accountItem.name
        holder.amount.text = holder.accountItem.amount
        holder.accountView.setOnClickListener {
            listener.onBilleramountListItemClick(holder.accountItem)
        }
//        holder.accountType.text = holder.accountItem.accountType
//        holder.bankBic.text = holder.accountItem.bic
//        holder.accountNo.text = holder.accountItem.accountNumber

    }


    inner class ViewHolder(val accountView: View) : RecyclerView.ViewHolder(accountView) {
//        val logo: ImageView = accountView.img_ec_biller_logo

        //        val accountType: TextView = accountView.img_pulsa
//        val bankBic: TextView = accountView.txt_bank_acc_bic
        val name: TextView = accountView.txt_name
        val amount: TextView = accountView.txt_amount
        val amountmain: TextView = accountView.txt_txn_time
        //        val accountNo: TextView = accountView.txt_bank_acc_no
//        val accMappingStatus: TextView = accountView.lbl_account_mapping_status
//        val mpinStatus: TextView = accountView.lbl_mpin_status
        lateinit var accountItem: PartnerProductData
    }
}