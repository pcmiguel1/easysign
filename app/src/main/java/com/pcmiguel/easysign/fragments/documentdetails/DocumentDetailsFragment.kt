package com.pcmiguel.easysign.fragments.documentdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentDocumentDetailsBinding
import com.pcmiguel.easysign.fragments.documentdetails.adapter.SignatureAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiAIInterface
import com.pcmiguel.easysign.services.ApiInterface
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DocumentDetailsFragment : Fragment() {

    private var binding: FragmentDocumentDetailsBinding? = null

    private lateinit var item : ApiInterface.SignatureRequest
    private lateinit var recyclerView : RecyclerView
    private lateinit var signatureAdapter : SignatureAdapter

    private lateinit var loadingDialog: LoadingDialog

    var signatureId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentDocumentDetailsBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        if (arguments != null && requireArguments().containsKey("item")) {

            item = (arguments?.getParcelable("item") as? ApiInterface.SignatureRequest)!!

        }

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())
        Utils.navigationBar(view, "", requireActivity())

        recyclerView = binding!!.recipients
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        signatureAdapter = SignatureAdapter(item.signatures!!)
        recyclerView.adapter = signatureAdapter

        val bottomMenuSign = binding!!.bottomMenuSign
        val viewBtn = binding!!.viewBtn
        val summarizeBtn = binding!!.resumeBtn
        val signBtn = binding!!.signBtn
        val fileName = binding!!.fileName
        val from = binding!!.from
        val sent = binding!!.sent

        from.text = item.requesterEmailAddress ?: ""
        sent.text = Utils.formatDate(item.createdAt!!)

        // bottom menu sign só aparece se user precisar de assinar o documento
        val signatures = item.signatures
        bottomMenuSign.visibility = View.GONE
        if (signatures != null && signatures.isNotEmpty()) {
            for (signature in signatures) {
                if (signature.signerEmailAddress == App.instance.preferences.getString("Email", "") && signature.statusCode == "awaiting_signature") {
                    signatureId = signature.signatureId ?: ""
                    bottomMenuSign.visibility = View.VISIBLE
                    break
                }
            }
        }
        else bottomMenuSign.visibility = View.GONE

        viewBtn.setOnClickListener {

            loadingDialog.startLoading()

            App.instance.backOffice.downloadFilesDataUri(object : Listener<Any> {
                override fun onResponse(response: Any?) {

                    loadingDialog.isDismiss()

                    if (isAdded) {

                        if (response != null && response is JsonObject) {

                            val dataUri = response.get("data_uri").asString

                            val base64Pdf = dataUri.substringAfter(",")

                            val pdfData = Base64.decode(base64Pdf, Base64.DEFAULT)

                            // Create a file to save the PDF in your app's external storage directory
                            val pdfFile = File(requireContext().getExternalFilesDir(null), "sample.pdf")

                            try {
                                // Save the byte array as a PDF file
                                val fos = FileOutputStream(pdfFile)
                                fos.write(pdfData)
                                fos.close()

                                // Create a content URI using FileProvider
                                val pdfUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "com.pcmiguel.easysign.provider", // Replace with your FileProvider authority
                                    pdfFile
                                )

                                // Open the PDF file with a PDF viewer
                                val pdfIntent = Intent(Intent.ACTION_VIEW)
                                pdfIntent.setDataAndType(pdfUri, "application/pdf")
                                pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission to the viewer app
                                startActivity(pdfIntent)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }

                }

            }, item.signatureRequestId!!)

        }

        summarizeBtn.setOnClickListener {


            loadingDialog.startLoading()

            App.instance.backOffice.downloadFilesDataUri(object : Listener<Any> {
                override fun onResponse(response: Any?) {

                    if (isAdded) {

                        if (response != null && response is JsonObject) {

                            val dataUri = response.get("data_uri").asString

                            val base64Pdf = dataUri.substringAfter(",")

                            val pdfData = Base64.decode(base64Pdf, Base64.DEFAULT)

                            // Create a file to save the PDF in your app's external storage directory
                            val pdfFile = File(requireContext().getExternalFilesDir(null), "sample.pdf")

                            try {
                                // Save the byte array as a PDF file
                                val fos = FileOutputStream(pdfFile)
                                fos.write(pdfData)
                                fos.close()

                                // Create a content URI using FileProvider
                                val pdfUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "com.pcmiguel.easysign.provider", // Replace with your FileProvider authority
                                    pdfFile
                                )

                                val pdfText = Utils.extractTextFromPdf(requireContext().contentResolver, pdfUri)

                                val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_summarize_ai, null)
                                val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)

                                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

                                dialog.setContentView(view)
                                dialog.setCancelable(true)

                                val resume = dialog.findViewById<TextView>(R.id.resume)

                                val json = JsonObject()
                                json.addProperty("model", "gpt-3.5-turbo")

                                val messages = JsonArray()
                                val msg = JsonObject()
                                msg.addProperty("role", "user")
                                msg.addProperty("content", "summarize this text: $pdfText")
                                messages.add(msg)

                                json.add("messages", messages)

                                App.instance.backOffice.chatAI(object : Listener<Any> {
                                    override fun onResponse(response: Any?) {

                                        loadingDialog.isDismiss()

                                        if (response != null && response is ApiAIInterface.ChatAI) {

                                            val choices = response.choices
                                            val text = choices!![0].message!!.content.toString()

                                            resume!!.text = text

                                            dialog.show()

                                        }

                                    }

                                }, json)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }

                }

            }, item.signatureRequestId!!)

        }

        signBtn.setOnClickListener {

            loadingDialog.startLoading()

            App.instance.backOffice.getEmbeddedSignURL(object : Listener<Any> {
                override fun onResponse(response: Any?) {

                    loadingDialog.isDismiss()

                    if (isAdded) {

                        if (response != null && response is ApiInterface.EmbeddedResponse) {

                            Log.d("urlembedded", response.embedded!!.signUrl!!)

                        }
                        else  {
                            Log.d("urlembedded", response.toString() + " " + signatureId)
                        }

                    }

                }

            }, signatureId)


        }

    }

}