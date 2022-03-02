package com.indepay.umps.pspsdk.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast


class PermissionCheck(internal var context: Context) {


    @TargetApi(Build.VERSION_CODES.M)
    fun checkStoragePermission(): Boolean {
        val define = Define()
        val permissionCheckRead = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionCheckWrite = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheckRead != PackageManager.PERMISSION_GRANTED || permissionCheckWrite != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        define.PERMISSION_STORAGE)
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        define.PERMISSION_STORAGE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false
        } else
            return true
    }


    @TargetApi(Build.VERSION_CODES.M)
    fun checkSmsPermission(): Boolean {
        val define = Define()
        val permissionCheckRead = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
        val permissionCallPhome = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
        return if (permissionCheckRead != PackageManager.PERMISSION_GRANTED || permissionCallPhome != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE),
                        define.MY_PERMISSIONS_REQUEST_SEND_SMS)
            } else {
                ActivityCompat.requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE),
                        define.MY_PERMISSIONS_REQUEST_SEND_SMS)
            }
            false
        } else
            true
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun checkGalleryPermission(): Boolean {
        val define = Define()
        val permissionCheckRead = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
        val permissionCheckWrite = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheckRead != PackageManager.PERMISSION_GRANTED || permissionCheckWrite != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity,
                            Manifest.permission.CAMERA)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        define.PERMISSION_STORAGE)
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(context as Activity,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        define.PERMISSION_STORAGE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false
        } else
            return true
    }

    fun showPermissionDialog() {
        Toast.makeText(context, "permission deny", Toast.LENGTH_SHORT).show()
    }
}
