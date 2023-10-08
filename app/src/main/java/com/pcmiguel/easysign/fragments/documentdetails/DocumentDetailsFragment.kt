package com.pcmiguel.easysign.fragments.documentdetails

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
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

        binding!!.optionsBtn.setOnClickListener {

            val popupMenu = PopupMenu(requireContext(), it)

            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { option ->
                when (option.itemId) {
                    R.id.option1 -> {

                        loadingDialog.startLoading()

                        App.instance.backOffice.cancelSignatureRequest(object : Listener<Any> {
                            override fun onResponse(response: Any?) {

                                loadingDialog.isDismiss()

                                if (isAdded) {

                                    if (response == null) {

                                        requireActivity().onBackPressed()

                                    }

                                }

                            }

                        }, item.signatureRequestId!!)


                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()

        }

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

        fileName.text = item.title ?: ""
        from.text = item.requesterEmailAddress ?: ""
        sent.text = Utils.formatDate(item.createdAt!!)

        if (item.requesterEmailAddress == App.instance.preferences.getString("Email", "")) {
            binding!!.optionsBtn.visibility = View.VISIBLE
        }

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

                            Log.d("base64Pdf", base64Pdf.toString())

                            val pdfData = Base64.decode(base64Pdf, Base64.DEFAULT)

                            // Create a temporary file to save the PDF
                            val tempFile = File.createTempFile("temp_pdf_", ".pdf", requireContext().cacheDir)

                            // Write the PDF data to the temporary file
                            val fos = FileOutputStream(tempFile)
                            fos.write(pdfData)
                            fos.close()

                            // Create a content URI using FileProvider for the temporary file
                            val tempPdfUri = FileProvider.getUriForFile(
                                requireContext(),
                                "com.pcmiguel.easysign.provider", // Replace with your FileProvider authority
                                tempFile
                            )

                            try {
                                // Open the temporary PDF file with a PDF viewer using an Intent
                                val pdfIntent = Intent(Intent.ACTION_VIEW)
                                pdfIntent.setDataAndType(tempPdfUri, "application/pdf")
                                pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read permission to the viewer app
                                startActivity(pdfIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Handle the exception as needed
                            }

                            // Delete the temporary file when you're done with it
                            //tempFile.delete()

                            /*// Create a file to save the PDF in your app's external storage directory
                            val pdfFile = File(requireContext().getExternalFilesDir(null), "temp_doc.pdf")

                            // Check if the file already exists
                            if (pdfFile.exists()) {
                                // Delete the existing file
                                pdfFile.delete()
                            }

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
                            }*/

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

                                            if (choices != null && choices.isNotEmpty()) {
                                                val text = choices[0].message!!.content.toString()
                                                resume!!.text = text
                                            }

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

                            val bundle = Bundle().apply {
                                putString("signUrl", response.embedded!!.signUrl!!)
                            }

                            findNavController().navigate(R.id.action_documentDetailsFragment_to_signDocumentFragment, bundle)

                        }
                        else if (response != null && response is String) {

                            val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_error_message, null)

                            val builder = AlertDialog.Builder(requireContext())
                                .setView(mDialogView)
                                .setCancelable(false)

                            val dialog = builder.create()
                            dialog.window?.setBackgroundDrawable(
                                ColorDrawable(
                                    Color.TRANSPARENT)
                            )

                            val errorMessage = mDialogView.findViewById<TextView>(R.id.message)
                            val okBtn = mDialogView.findViewById<View>(R.id.okBtn)

                            errorMessage.text = response

                            okBtn.setOnClickListener {
                                dialog.dismiss()
                            }

                            dialog.show()

                        }

                    }

                }

            }, signatureId)


        }

    }

}