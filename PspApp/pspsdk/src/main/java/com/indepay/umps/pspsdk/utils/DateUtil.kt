package com.indepay.umps.pspsdk.utils

import android.os.Build
import android.util.Log
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

open class DateUtil {

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun  getMonthsBetWeenDates(startdate: String, enddate: String):List<String>
//    {
//      var  monthsList: ArrayList<String>  = ArrayList<String>()
//
//        var date1 = LocalDate.parse(startdate)
//        val date2 = LocalDate.parse(enddate)
//        while (date1.isBefore(date2)) {
//            System.out.println(date1.month)
//            monthsList.add(date1.month.toString())
//           date1 = date1.plus(Period.ofMonths(1))
//        }
//
//
//        return monthsList
//
//
//    }

   fun  getMonthsBetWeenDates(startdate: String, enddate: String):List<String>
    {
        var  monthsList: ArrayList<String>  = ArrayList<String>()

        val date1 = startdate
        val date2 = enddate
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ENGLISH)
//
//        val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM/uuuu", Locale.ENGLISH)
//        val endMonth: YearMonth = YearMonth.parse(date2, dateFormatter)
//            var month: YearMonth = YearMonth.parse(date1, dateFormatter)
//            while (!month.isAfter(endMonth)) {
//
//                monthsList.add(month.format(monthFormatter));
//                System.out.println(month.format(monthFormatter))
//                month = month.plusMonths(1)
//            }
//        } else {
//

            val formater: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val outputFormater: DateFormat = SimpleDateFormat("MMMM")

            val beginCalendar = Calendar.getInstance()
            val finishCalendar = Calendar.getInstance()

            try {
                beginCalendar.time = formater.parse(date1)
                finishCalendar.time = formater.parse(date2)
                if (beginCalendar[Calendar.MONTH] !== finishCalendar[Calendar.MONTH]) {
                    beginCalendar[Calendar.DAY_OF_MONTH] = 1
                    finishCalendar[Calendar.DAY_OF_MONTH] = 2
                }
                do {
                    // add one month to date per loop
                    val month_year: String = outputFormater.format(beginCalendar.time)
                    Log.d("Date_Range", month_year)
                    monthsList.add(month_year.format(outputFormater));
                    System.out.println(month_year.format(outputFormater))

                    beginCalendar.add(Calendar.MONTH, 1)
                } while (beginCalendar.before(finishCalendar))
            } catch (e: ParseException) {
                e.printStackTrace()
            }
//        }

        return monthsList


    }
}

private fun LocalDate.toString(s: String) {
    System.out.println(s)

}
