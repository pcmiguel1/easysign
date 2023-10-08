package com.pcmiguel.easysign.fragments.addrole

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.BuildConfig
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentAddSignerRoleBinding
import com.pcmiguel.easysign.fragments.addrole.adapter.SignerRoleAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddSignerRoleFragment : Fragment() {

    private var binding : FragmentAddSignerRoleBinding? = null

    private lateinit var recyclerViewRoles: RecyclerView
    private lateinit var signerRoleAdapter: SignerRoleAdapter

    private var templateSelected : ApiInterface.Templates? = null

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentAddSignerRoleBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        if (arguments != null && requireArguments().containsKey("itemSelected")) {

            templateSelected = arguments?.getParcelable("itemSelected")


        }

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Utils.navigationBar(view, "Add Signers", requireActivity())

        loadingDialog = LoadingDialog(requireContext())

        recyclerViewRoles = binding!!.signers
        recyclerViewRoles.setHasFixedSize(true)
        recyclerViewRoles.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        signerRoleAdapter = SignerRoleAdapter(templateSelected!!.signerRoles!!)
        recyclerViewRoles.adapter = signerRoleAdapter


        binding!!.nextBtn.setOnClickListener {

            if (signerRoleAdapter.validateItems()) {

                loadingDialog.startLoading()

                val signersList = signerRoleAdapter.getAllItems()

                val json = JsonObject()

                json.addProperty("client_id", BuildConfig.CLIENT_ID)

                val templates = JsonArray()
                templates.add(templateSelected!!.templateId)

                json.add("template_ids", templates)


                json.addProperty("subject", "")
                json.addProperty("message", "")

                val signers = JsonArray()

                for ((count, recipient) in signersList.withIndex()) {

                    val signer = JsonObject()
                    signer.addProperty("role", recipient.role)
                    signer.addProperty("name", recipient.nameSigner)
                    signer.addProperty("email_address", recipient.emailSigner)

                    signers.add(signer)

                }
                json.add("signers", signers)

                val options = JsonObject()
                options.addProperty("draw", true)
                options.addProperty("type", true)
                options.addProperty("upload", true)
                options.addProperty("phone", false)
                options.addProperty("default_type", "draw")

                json.add("signing_options", options)

                json.addProperty("test_mode", true)


                App.instance.backOffice.createEmbeddedSignatureRequestWithTemplate(object : Listener<Any> {
                    override fun onResponse(response: Any?) {

                        GlobalScope.launch(Dispatchers.IO) {
                            // You can update the UI from the main thread if needed
                            withContext(Dispatchers.Main) {
                                loadingDialog.isDismiss()
                            }
                        }

                        if (isAdded) {

                            if (response != null && response is ApiInterface.CreateEmbeddedSignatureRequest) {

                                Toast.makeText(requireContext(), "document sent!", Toast.LENGTH_SHORT).show()

                                GlobalScope.launch(Dispatchers.IO) {
                                    withContext(Dispatchers.Main) {
                                        findNavController().navigate(R.id.homeFragment2)
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

            }
            else {
                Toast.makeText(requireContext(), "Fill in all the spaces above correctly!", Toast.LENGTH_SHORT).show()
            }

        }

    }

}