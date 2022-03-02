package com.indepay.umps.pspsdk.billPayment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.PacketDataFragmentAdapter
import com.indepay.umps.pspsdk.adapter.PulsaFragmentAdapter
import com.indepay.umps.pspsdk.callbacks.OnBillerListInteractionListner
import com.indepay.umps.pspsdk.models.PartnerProductData
import com.indepay.umps.pspsdk.utils.getPicassoInstance
import com.indepay.umps.pspsdk.utils.getPspSslConfig
import kotlinx.android.synthetic.main.fragment_bill_packet_data.*
import kotlinx.android.synthetic.main.fragment_bill_pulsa.*

class PacketDataFragment: Fragment(), OnBillerListInteractionListner {

    private lateinit var billerData: ArrayList<PartnerProductData>
    private var billerArrayList: ArrayList<String> = ArrayList()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        Log.d("Sudhir","pulsa Fragment::")
        val args = getArguments()
        billerData = args!!.getSerializable("billerData") as ArrayList<PartnerProductData>


        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_bill_packet_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey("billerData") }?.apply {
//            val textView: TextView = view.findViewById(R.id.text)
//            textView.text = billerData.get(0).biller


            val layoutManagerbiller = android.support.v7.widget.LinearLayoutManager(context)
            val itemDecorationbiller = android.support.v7.widget.DividerItemDecoration(context, layoutManagerbiller.orientation)

            biller_packet_acc_list_container.layoutManager = layoutManagerbiller
            biller_packet_acc_list_container.addItemDecoration(itemDecorationbiller)


            for (i in 0..billerData.size-1) {
                if(billerData.get(i).category.equals("Paket Data")) {
                    Log.d("Sudhir", "billerData Caetgeory::" + billerData.get(i).category)
                    Log.d("Sudhir", "billerData::" + billerData.get(i).biller)
                    billerArrayList.add(billerData.get(i).biller.toString())
                }
            }
            val biillerdistinct: List<String> = LinkedHashSet(billerArrayList).toMutableList()

            val picasso = getPicassoInstance(view.context, getPspSslConfig(view.context))
            val billInfoAdapter = PacketDataFragmentAdapter(
                    billpaymentList = biillerdistinct,
                    picassoInstance = picasso,
                    listener = this@PacketDataFragment)
            biller_packet_acc_list_container.adapter = billInfoAdapter

        }
    }

    override fun onBillerListItemClick(billerItem: String) {
        Log.d("Sudhir","biller Item Clicked::"+billerItem)


        val partnerProductList: ArrayList<PartnerProductData> = ArrayList()

        for(i in 0..billerData.size-1) {
            if(billerData.get(i).category.equals("Paket Data")) {
                if (billerItem.equals(billerData.get(i).biller)) {

                    partnerProductList.add(billerData.get(i))

                }
            }

        }
        val intent = Intent(context, BillNumberFilled::class.java)
// To pass any data to next activity
        intent.putExtra(BillNumberFilled.BILLERNAME, billerItem)
        intent.putExtra(BillNumberFilled.BILLERDATA, partnerProductList)
// start your next activity
        startActivity(intent)
//        startActivity(intentFor<BillNumberFilled>(BillNumberFilled.BILLERNAME to billekeyIdentifierrItem,BillNumberFilled.BILLERDATA to partnerProductList).newTask().singleTop())

    }
}