package com.pcmiguel.easysign.fragments.templates

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
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.sharing.RequestedVisibility
import com.dropbox.core.v2.sharing.SharedLinkSettings
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.BuildConfig
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentAddSignerRolesBinding
import com.pcmiguel.easysign.fragments.addrecipient.model.Recipient
import com.pcmiguel.easysign.fragments.templates.adapter.RoleAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class AddSignerRolesFragment : Fragment() {

    private var binding : FragmentAddSignerRolesBinding? = null

    private lateinit var recyclerViewRoles : RecyclerView
    private var roles : MutableList<String> = mutableListOf()
    private var finalDocuments: MutableList<File> = mutableListOf()
    private lateinit var roleAdapter: RoleAdapter

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentAddSignerRolesBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        /*if (arguments != null && requireArguments().containsKey("templateName")) {

            templateName = arguments?.getString("templateName") ?: ""

        }*/

        if (arguments != null && requireArguments().containsKey("documents")) {

            val documents = arguments?.getSerializable("documents") as? ArrayList<File>
            if (documents != null) finalDocuments.addAll(documents)

        }

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        Utils.navigationBar(view, "Add Signer Roles", requireActivity())

        recyclerViewRoles = binding!!.roles
        recyclerViewRoles.setHasFixedSize(true)
        recyclerViewRoles.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        roleAdapter = RoleAdapter(roles)
        recyclerViewRoles.adapter = roleAdapter

        binding!!.createBtn.setOnClickListener {

            if (roles.isNotEmpty()) {

                loadingDialog.startLoading()
                uploadDocumentsToDropbox(finalDocuments, roles)

            }
            else {
                Toast.makeText(requireContext(), "Please add at least one role.", Toast.LENGTH_SHORT).show()
            }

        }

        roleAdapter.setOnItemClickListener(object : RoleAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = roleAdapter.getItem(position)

                val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_add_role, null)
                val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
                dialog.setContentView(view)
                dialog.setCancelable(true)

                val name = dialog.findViewById<EditText>(R.id.name)

                val saveBtn = dialog.findViewById<View>(R.id.saveBtn)

                name!!.setText(item)

                saveBtn!!.setOnClickListener {

                    if (name!!.text.isNotEmpty()) {

                        var exists = false
                        for (role in roles) {
                            if (name!!.text.toString() == role) exists = true
                        }

                        if (!exists) {

                            dialog.dismiss()

                            roles[position] = name.text.toString()

                            roleAdapter.notifyItemChanged(position)

                        }
                        else {
                            Toast.makeText(requireContext(), "Role already listed!", Toast.LENGTH_SHORT).show()
                        }

                    }
                    else {
                        var erro = ""
                        if (name!!.text.isEmpty()) erro = "Enter a role name."
                        Toast.makeText(requireContext(), erro, Toast.LENGTH_SHORT).show()
                    }

                }



                dialog.show()

            }
        })

        roleAdapter.onOptionsClickListener(object : RoleAdapter.onOptionsClickListener {
            override fun onItemClick(position: Int) {

                val item = roleAdapter.getItem(position)

                val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_options_recipipient, null)
                val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
                dialog.setContentView(view)
                dialog.setCancelable(true)

                val deleteBtn = dialog.findViewById<View>(R.id.deleteBtn)

                deleteBtn!!.setOnClickListener {

                    dialog.dismiss()

                    roles.removeAt(position)
                    roleAdapter.notifyItemRemoved(position)

                }

                dialog.show()

            }
        })

        binding!!.addRole.setOnClickListener {

            val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_add_role, null)
            val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
            dialog.setContentView(view)
            dialog.setCancelable(true)

            val name = dialog.findViewById<EditText>(R.id.name)

            val saveBtn = dialog.findViewById<View>(R.id.saveBtn)

            saveBtn!!.setOnClickListener {

                if (name!!.text.isNotEmpty()) {

                    var exists = false
                    for (role in roles) {
                        if (name!!.text.toString() == role) exists = true
                    }

                    if (!exists) {

                        dialog.dismiss()

                        roles.add(name.text.toString())
                        roleAdapter.notifyDataSetChanged()

                    }
                    else {
                        Toast.makeText(requireContext(), "Role already listed!", Toast.LENGTH_SHORT).show()
                    }

                }
                else {
                    var erro = ""
                    if (name!!.text.isEmpty()) erro = "Enter a role name."
                    Toast.makeText(requireContext(), erro, Toast.LENGTH_SHORT).show()
                }

            }

            dialog.show()

        }

    }

    private fun uploadDocumentsToDropbox(documentFiles: List<File>, roles : MutableList<String>) {
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

                    //val sharedLinkUrl = sharedLink.url

                    uploadedPaths.add(sharedLinkUrl)

                }

                val json = JsonObject()

                json.addProperty("client_id", BuildConfig.CLIENT_ID)
                json.addProperty("title", "")
                json.addProperty("subject", "")
                json.addProperty("message", "")

                val singerRoles = JsonArray()
                var signerCount = 0

                for ((count, role) in roles.withIndex()) {

                    val signerRole = JsonObject()
                    signerRole.addProperty("name", role)
                    signerRole.addProperty("order", signerCount)
                    singerRoles.add(signerRole)
                    signerCount++

                }
                json.add("signer_roles", singerRoles)

                val files = JsonArray()

                for (uploadedPath in uploadedPaths) {
                    files.add(uploadedPath)
                }

                json.add("file_urls", files)

                json.addProperty("test_mode", true)

                Log.d("jsoncreate", json.toString())


                App.instance.backOffice.createEmbeddedTemplateDraft(object : Listener<Any> {
                    override fun onResponse(response: Any?) {

                        GlobalScope.launch(Dispatchers.IO) {
                            // You can update the UI from the main thread if needed
                            withContext(Dispatchers.Main) {
                                loadingDialog.isDismiss()
                            }
                        }

                        if (isAdded) {

                            if (response != null && response is ApiInterface.CreateEmbeddedDraftRequest) {

                                GlobalScope.launch(Dispatchers.IO) {
                                    withContext(Dispatchers.Main) {
                                       // findNavController().navigate(R.id.templatesFragment)

                                        Log.d("editUrl", response.template!!.editUrl.toString())

                                        val bundle = Bundle().apply {
                                            putString("editUrl", response.template!!.editUrl)
                                        }

                                        findNavController().navigate(R.id.action_addSignerRolesFragment_to_editDocumentFragment, bundle)

                                    }
                                }

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
            }
        }
    }

}