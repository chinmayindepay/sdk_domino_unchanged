package com.indepay.umps.pspsdk.adapter


import android.R.attr.fragment
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.indepay.umps.pspsdk.billPayment.PacketDataFragment
import com.indepay.umps.pspsdk.billPayment.PulsaFragment
import com.indepay.umps.pspsdk.models.PartnerProductData


class MyAdapter(private val myContext: Context, fm: FragmentManager, private val billerData: ArrayList<PartnerProductData>, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    private lateinit var billerpulsaDat: ArrayList<PartnerProductData>

   //
//    val newbundle = Bundle()
//    newbundle.putSerializable("billerData", billerData)

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment? {
//        val bundle = Bundle()

//        billerData = bundle.getSerializable("billerData") as ArrayList<PartnerProductData>

        when (position) {
            0 -> {
                  val pulsaFragment: PulsaFragment = PulsaFragment()
//                val mFragment = PulsaFragment()
                val mArgs = Bundle()
                mArgs.putSerializable("billerData", billerData)
                pulsaFragment.setArguments(mArgs)

                return pulsaFragment
            }
            1 -> {
                val PacketDataFragment: PacketDataFragment = PacketDataFragment()
                val mArgs = Bundle()
                mArgs.putSerializable("billerData", billerData)
                PacketDataFragment.setArguments(mArgs)

                return PacketDataFragment
            }

            else -> return null
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}