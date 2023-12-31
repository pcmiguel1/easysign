package com.pcmiguel.easysign.fragments.home

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.card.MaterialCardView
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentHomeBinding
import com.pcmiguel.easysign.fragments.home.adapter.RequestsAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private lateinit var recyclerViewRequests: RecyclerView
    private var requests: MutableList<ApiInterface.SignatureRequest> = mutableListOf()
    private lateinit var requestsAdapter: RequestsAdapter

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.VISIBLE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.VISIBLE

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        binding!!.welcome.text = String.format(getString(R.string.welcome), App.instance.preferences.getString("Name", ""))

        recyclerViewRequests = binding!!.requests
        recyclerViewRequests.setHasFixedSize(true)
        recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        requestsAdapter = RequestsAdapter(requests)
        recyclerViewRequests.adapter = requestsAdapter

        requestsAdapter.setOnItemClickListener(object : RequestsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = requestsAdapter.getItem(position)

                val bundle = Bundle().apply {
                    putParcelable("item", item)
                }

                findNavController().navigate(R.id.action_homeFragment2_to_documentDetailsFragment, bundle)

            }

        })

        addRequestsToList()

        binding!!.createSignatureBtn.setOnClickListener {

            createSignature()

        }

        binding!!.allBtn.setOnClickListener {
            findNavController().navigate(R.id.documentsFragment2)
        }

        binding!!.needActionBtn.setOnClickListener {

            val bundle = Bundle().apply {
                putInt("tabPosition", 0)
            }
            findNavController().navigate(R.id.documentsFragment2, bundle)
        }

        binding!!.waitingForOtherBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("tabPosition", 1)
            }
            findNavController().navigate(R.id.documentsFragment2, bundle)
        }

        binding!!.completedBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("tabPosition", 2)
            }
            findNavController().navigate(R.id.documentsFragment2, bundle)
        }

        binding!!.rejectedBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("tabPosition", 3)
            }
            findNavController().navigate(R.id.documentsFragment2, bundle)
        }

        val signature = binding!!.signature

        val client = DbxClientV2(
            DbxRequestConfig.newBuilder("easysignapp").build(),
            App.instance.preferences.getString("AccessToken", "")
        )
        val remoteFilePath = "/signatures/"+"signature_" + App.instance.preferences.getString("UserId", "")+".png"

        Log.d("remoteFilePath", remoteFilePath)

        lifecycleScope.launch {
            getSignature(client, remoteFilePath)
        }


    }

    private fun addRequestsToList() {

        loadingDialog.startLoading()

        requests.clear()

        var countComplete = 0
        var countNeedsAction = 0
        var countWaitingForOther = 0
        var countRejected = 0

        App.instance.backOffice.listSignatureRequests(object : Listener<Any> {
            override fun onResponse(response: Any?) {

                loadingDialog.isDismiss()

                if (isAdded) {

                    if (response != null && response is ApiInterface.SignatureRequests) {

                        val list = response.signatureRequests

                        if (list!!.isNotEmpty()) {

                            for (sig in list) {

                                val isComplete = sig.isComplete
                                val isDeclined = sig.isDeclined

                                //completed
                                if (isComplete) {
                                    countComplete++
                                }
                                else {

                                    if (sig.signatures != null) {

                                        val signers = sig.signatures?.toMutableList() ?: mutableListOf()

                                        var needsAction = false
                                        for (signer in signers) {
                                            if (signer.signerEmailAddress == App.instance.preferences.getString("Email", "") && signer.statusCode == "awaiting_signature") {
                                                needsAction = true
                                                break
                                            }
                                        }

                                        if (needsAction) {
                                            //needs action
                                            countNeedsAction++
                                        }
                                        else {
                                            //Waiting for other
                                            countWaitingForOther++
                                        }

                                    }

                                }

                                //rejected
                                if (isDeclined) {
                                    countRejected++
                                }

                            }

                            binding!!.needAction.text = countNeedsAction.toString()
                            binding!!.waitingForOther.text = countWaitingForOther.toString()
                            binding!!.completed.text = countComplete.toString()
                            binding!!.rejected.text = countRejected.toString()



                            recyclerViewRequests.visibility = View.VISIBLE
                            binding!!.empty.visibility = View.GONE
                            requests.addAll(list.take(10))
                            requestsAdapter.notifyDataSetChanged()

                        }
                        else {
                            recyclerViewRequests.visibility = View.GONE
                            binding!!.empty.visibility = View.VISIBLE
                        }

                    }
                    else {
                        recyclerViewRequests.visibility = View.GONE
                        binding!!.empty.visibility = View.VISIBLE
                    }

                }

            }

        }, 1, 100, "")

    }

    private fun createSignature() {

        val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_signature, null)

        val builder = AlertDialog.Builder(requireContext())
            .setView(mDialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val signaturePad = mDialogView.findViewById<SignaturePad>(R.id.signaturePad)
        val saveBtn = mDialogView.findViewById<AppCompatButton>(R.id.save)
        val clearBtn = mDialogView.findViewById<AppCompatButton>(R.id.clear)

        var blackColorCard = mDialogView.findViewById<MaterialCardView>(R.id.blackColor_card)
        var blackColor = mDialogView.findViewById<View>(R.id.blackColor)
        var blueColorCard = mDialogView.findViewById<MaterialCardView>(R.id.blueColor_card)
        var blueColor = mDialogView.findViewById<View>(R.id.blueColor)
        var redColorCard = mDialogView.findViewById<MaterialCardView>(R.id.redColor_card)
        var redColor = mDialogView.findViewById<View>(R.id.redColor)

        blackColorCard.setOnClickListener {

            blackColorCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.black)
            blueColorCard.strokeColor = Color.TRANSPARENT
            redColorCard.strokeColor = Color.TRANSPARENT

            signaturePad.setPenColor(ContextCompat.getColor(requireContext(), R.color.black))

        }

        blueColorCard.setOnClickListener {

            blackColorCard.strokeColor = Color.TRANSPARENT
            blueColorCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.black)
            redColorCard.strokeColor = Color.TRANSPARENT

            signaturePad.setPenColor(ContextCompat.getColor(requireContext(), R.color.blue))

        }

        redColorCard.setOnClickListener {

            blackColorCard.strokeColor = Color.TRANSPARENT
            blueColorCard.strokeColor = Color.TRANSPARENT
            redColorCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.black)

            signaturePad.setPenColor(ContextCompat.getColor(requireContext(), R.color.red))

        }

        signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {

            }

            override fun onSigned() {
                saveBtn.isEnabled = true
                clearBtn.isEnabled = true
            }

            override fun onClear() {
                saveBtn.isEnabled = false
                clearBtn.isEnabled = false
            }

        })

        clearBtn.setOnClickListener {
            signaturePad.clear()
        }

        saveBtn.setOnClickListener {

            loadingDialog.startLoading()

            val signatureBitmap = signaturePad.signatureBitmap
            val signature = binding!!.signature

            signature.setImageBitmap(signatureBitmap)

            val remoteFileName = "signature_" + App.instance.preferences.getString("UserId", "")+".png"
            val signatureFile = File(requireContext().cacheDir, remoteFileName)

            try {
                // Create a FileOutputStream to write the bitmap data to the file
                val fileOutputStream = FileOutputStream(signatureFile)

                // Compress the bitmap as PNG format and save it to the file
                signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

                // Close the FileOutputStream
                fileOutputStream.close()

                // Use a Kotlin coroutine to upload the image to Dropbox in the background
                uploadSignatureToDropbox(signatureFile, remoteFileName)

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle any exceptions that may occur during file saving
            }

            dialog.dismiss()

        }

        dialog.show()


    }

    private fun uploadSignatureToDropbox(signatureFile: File, remoteFileName: String) {
        // Start a coroutine in the background
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val remoteFolderPath = "/signatures"
                val client = DbxClientV2(
                    DbxRequestConfig.newBuilder("easysignapp").build(),
                    App.instance.preferences.getString("AccessToken", "")
                )

                val remotePath = "$remoteFolderPath/$remoteFileName"
                val inputStream = signatureFile.inputStream()
                val fileMetadata = client.files().uploadBuilder(remotePath)
                    .withMode(WriteMode.OVERWRITE) // Specify this line to overwrite the existing file
                    .uploadAndFinish(inputStream)

                // Handle the result of the upload here, if needed
                val uploadedPath = fileMetadata.pathDisplay

                // You can update the UI from the main thread if needed
                withContext(Dispatchers.Main) {
                    // Update the UI or display a success message
                    loadingDialog.isDismiss()
                }

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

    private suspend fun getSignature(client: DbxClientV2, remoteFilePath: String) {
        withContext(Dispatchers.IO) {
            try {

                val fileMetadata = client.files().download(remoteFilePath).inputStream
                val imageBitmap = if (fileMetadata != null) BitmapFactory.decodeStream(fileMetadata) else null

                if (imageBitmap != null) {
                    withContext(Dispatchers.Main) { binding!!.signature.setImageBitmap(imageBitmap) }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                if (e is com.dropbox.core.InvalidAccessTokenException) {

                    Utils.logout(requireContext())

                }
            }
        }
    }

}