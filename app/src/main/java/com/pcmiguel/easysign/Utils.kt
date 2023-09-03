package com.pcmiguel.easysign

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.auth0.jwt.JWT
import java.util.ArrayList
import java.util.regex.Pattern

object Utils {

    fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
        var intent = Intent(this, it)
        intent.putExtras(Bundle().apply(extras))
        startActivity(intent)
    }

    fun validCode(code: String): Boolean {
        val codePattern = Pattern.compile("^[0-9]{4}$")
        return code.matches(codePattern.toRegex())
    }

    fun isKeyCodeNumber(keycode: Int): Boolean {
        return when (keycode) {
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2,
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8,
            KeyEvent.KEYCODE_9 -> true
            else -> false
        }
    }

    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@#\$%^&*()_+|`–=\\\\{}\\[\\]:\\\";'<>?,./]).{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //Android 10+
            cm.getNetworkCapabilities(cm.activeNetwork)?.let { networkCapabilities ->
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
        }
        else {
            return cm.activeNetworkInfo?.isConnectedOrConnecting == true
        }
        return false

    }

    fun validEmail(email: String): Boolean {
        val VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)

        val matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email)
        return matcher.find()
    }

    fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getExpirationDateToken(token: String): Long {
        val decodedToken = JWT.decode(token)
        return decodedToken.expiresAt.time
    }


    fun checkPhotoPermissions(fragment: Fragment, code: Int): Boolean {
        val cameraPermission = Manifest.permission.CAMERA
        val writeExternalPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val readExternalPermission = Manifest.permission.READ_EXTERNAL_STORAGE

        val permissionsToRequest = ArrayList<String>()

        if (ActivityCompat.checkSelfPermission(fragment.requireContext(), cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(cameraPermission)
        }

        if (ActivityCompat.checkSelfPermission(fragment.requireContext(), writeExternalPermission) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsToRequest.add(writeExternalPermission)
        }

        if (ActivityCompat.checkSelfPermission(fragment.requireContext(), readExternalPermission) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsToRequest.add(readExternalPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(fragment.requireActivity(), permissionsToRequest.toTypedArray(), code)
            return false
        }

        return true
    }

    fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri) : Bitmap {

        var ei : ExifInterface
        val input = context.contentResolver.openInputStream(selectedImage)
        if (Build.VERSION.SDK_INT > 23) ei = ExifInterface(input!!)
        else ei = ExifInterface(selectedImage.path!!)

        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        when (orientation) {
            0 -> return rotateBitmap(img, 0)
            ExifInterface.ORIENTATION_ROTATE_90 -> return rotateBitmap(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> return rotateBitmap(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> return rotateBitmap(img, 270)
            else -> return img

        }

    }

    fun rotateBitmap(bMap: Bitmap, orientation: Int): Bitmap {
        Log.d("Time", "Rotate init" + System.currentTimeMillis())

        val bMapRotate: Bitmap
        bMapRotate = if (orientation != 0) {
            val matrix = Matrix()
            matrix.postRotate(orientation.toFloat())
            Bitmap.createBitmap(bMap, 0, 0, bMap.width,
                bMap.height, matrix, true)
        } else
            Bitmap.createScaledBitmap(bMap, bMap.width,
                bMap.height, true)

        return bMapRotate
    }

    fun navigationBar(v: View, theme: String, activity: Activity) {

        val title = v.findViewById<View>(R.id.toolbar).findViewById<TextView>(R.id.title)
        title.text = theme

        val backBt = v.findViewById<View>(R.id.back_bt)
        backBt.setOnClickListener { activity.onBackPressed() }

    }

}