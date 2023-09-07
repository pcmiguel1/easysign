package com.pcmiguel.easysign.fragments.addrecipient

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
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
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentAddRecipientBinding
import com.pcmiguel.easysign.fragments.adddocuments.adapter.DocumentsAdapter
import com.pcmiguel.easysign.fragments.addrecipient.adapter.RecipientsAdapter
import com.pcmiguel.easysign.fragments.addrecipient.model.Recipient
import com.pcmiguel.easysign.libraries.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import kotlin.math.sin

class AddRecipientFragment : Fragment() {

    private var binding: FragmentAddRecipientBinding? = null

    private lateinit var recyclerViewRecipients: RecyclerView
    private var recipients: MutableList<Recipient> = mutableListOf()
    private var finalDocuments: MutableList<File> = mutableListOf()
    private lateinit var recipientsAdapter: RecipientsAdapter

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentAddRecipientBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        if (arguments != null && requireArguments().containsKey("documents")) {

            val documents = arguments?.getSerializable("documents") as? ArrayList<File>
            if (documents != null) finalDocuments.addAll(documents)

        }

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        Utils.navigationBar(view, "Add Recipients", requireActivity())

        recyclerViewRecipients = binding!!.recipients
        recyclerViewRecipients.setHasFixedSize(true)
        recyclerViewRecipients.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        recipientsAdapter = RecipientsAdapter(recipients)
        recyclerViewRecipients.adapter = recipientsAdapter

        recipientsAdapter.setOnItemClickListener(object : RecipientsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = recipientsAdapter.getItem(position)

                val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_add_recipient, null)
                val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
                dialog.setContentView(view)
                dialog.setCancelable(true)

                val name = dialog.findViewById<EditText>(R.id.name)
                val email = dialog.findViewById<EditText>(R.id.email)

                val needsToSignRadio = dialog.findViewById<RadioButton>(R.id.needToSignRadio)
                val inPersonSignerRadio = dialog.findViewById<RadioButton>(R.id.inPersonSignerRadio)
                val receivesCopyRadio = dialog.findViewById<RadioButton>(R.id.receivesCopyRadio)

                val saveBtn = dialog.findViewById<View>(R.id.saveBtn)
                val assigntomeBtn = dialog.findViewById<View>(R.id.assigntomeBtn)

                name!!.setText(item.name)
                email!!.setText(item.email)

                when (item.role) {

                    "Needs to sign" -> {
                        needsToSignRadio!!.isChecked = true
                    }

                    "In-person signer" -> {
                        inPersonSignerRadio!!.isChecked = true
                    }

                    "Receives a copy" -> {
                        receivesCopyRadio!!.isChecked = true
                    }

                }

                assigntomeBtn!!.setOnClickListener {

                    name!!.setText(App.instance.preferences.getString("Name", ""))
                    email!!.setText(App.instance.preferences.getString("Email", ""))

                }

                needsToSignRadio!!.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        inPersonSignerRadio!!.isChecked = false
                        receivesCopyRadio!!.isChecked = false
                    }
                }

                inPersonSignerRadio!!.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        needsToSignRadio!!.isChecked = false
                        receivesCopyRadio!!.isChecked = false
                    }
                }

                receivesCopyRadio!!.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        inPersonSignerRadio!!.isChecked = false
                        needsToSignRadio!!.isChecked = false
                    }
                }

                saveBtn!!.setOnClickListener {

                    if (name!!.text.isNotEmpty() && email!!.text.isNotEmpty()) {

                        var exists = false
                        for (reci in recipients) {
                            if (email!!.text.toString() == reci.email) exists = true
                        }

                        if (!exists) {

                            dialog.dismiss()

                            var role = ""
                            if (needsToSignRadio.isChecked) role = "Needs to sign"
                            if (inPersonSignerRadio.isChecked) role = "In-person signer"
                            if (receivesCopyRadio.isChecked) role = "Receives a copy"

                            var me = false
                            if (App.instance.preferences.getString("Email", "") == email!!.text.toString()) me = true

                            recipients[position].name = name.text.toString()
                            recipients[position].email = email.text.toString()
                            recipients[position].role = role
                            recipients[position].me = me

                            recipientsAdapter.notifyItemChanged(position)

                        }
                        else {
                            Toast.makeText(requireContext(), "Recipient already listed!", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else {
                        var erro = ""
                        if (name!!.text.isEmpty()) erro = "Enter a recipient name."
                        if (email!!.text.isEmpty()) erro = "Enter a recipient email."
                        Toast.makeText(requireContext(), erro, Toast.LENGTH_SHORT).show()
                    }

                }



                dialog.show()

            }

        })

        recipientsAdapter.onOptionsClickListener(object : RecipientsAdapter.onOptionsClickListener {
            override fun onItemClick(position: Int) {

                val item = recipientsAdapter.getItem(position)

                val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_options_recipipient, null)
                val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
                dialog.setContentView(view)
                dialog.setCancelable(true)

                val deleteBtn = dialog.findViewById<View>(R.id.deleteBtn)

                deleteBtn!!.setOnClickListener {

                    dialog.dismiss()

                    recipients.removeAt(position)
                    recipientsAdapter.notifyItemRemoved(position)

                }

                dialog.show()

            }

        })

        binding!!.addRecipient.setOnClickListener {

            val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_add_recipient, null)
            val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
            dialog.setContentView(view)
            dialog.setCancelable(true)

            val name = dialog.findViewById<EditText>(R.id.name)
            val email = dialog.findViewById<EditText>(R.id.email)

            val needsToSignRadio = dialog.findViewById<RadioButton>(R.id.needToSignRadio)
            val inPersonSignerRadio = dialog.findViewById<RadioButton>(R.id.inPersonSignerRadio)
            val receivesCopyRadio = dialog.findViewById<RadioButton>(R.id.receivesCopyRadio)

            val saveBtn = dialog.findViewById<View>(R.id.saveBtn)
            val assigntomeBtn = dialog.findViewById<View>(R.id.assigntomeBtn)

            assigntomeBtn!!.setOnClickListener {

                name!!.setText(App.instance.preferences.getString("Name", ""))
                email!!.setText(App.instance.preferences.getString("Email", ""))

            }

            needsToSignRadio!!.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        inPersonSignerRadio!!.isChecked = false
                        receivesCopyRadio!!.isChecked = false
                    }
            }

            inPersonSignerRadio!!.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    needsToSignRadio!!.isChecked = false
                    receivesCopyRadio!!.isChecked = false
                }
            }

            receivesCopyRadio!!.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    inPersonSignerRadio!!.isChecked = false
                    needsToSignRadio!!.isChecked = false
                }
            }

            saveBtn!!.setOnClickListener {

                if (name!!.text.isNotEmpty() && email!!.text.isNotEmpty()) {

                    var exists = false
                    for (reci in recipients) {
                        if (email!!.text.toString() == reci.email) exists = true
                    }

                    if (!exists) {

                        dialog.dismiss()

                        var role = ""
                        if (needsToSignRadio.isChecked) role = "Needs to sign"
                        if (inPersonSignerRadio.isChecked) role = "In-person signer"
                        if (receivesCopyRadio.isChecked) role = "Receives a copy"

                        var me = false
                        if (App.instance.preferences.getString("Email", "") == email!!.text.toString()) me = true

                        recipients.add(Recipient(name.text.toString(), email.text.toString(), role, me))
                        recipientsAdapter.notifyDataSetChanged()

                    }
                    else {
                        Toast.makeText(requireContext(), "Recipient already listed!", Toast.LENGTH_SHORT).show()
                    }

                }
                else {
                    var erro = ""
                    if (name!!.text.isEmpty()) erro = "Enter a recipient name."
                    if (email!!.text.isEmpty()) erro = "Enter a recipient email."
                    Toast.makeText(requireContext(), erro, Toast.LENGTH_SHORT).show()
                }

            }



            dialog.show()

        }

        binding!!.nextBtn.setOnClickListener {

            if (recipients.isNotEmpty()) {

                loadingDialog.startLoading()
                uploadDocumentsToDropbox(finalDocuments, recipients)

            }
            else {
                Toast.makeText(requireContext(), "Please add at least one recipient.", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun uploadDocumentsToDropbox(documentFiles: List<File>, recipients : MutableList<Recipient>) {
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
                    val sharedLinkUrl = sharedLink.url

                    uploadedPaths.add(sharedLinkUrl)

                }

                val json = JsonObject()

                json.addProperty("client_id", "0444b778f0f8859d41e0535e40370be8")
                json.addProperty("title", "teste")
                json.addProperty("subject", "teste")
                json.addProperty("message", "teste")

                val signers = JsonArray()
                val ccEmails = JsonArray()

                for ((count, recipient) in recipients.withIndex()) {

                    val signer = JsonObject()
                    ccEmails.add(recipient.email)
                    signer.addProperty("email_address", recipient.email)
                    signer.addProperty("name", recipient.name)
                    signer.addProperty("order", count)
                    signers.add(signer)

                }
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

                           if (response != null) {

                               Toast.makeText(requireContext(), "document sent!", Toast.LENGTH_SHORT).show()

                           }

                       }

                    }

                }, json)

                // You can update the UI from the main thread if needed
                withContext(Dispatchers.Main) {
                    // Update the UI or display a success message
                    loadingDialog.isDismiss()
                    findNavController().navigate(R.id.homeFragment2)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle any exceptions that may occur during the Dropbox upload
                // You can also update the UI with an error message from the main thread
            }
        }
    }

}