package com.indepay.umps.pspsdk.billPayment

import android.app.Fragment
import android.os.Bundle
import android.support.annotation.Keep
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.BillerInfoAdapter
import com.indepay.umps.pspsdk.adapter.MyAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnBillerListInteractionListner
import com.indepay.umps.pspsdk.models.PartnerProductData
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import com.indepay.umps.pspsdk.utils.getPicassoInstance
import com.indepay.umps.pspsdk.utils.getPspSslConfig
import kotlinx.android.synthetic.main.biller_layout.*
import okhttp3.OkHttpClient
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.singleTop

class BillerInfoActivity  : SdkBaseActivity(), OnBillerListInteractionListner {


    private var client = OkHttpClient()
    private lateinit var categeoryName:String
    private lateinit var billerData: ArrayList<PartnerProductData>
    private var billerArrayList: ArrayList<String> = ArrayList()

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    var bundle: Bundle? = null

    @Keep
    companion object : SdkCommonMembers() {
        @Keep
        const val BILLERDATA = "biller_data"
        @Keep
        const val CATEGEORYNAME = "categeory_name"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.biller_layout)
        categeoryName = intent.getStringExtra(BillerInfoActivity.CATEGEORYNAME).toString()
        billerData = (intent.getSerializableExtra(BillerInfoActivity.BILLERDATA) as ArrayList<PartnerProductData>?)!!

        lbl_biller_name.text = categeoryName

        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager>(R.id.viewPager)

        if(lbl_biller_name.text.equals("Pulsa/Paket Data")){
//        if(lbl_biller_name.text.equals("Pulsa")||lbl_biller_name.text.equals("Paket Data")) {
            tabLayout!!.visibility = View.VISIBLE
            viewPager!!.visibility = View.VISIBLE



            tabLayout!!.addTab(tabLayout!!.newTab().setText("Pulsa"))
            tabLayout!!.addTab(tabLayout!!.newTab().setText("Packet Data"))
            tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL



            val adapter = MyAdapter(this, supportFragmentManager,billerData, tabLayout!!.tabCount)
            viewPager!!.adapter = adapter

//            tabLayout!!.setupWithViewPager(viewPager);



            viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

            tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    viewPager!!.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {

                }

                override fun onTabReselected(tab: TabLayout.Tab) {

                }
            })
        }

        else {
            tabLayout!!.visibility = View.GONE
            viewPager!!.visibility = View.GONE


            val layoutManagerbiller = android.support.v7.widget.LinearLayoutManager(this)
            val itemDecorationbiller = android.support.v7.widget.DividerItemDecoration(this, layoutManagerbiller.orientation)

            biller_acc_list_container.layoutManager = layoutManagerbiller
            biller_acc_list_container.addItemDecoration(itemDecorationbiller)




            for (i in 0..billerData.size - 1) {

                Log.d("Sudhir", "billerData::" + billerData.get(i).biller)
                billerArrayList.add(billerData.get(i).biller.toString())
            }
            val biillerdistinct: List<String> = LinkedHashSet(billerArrayList).toMutableList()

            val picasso = getPicassoInstance(this@BillerInfoActivity, getPspSslConfig(this@BillerInfoActivity))
            val billInfoAdapter = BillerInfoAdapter(
                    billpaymentList = biillerdistinct,
                    picassoInstance = picasso,
                    listener = this@BillerInfoActivity)
            biller_acc_list_container.adapter = billInfoAdapter
        }
    }

    override fun onBillerListItemClick(billerItem: String) {
        Log.d("Sudhir","biller Item Clicked::"+billerItem)


        val partnerProductList: ArrayList<PartnerProductData> = ArrayList()

        for(i in 0..billerData.size-1) {
            if(billerItem.equals(billerData.get(i).biller)){

                partnerProductList.add(billerData.get(i))

            }

        }
        startActivity(intentFor<BillNumberFilled>(BillNumberFilled.BILLERNAME to billerItem,BillNumberFilled.BILLERDATA to partnerProductList).newTask().singleTop())
//        startActivity(intentFor<PhoneNumberAccountNumberFilling>(PhoneNumberAccountNumberFilling.BILLERNAME to billerItem,PhoneNumberAccountNumberFilling.BILLERDATA to partnerProductList).newTask().singleTop())

    }
}