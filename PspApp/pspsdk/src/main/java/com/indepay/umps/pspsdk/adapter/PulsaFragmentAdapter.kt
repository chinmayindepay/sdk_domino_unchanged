package com.indepay.umps.pspsdk.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.billPayment.BillPaymentActivity
import com.indepay.umps.pspsdk.billPayment.PulsaFragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bill_payment_account_list_item.view.*
import kotlinx.android.synthetic.main.biller_list_item.view.*

class PulsaFragmentAdapter (
        private val billpaymentList: List<String>,
        private val picassoInstance: Picasso,
        private val listener: PulsaFragment)
    : RecyclerView.Adapter<PulsaFragmentAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.biller_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return billpaymentList.size
    }

    override fun onBindViewHolder(holder: PulsaFragmentAdapter.ViewHolder, position: Int) {
        holder.accountItem = billpaymentList[position]


        //   picassoInstance.load(holder.accountItem.logo?.let { "getPspBaseUrlForBankLogo(it, accessToken, appName, custPspId)" }).placeholder(R.drawable.pulsa).into(holder.logo)

        holder.name.text = holder.accountItem

        holder.accountView.setOnClickListener {
            listener.onBillerListItemClick(holder.accountItem)


        }
//        holder.accountType.text = holder.accountItem.accountType
//        holder.bankBic.text = holder.accountItem.bic
//        holder.accountNo.text = holder.accountItem.accountNumber

    }


    inner class ViewHolder(val accountView: View) : RecyclerView.ViewHolder(accountView) {
        val logo: ImageView = accountView.img_ec_biller_logo

        //        val accountType: TextView = accountView.img_pulsa
//        val bankBic: TextView = accountView.txt_bank_acc_bic
        val name: TextView = accountView.txt_ec_biller_name

        //        val accountNo: TextView = accountView.txt_bank_acc_no
//        val accMappingStatus: TextView = accountView.lbl_account_mapping_status
//        val mpinStatus: TextView = accountView.lbl_mpin_status
        lateinit var accountItem: String
    }
}