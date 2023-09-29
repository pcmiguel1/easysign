package com.pcmiguel.easysign

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.auth0.jwt.JWT
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.pcmiguel.easysign.Utils.openActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*
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

    fun logout(context: Context) {

        App.instance.preferences.edit().clear().apply()

        val accessToken = Auth.getOAuth2Token()

        Toast.makeText(context, "expired login!", Toast.LENGTH_SHORT).show()

        if (accessToken != null) {

            GlobalScope.launch(Dispatchers.IO) {

                val client = DbxClientV2(DbxRequestConfig.newBuilder("easysignapp").build(), accessToken)

                try {
                    // Revoke the access token
                    client.auth().tokenRevoke()

                    context.openActivity(LoadingActivity::class.java)

                } catch (e: DbxException) {
                    e.printStackTrace()
                }

            }

        }
        else {
            context.openActivity(LoadingActivity::class.java)
        }

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

    fun isPdfFile(uri: Uri, context: Context): Boolean {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return mimeType == "application/pdf"
    }

    fun extractTextFromPdf(contentResolver: ContentResolver, pdfUri: Uri): String {
        val pdfInputStream = contentResolver.openInputStream(pdfUri)
        val pdfDocument = PdfDocument(PdfReader(pdfInputStream))
        val pageCount = pdfDocument.numberOfPages
        val text = StringBuilder()

        for (pageNum in 1..pageCount) {
            val page = pdfDocument.getPage(pageNum)
            val textExtractor = PdfTextExtractor.getTextFromPage(page)
            text.append(textExtractor)
        }

        pdfDocument.close()
        pdfInputStream?.close()

        return text.toString()
    }

    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        var uri: Uri? = null

        try {
            // Create a temporary file to store the bitmap
            val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.png")

            // Create an output stream to write the bitmap to the file
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Convert the file to a Uri
            uri = Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return uri
    }

    fun openPdf(pdfFile: File, context: Context) {
        val pdfUri = FileProvider.getUriForFile(context, "com.pcmiguel.easysign.provider", pdfFile)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    fun getByteFromDrawable(context: Context, resDrawable: Int) : ByteArray {
        val bitmap = BitmapFactory.decodeResource(context.resources, resDrawable)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) {
            return "0 B"
        }

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(sizeInBytes.toDouble()) / Math.log10(1024.0)).toInt()

        val formattedSize = String.format("%.2f", sizeInBytes / Math.pow(1024.0, digitGroups.toDouble()))
        return "$formattedSize ${units[digitGroups]}"
    }

    fun uriToPdfFile(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

        if (inputStream != null) {
            try {
                val pdfFile = createTempPdfFile(context)
                val outputStream = FileOutputStream(pdfFile)
                val buffer = ByteArray(4 * 1024) // 4k buffer

                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
                return pdfFile
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                inputStream.close()
            }
        }
        return null
    }

    private fun createTempPdfFile(context: Context): File {
        val timestamp = System.currentTimeMillis()
        val storageDir = context.getExternalFilesDir(null)
        return File(storageDir, "temp_$timestamp.pdf")
    }

    fun formatDate(longDate: Long): String {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val date = Date(longDate * 1000)
        return dateFormat.format(date)
    }

}