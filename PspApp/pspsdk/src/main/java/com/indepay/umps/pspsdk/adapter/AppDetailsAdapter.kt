package com.indepay.umps.pspsdk.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

import com.indepay.umps.pspsdk.models.AppDetail
import kotlinx.android.synthetic.main.spinner_custom_layout.view.*
import android.os.Build
import android.support.annotation.RequiresApi

import kotlin.collections.ArrayList

import android.graphics.BitmapFactory
import android.support.v4.content.ContextCompat
import android.util.Base64
import com.indepay.umps.pspsdk.R


class AppDetailsAdapter(private val context:Context): BaseAdapter(){
    private val mlayoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var appDetailsList = java.util.ArrayList<AppDetail>()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(position: Int, p1: View?, parent: ViewGroup?): View {
      val  view = mlayoutInflater.inflate(R.layout.spinner_custom_layout,parent,false)
        view.tvAppName.text=appDetailsList[position].name
        if(!TextUtils.isEmpty(appDetailsList[position].logo)){
            try {
                val imageByteArray = Base64.decode(appDetailsList[position].logo,Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                view.ivAppIcon.setImageBitmap(decodedByte)
            } catch (e: Exception) {
            }

        }else{
            view.ivAppIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_bank_place_holder))
        }
        return view
    }

    override fun getItem(p0: Int): Any? {
        return null
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return appDetailsList.size
    }

    fun setData(appList: ArrayList<AppDetail>) {
        this.appDetailsList=appList

    }

  /*  val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Base64.getDecoder().decode(str)
    } else {
        Base64.decode(str, Base64.DEFAULT) // Unresolved reference: decode
    }*/
}