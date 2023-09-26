package com.pcmiguel.easysign.fragments.templates

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
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentTemplatesBinding
import com.pcmiguel.easysign.fragments.templates.adapter.TemplatesAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiInterface
import java.io.File
import java.io.FileOutputStream


class TemplatesFragment : Fragment() {

    private var binding: FragmentTemplatesBinding? = null

    private lateinit var recyclerViewTemplates: RecyclerView
    private var templates: MutableList<ApiInterface.Templates> = mutableListOf()
    private lateinit var templatesAdapter: TemplatesAdapter

    private lateinit var loadingDialog: LoadingDialog

    private var selectTemplate = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentTemplatesBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        if (arguments != null && requireArguments().containsKey("selectTemplate")) {

            selectTemplate = arguments?.getBoolean("selectTemplate") ?: false

        }

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        Utils.navigationBar(view, "Templates", requireActivity())

        if (selectTemplate) {

            binding!!.addTemplate.visibility = View.GONE
            binding!!.selectTemplate.visibility = View.VISIBLE
            binding!!.nextBtn.visibility = View.VISIBLE

            binding!!.nextBtn.setOnClickListener {

                val itemSelected = templatesAdapter.getSelectedItem()

                if (itemSelected != null) {

                    val bundle = Bundle().apply {
                        putParcelable("itemSelected", itemSelected)
                    }

                    findNavController().navigate(R.id.action_templatesFragment_to_addSignerRoleFragment, bundle)


                } else {
                    Toast.makeText(requireContext(), "Please select one template.", Toast.LENGTH_SHORT).show()
                }

            }


        }

        recyclerViewTemplates = binding!!.templatesList
        recyclerViewTemplates.setHasFixedSize(true)
        recyclerViewTemplates.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        templatesAdapter = TemplatesAdapter(templates, selectTemplate)
        recyclerViewTemplates.adapter = templatesAdapter

        templatesAdapter.onPreviewClickListener(object : TemplatesAdapter.onPreviewClickListener {
            override fun onItemClick(position: Int) {

                val item = templatesAdapter.getItem(position)

                loadingDialog.startLoading()

                App.instance.backOffice.getTemplateFilesDataUri(object : Listener<Any> {
                    override fun onResponse(response: Any?) {

                        loadingDialog.isDismiss()

                        if (isAdded) {

                            if (response != null && response is JsonObject) {

                                val dataUri = response.get("data_uri").asString

                                val base64Pdf = dataUri.substringAfter(",")

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

                }, item.templateId!!)


            }
        })


        templatesAdapter.onEditClickListener(object : TemplatesAdapter.onEditClickListener {
            override fun onItemClick(position: Int) {

                val item = templatesAdapter.getItem(position)

                loadingDialog.startLoading()

                val json = JsonObject()
                json.addProperty("test_mode", true)

                App.instance.backOffice.getEmbeddedTemplateEditUrl(object : Listener<Any> {
                    override fun onResponse(response: Any?) {

                        loadingDialog.isDismiss()

                        if (isAdded) {

                            if (response != null && response is ApiInterface.EmbeddedResponse) {

                                val bundle = Bundle().apply {
                                    putString("editUrl", response.embedded!!.editUrl)
                                }

                                findNavController().navigate(R.id.action_templatesFragment_to_editDocumentFragment, bundle)

                            }
                            else {

                            }

                        }

                    }

                }, json, item.templateId!!)

            }
        })

        templatesAdapter.onDeleteClickListener(object : TemplatesAdapter.onDeleteClickListener {
            override fun onItemClick(position: Int) {

                val item = templatesAdapter.getItem(position)

                val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_confirm, null)

                val builder = AlertDialog.Builder(requireContext())
                    .setView(mDialogView)
                    .setCancelable(false)

                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(
                    ColorDrawable(
                        Color.TRANSPARENT)
                )

                val message = mDialogView.findViewById<TextView>(R.id.message)
                val cancelBtn = mDialogView.findViewById<View>(R.id.cancelBtn)
                val deleteBtn = mDialogView.findViewById<View>(R.id.deleteBtn)

                message.text = "Do you want to delete this template?"

                cancelBtn.setOnClickListener {
                    dialog.dismiss()
                }

                deleteBtn.setOnClickListener {
                    dialog.dismiss()

                    loadingDialog.startLoading()

                    App.instance.backOffice.deleteTemplate(object : Listener<Any> {
                        override fun onResponse(response: Any?) {

                            loadingDialog.isDismiss()

                            if (isAdded) {

                                if (response == null) {

                                    templates.removeAt(position)
                                    templatesAdapter.notifyItemRemoved(position)

                                }

                            }

                        }

                    }, item.templateId!!)


                }

                dialog.show()

            }
        })

        binding!!.addTemplate.setOnClickListener {

            val bundle = Bundle().apply {
                putBoolean("createTemplate", true)
            }

            findNavController().navigate(R.id.action_templatesFragment_to_addDocumentsFragment, bundle)

        }


        addTemplatesToList()


    }

    private fun addTemplatesToList() {

        loadingDialog.startLoading()

        templates.clear()

        App.instance.backOffice.listTemplates(object : Listener<Any> {
            override fun onResponse(response: Any?) {

                loadingDialog.isDismiss()

                if (isAdded) {

                    if (response != null && response is ApiInterface.TemplateRequests) {

                        val list = response.templates

                        if (list!!.isNotEmpty()) {

                            recyclerViewTemplates.visibility = View.VISIBLE
                            binding!!.empty.visibility = View.GONE
                            templates.addAll(list)
                            templatesAdapter.notifyDataSetChanged()

                        }
                        else {
                            recyclerViewTemplates.visibility = View.GONE
                            binding!!.empty.visibility = View.VISIBLE
                        }

                    }
                    else {
                        recyclerViewTemplates.visibility = View.GONE
                        binding!!.empty.visibility = View.VISIBLE
                    }

                }

            }

        }, 1, 100, "")

    }

}