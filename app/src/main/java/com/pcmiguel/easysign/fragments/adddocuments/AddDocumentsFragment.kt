package com.pcmiguel.easysign.fragments.adddocuments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.*
import com.pcmiguel.easysign.databinding.FragmentAddDocumentsBinding
import com.pcmiguel.easysign.fragments.adddocuments.adapter.DocumentsAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.libraries.scanner.activity.ScanActivity
import com.pcmiguel.easysign.libraries.scanner.constants.ScanConstants
import com.pcmiguel.easysign.libraries.scanner.util.ScanUtils
import com.pcmiguel.easysign.services.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.collections.ArrayList

class AddDocumentsFragment : Fragment() {

    private var binding: FragmentAddDocumentsBinding? = null

    private lateinit var recyclerViewDocuments: RecyclerView
    private var documents: MutableList<File> = mutableListOf()
    private lateinit var documentsAdapter: DocumentsAdapter

    private var noRecipients = false
    private var createTemplate = false

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentAddDocumentsBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        if (arguments != null && requireArguments().containsKey("pdfFile")) {
            val pdfFile = arguments?.getSerializable("pdfFile") as File
            if (pdfFile != null) {
                documents.add(pdfFile)
            }
        }

        if (arguments != null && requireArguments().containsKey("pdfsFile")) {

            val imagesUri = arguments?.getSerializable("pdfsFile") as? ArrayList<File>

            if (imagesUri != null && imagesUri.isNotEmpty()) {
                documents.addAll(imagesUri)
            }

        }

        if (arguments != null && requireArguments().containsKey("noRecipients")) {

            noRecipients = arguments?.getBoolean("noRecipients") ?: false

        }

        if (arguments != null && requireArguments().containsKey("createTemplate")) {

            createTemplate = arguments?.getBoolean("createTemplate")?: false

        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Utils.navigationBar(view, "Add Documents", requireActivity())

        loadingDialog = LoadingDialog(requireContext())

        if (noRecipients) binding!!.nextBtn.text = "Send"
        else binding!!.nextBtn.text = "Next"

        if (createTemplate) binding!!.agreementName.visibility = View.GONE

        recyclerViewDocuments = binding!!.documents
        recyclerViewDocuments.setHasFixedSize(true)
        recyclerViewDocuments.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        documentsAdapter = DocumentsAdapter(documents)
        recyclerViewDocuments.adapter = documentsAdapter

        documentsAdapter.setOnItemClickListener(object : DocumentsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = documentsAdapter.getItem(position)

                Utils.openPdf(item, requireContext())

            }

        })

        documentsAdapter.onOptionsClickListener(object : DocumentsAdapter.onOptionsClickListener {
            override fun onItemClick(position: Int) {

                val item = documentsAdapter.getItem(position)

                val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_options_document, null)
                val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
                dialog.setContentView(view)
                dialog.setCancelable(true)

                val renameBtn = dialog.findViewById<View>(R.id.renameBtn)
                val deleteBtn = dialog.findViewById<View>(R.id.deleteBtn)


                renameBtn!!.setOnClickListener {

                    dialog.dismiss()

                    val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_rename_document, null)

                    val builder = AlertDialog.Builder(requireContext())
                        .setView(mDialogView)
                        .setCancelable(false)

                    val dialog = builder.create()
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                    val nameText = mDialogView.findViewById<EditText>(R.id.name)
                    val cancelBtn = mDialogView.findViewById<View>(R.id.cancelBtn)
                    val okBtn = mDialogView.findViewById<View>(R.id.okBtn)

                    nameText.setText(item.name.replace(".pdf", ""))

                    cancelBtn.setOnClickListener {
                        dialog.dismiss()
                    }

                    okBtn.setOnClickListener {

                        dialog.dismiss()

                        val newName = nameText.text.toString() + ".pdf"

                        val newPath = item.toPath().resolveSibling(newName)
                        try {
                            Files.move(item.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING)
                            val file = File(newPath.toString())

                            documents.removeAt(position)
                            documents.add(file)

                            documentsAdapter.notifyItemChanged(position)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    dialog.show()

                }

                deleteBtn!!.setOnClickListener {

                    dialog.dismiss()

                    documents.removeAt(position)
                    documentsAdapter.notifyItemRemoved(position)

                }

                dialog.show()


            }

        })

        binding!!.addDocument.setOnClickListener {

            val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_options_documents2, null)

            val builder = AlertDialog.Builder(requireContext())
                .setView(mDialogView)
                .setCancelable(true)

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val scanButton = mDialogView.findViewById<View>(R.id.scanBtn)
            val photoButton = mDialogView.findViewById<View>(R.id.photoBtn)
            val browseButton = mDialogView.findViewById<View>(R.id.browseBtn)


            scanButton.setOnClickListener {

                dialog.dismiss()

                val intent = Intent(requireContext(), ScanActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)

            }

            photoButton.setOnClickListener {

                dialog.dismiss()

                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, REQUEST_CODE_GALLERY)

            }

            browseButton.setOnClickListener {

                dialog.dismiss()

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf" // Limit the selection to PDF files
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, FILE_PICKER2_EXTRACT_REQUEST_CODE)

            }

            dialog.show()

        }

        binding!!.nextBtn.setOnClickListener {

            val agreementName = binding!!.agreementName.text.toString()

            if (createTemplate) {

                if (documents.isNotEmpty()) {

                    requireArguments().remove("pdfFile")

                    val bundle = Bundle().apply {
                        //putString("templateName", binding!!.agreementName.text.toString())
                        putSerializable("documents", ArrayList(documents))
                    }

                    findNavController().navigate(R.id.action_addDocumentsFragment_to_addSignerRolesFragment, bundle)

                }
                else {
                    Toast.makeText(requireContext(), "Please select at least one document.", Toast.LENGTH_SHORT).show()
                }

            } else {

                if (documents.isNotEmpty() && agreementName.isNotEmpty()) {

                    requireArguments().remove("pdfFile")

                    if (noRecipients) {

                        loadingDialog.startLoading()
                        uploadDocumentsToDropbox(documents)

                    }
                    else {

                        val bundle = Bundle().apply {
                            putString("agreementName", binding!!.agreementName.text.toString())
                            putSerializable("documents", ArrayList(documents))
                        }

                        findNavController().navigate(R.id.action_addDocumentsFragment_to_addRecipientFragment, bundle)

                    }

                }
                else {

                    var erro = ""
                    if (agreementName.isEmpty()) erro = "Give the agreement a name!"
                    else if (documents.isEmpty()) erro = "Please select at least one document."

                    Toast.makeText(requireContext(), erro, Toast.LENGTH_SHORT).show()
                }

            }

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {

            val selectedImageUri = data?.data

            if (selectedImageUri != null) {

                requireArguments().remove("pdfFile")

                val bundle = Bundle().apply {
                    putParcelable("imageUri", selectedImageUri)
                    putSerializable("imagesUri", ArrayList(documents))
                    putBoolean("newImage", true)
                }

                findNavController().navigate(R.id.createDocumentFragment, bundle)

            }

        }
        else if (requestCode == FILE_PICKER2_EXTRACT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val uri = data?.data
            if (uri != null) {

                if (Utils.isPdfFile(uri, requireContext())) {

                    val filePdf = Utils.uriToPdfFile(requireContext(), uri)

                    if (filePdf != null) {

                        documents.add(filePdf)
                        documentsAdapter.notifyDataSetChanged()

                    }

                }
                else {
                    // The selected file is not a PDF. You can display an error message to the user.
                    Toast.makeText(requireContext(), "The selected file is not a PDF", Toast.LENGTH_SHORT).show()
                }

            }

        }
        else if (requestCode == REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {

            if (data != null && data.extras != null) {

                val filePath = data.getStringExtra(ScanConstants.SCANNED_RESULT)
                val baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME)
                val uri = Utils.bitmapToUri(requireContext(), baseBitmap)
                if (uri != null) {

                    requireArguments().remove("pdfFile")

                    val bundle = Bundle().apply {
                        putParcelable("imageUri", uri)
                        putSerializable("imagesUri", ArrayList(documents))
                        putBoolean("newImage", true)
                    }

                    findNavController().navigate(R.id.createDocumentFragment, bundle)

                }


            }

        }

    }

    private fun uploadDocumentsToDropbox(documentFiles: List<File>) {
        // Start a coroutine in the background
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val remoteFolderPath = "/documents"
                val client = DbxClientV2(
                    DbxRequestConfig.newBuilder("easysignapp").build(),
                    App.instance.preferences.getString("AccessToken", "")
                )

                val uploadedPaths : ArrayList<String> = arrayListOf()

                for (documentFile in documentFiles) {

                    val remoteFileName = documentFile.name.replace(".pdf", "") + "_" + App.instance.preferences.getString("UserId", "") + "_" + Date().time.toString() + ".pdf"

                    val remotePath = "$remoteFolderPath/$remoteFileName"
                    val inputStream = documentFile.inputStream()
                    val fileMetadata = client.files().uploadBuilder(remotePath)
                        .withMode(WriteMode.OVERWRITE) // Specify this line to overwrite the existing file
                        .uploadAndFinish(inputStream)

                    // Handle the result of the upload here, if needed
                    val uploadedPath = fileMetadata.pathDisplay

                    // Get the shared link for the uploaded file
                    val sharedLink = client.sharing().createSharedLinkWithSettings(uploadedPath)

                    // Add the `dl=1` parameter to force download
                    val sharedLinkUrl = sharedLink.url + "&dl=1"

                    Log.d("sharedLinkUrl", sharedLinkUrl)

                    //val sharedLinkUrl = sharedLink.url

                    uploadedPaths.add(sharedLinkUrl)

                }

                val json = JsonObject()

                json.addProperty("client_id", BuildConfig.CLIENT_ID)
                json.addProperty("title", binding!!.agreementName.text.toString())
                json.addProperty("subject", binding!!.agreementName.text.toString())
                json.addProperty("message", binding!!.agreementName.text.toString())

                val signers = JsonArray()
                val ccEmails = JsonArray()

                val email = App.instance.preferences.getString("Email", "")
                val name = App.instance.preferences.getString("Name", "")

                val signer = JsonObject()
                //ccEmails.add(email)
                signer.addProperty("email_address", email)
                signer.addProperty("name", name)
                signers.add(signer)

                json.add("signers", signers)
                json.add("cc_email_addresses", ccEmails)

                val files = JsonArray()

                for (uploadedPath in uploadedPaths) {
                    files.add(uploadedPath)
                }

                json.add("file_urls", files)

                val options = JsonObject()
                options.addProperty("draw", true)
                options.addProperty("type", true)
                options.addProperty("upload", true)
                options.addProperty("phone", false)
                options.addProperty("default_type", "draw")

                json.add("signing_options", options)

                json.addProperty("test_mode", true)


                App.instance.backOffice.createEmbeddedSignatureRequest(object : Listener<Any> {
                    override fun onResponse(response: Any?) {

                        if (isAdded) {

                            if (response != null && response is ApiInterface.CreateEmbeddedSignatureRequest) {

                                //Toast.makeText(requireContext(), "document sent!", Toast.LENGTH_SHORT).show()

                                //get my signature id
                                val signatureId = response.signatureRequest!!.signatures?.get(0)?.signatureId

                                //sign the document

                                if (signatureId != null) {

                                    App.instance.backOffice.getEmbeddedSignURL(object : Listener<Any> {
                                        override fun onResponse(response: Any?) {

                                            GlobalScope.launch(Dispatchers.IO) {
                                                // You can update the UI from the main thread if needed
                                                withContext(Dispatchers.Main) {
                                                    loadingDialog.isDismiss()
                                                }
                                            }

                                            if (isAdded) {

                                                if (response != null && response is ApiInterface.EmbeddedResponse) {


                                                    GlobalScope.launch(Dispatchers.IO) {
                                                        withContext(Dispatchers.Main) {

                                                            val bundle = Bundle().apply {
                                                                putString("signUrl", response.embedded!!.signUrl!!)
                                                            }

                                                            findNavController().navigate(R.id.action_addDocumentsFragment_to_signDocumentFragment, bundle)

                                                        }
                                                    }

                                                }

                                            }

                                        }

                                    }, signatureId)

                                }


                                /*GlobalScope.launch(Dispatchers.IO) {
                                    withContext(Dispatchers.Main) {
                                        findNavController().navigate(R.id.homeFragment2)
                                    }
                                }*/

                            }
                            else if (response != null && response is String) {

                                GlobalScope.launch(Dispatchers.IO) {
                                    // You can update the UI from the main thread if needed
                                    withContext(Dispatchers.Main) {
                                        loadingDialog.isDismiss()
                                    }
                                }

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

                                    GlobalScope.launch(Dispatchers.IO) {
                                        withContext(Dispatchers.Main) {
                                            findNavController().navigate(R.id.homeFragment2)
                                        }
                                    }

                                }

                                dialog.show()

                            }

                        }

                    }

                }, json)

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle any exceptions that may occur during the Dropbox upload
                // You can also update the UI with an error message from the main thread
                if (e is com.dropbox.core.InvalidAccessTokenException) {

                    Utils.logout(requireContext())

                }
            }
        }
    }

    companion object {
        private const val FILE_PICKER2_EXTRACT_REQUEST_CODE = 198
        private const val REQUEST_CODE_GALLERY = 334
        private const val REQUEST_CODE = 101
    }

}