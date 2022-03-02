/*
 * ******************************************************************************
 *  * Copyright, INDEPAY 2019 All rights reserved.
 *  *
 *  * The copyright in this work is vested in INDEPAY and the
 *  * information contained herein is confidential.  This
 *  * work (either in whole or in part) must not be modified,
 *  * reproduced, disclosed or disseminated to others or used
 *  * for purposes other than that for which it is supplied,
 *  * without the prior written permission of INDEPAY.  If this
 *  * work or any part hereof is furnished to a third party by
 *  * virtue of a contract with that party, use of this work by
 *  * such party shall be governed by the express contractual
 *  * terms between the INDEPAY which is a party to that contract
 *  * and the said party.
 *  *
 *  * Revision History
 *  * Date           Who        Description
 *  * 06-09-2019     Mayank D   Added file header
 *  *
 *  *****************************************************************************
 */

package com.indepay.umps.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import com.indepay.umps.R
import com.indepay.umps.utils.getStringData
import com.indepay.umps.utils.saveStringData
import kotlinx.android.synthetic.main.activity_register.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class RegistrationActivity : BaseActivity() {

    private var isUnameError = true
    private var isMobileNoError = true

    companion object {
        const val USER_NAME = "user_name"
        const val MOBILE_NUMBER = "user_mobile"
        const val _LOCALE = "locale"
    }

    /**
     * This method is a part of Base app and gets call in initial stage,
     *               It asks to enter user name and mobile number.
     *
     *
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        addMobileToAutoComplete(populateAutoCompleteMobileNo())
        addUserNameToAutoComplete(populateAutocompleteUserName())

        uname.addTextChangedListener(
                object : TextWatcher {

                    override fun afterTextChanged(s: Editable?) {
                        if (s != null) {
                            if (s.length > edt_user_name.counterMaxLength) {
                                isUnameError = true
                                edt_user_name.error = "Max character length is " + edt_user_name.counterMaxLength
                            } else if (s.isNullOrEmpty()) {
                                isUnameError = true
                                edt_user_name.error = "User name can not be empty"
                            } else {
                                isUnameError = false
                                edt_user_name.error = null
                            }
                        }

                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                }
        )
        mobile.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if (s != null) {
                            if (s.length > edt_mobile_no.counterMaxLength) {
                                isMobileNoError = true
                                edt_mobile_no.error = "Max character length is " + edt_mobile_no.counterMaxLength
                            } else if (s.isEmpty()) {
                                isMobileNoError = true
                                edt_mobile_no.error = "Mobile number can not be empty"
                            } else {
                                isMobileNoError = false
                                edt_mobile_no.error = null
                            }
                        }
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                }
        )
        mobile.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        btn_login.setOnClickListener {
            attemptLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!getStringData(this, USER_NAME).isBlank() && !getStringData(this, MOBILE_NUMBER).isBlank()) {
            startActivity<HomeActivity>()
            finish()
        }
    }

    private fun attemptLogin() {
        if (isUnameError || isMobileNoError) {
            toast("Please enter a valid mobile number and user name").show()
        } else {
            saveStringData(this, USER_NAME, uname.text.toString())
            saveStringData(this, MOBILE_NUMBER, mobile.text.toString())
            startActivity<HomeActivity>()
            finish()
        }
    }

    private fun populateAutoCompleteMobileNo(): ArrayList<String> {
        val listOfMobileNo = ArrayList<String>()
        listOfMobileNo.add("9836761919")//Rudra
        listOfMobileNo.add("4444444444")//QA_1
        listOfMobileNo.add("5555555555")//QA_2
        listOfMobileNo.add("7980096009")//GARGI_1
        listOfMobileNo.add("9830878854")//GARGI_2
        listOfMobileNo.add("6593587762")//GARGI_3
        listOfMobileNo.add("9051114133")//SURANJANA
        listOfMobileNo.add("8420369621")//ABHISHEK
        listOfMobileNo.add("9231175966")//SUPARNA
        listOfMobileNo.add("7890635361")//PRASENJIT
        listOfMobileNo.add("9874543225")//ARINDAM
        listOfMobileNo.add("8700270570")//SURBHI1
        listOfMobileNo.add("7696369663")//SURBHI2
        listOfMobileNo.add("9910910050")//MAYANK
        listOfMobileNo.add("9830105024")//Suranjana
        listOfMobileNo.add("9830105025")//Suranjana
        listOfMobileNo.add("8106170677")//Sudhir

        return listOfMobileNo
    }

    private fun populateAutocompleteUserName(): ArrayList<String> {
        val listOfUserName = ArrayList<String>()
        listOfUserName.add("Rudra")
        listOfUserName.add("Suranjana")
        listOfUserName.add("Gargi")
        listOfUserName.add("QA_1")
        listOfUserName.add("QA_2")
        listOfUserName.add("Abhishek")
        listOfUserName.add("Suparna")
        listOfUserName.add("Prasenjit")
        listOfUserName.add("Arindam")
        listOfUserName.add("Surbhi")
        listOfUserName.add("Mayank")
        listOfUserName.add("Sudhir")

        return listOfUserName
    }

    private fun addMobileToAutoComplete(mobileNumberCollection: List<String>) {
        val adapter = ArrayAdapter(this@RegistrationActivity,
                android.R.layout.simple_dropdown_item_1line, mobileNumberCollection)
        mobile.setAdapter(adapter)
    }

    private fun addUserNameToAutoComplete(userNameCollection: List<String>) {
        val adapter = ArrayAdapter(this@RegistrationActivity,
                android.R.layout.simple_dropdown_item_1line, userNameCollection)
        uname.setAdapter(adapter)
    }
}
