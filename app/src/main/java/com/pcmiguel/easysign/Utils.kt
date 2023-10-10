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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Utils {

    fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
        var intent = Intent(this, it)
        intent.putExtras(Bundle().apply(extras))
        startActivity(intent)
    }

    fun logout(context: Context) {

        App.instance.preferences.edit().clear().apply()

        val accessToken = Auth.getOAuth2Token()

        if (accessToken != null) {

            GlobalScope.launch(Dispatchers.IO) {

                val client = DbxClientV2(DbxRequestConfig.newBuilder("easysignapp").build(), accessToken)

                try {
                    // Revoke the access token
                    client.auth().tokenRevoke()

                    // Use um Handler para mostrar o Toast na thread principal (UI thread)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "expired login!", Toast.LENGTH_SHORT).show()
                        context.openActivity(LoadingActivity::class.java)
                    }

                } catch (e: DbxException) {
                    e.printStackTrace()
                }

            }

        }
        else {
            // Use um Handler para mostrar o Toast na thread principal (UI thread)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "expired login!", Toast.LENGTH_SHORT).show()
                context.openActivity(LoadingActivity::class.java)
            }
        }

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