package com.pcmiguel.easysign.fragments.adddocuments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pcmiguel.easysign.*
import com.pcmiguel.easysign.Utils.openActivity
import com.pcmiguel.easysign.databinding.FragmentAddDocumentsBinding
import com.pcmiguel.easysign.fragments.adddocuments.adapter.DocumentsAdapter
import com.pcmiguel.easysign.fragments.adddocuments.model.Document
import com.pcmiguel.easysign.fragments.scan.Scanner
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddDocumentsFragment : Fragment() {

    private var binding: FragmentAddDocumentsBinding? = null

    private lateinit var recyclerViewDocuments: RecyclerView
    private var documents: MutableList<Document> = mutableListOf()
    private lateinit var documentsAdapter: DocumentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentAddDocumentsBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Utils.navigationBar(view, "Add Documents", requireActivity())

        recyclerViewDocuments = binding!!.documents
        recyclerViewDocuments.setHasFixedSize(true)
        recyclerViewDocuments.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        documentsAdapter = DocumentsAdapter(documents)
        recyclerViewDocuments.adapter = documentsAdapter

        documentsAdapter.setOnItemClickListener(object : DocumentsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = documentsAdapter.getItem(position)

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

                    cancelBtn.setOnClickListener {
                        dialog.dismiss()
                    }

                    okBtn.setOnClickListener {

                    }

                    dialog.show()

                }

                deleteBtn!!.setOnClickListener {



                }

                dialog.show()


            }

        })

        addDocumentsToList()

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

                val intent = Intent(requireContext(), Scanner::class.java)
                startActivity(intent)

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

            findNavController().navigate(R.id.action_addDocumentsFragment_to_addRecipientFragment)

        }


    }

    private fun addDocumentsToList() {

        documents.clear()

        documents.add(Document("test", "3", ""))
        documents.add(Document("test", "3", ""))

        documentsAdapter.notifyDataSetChanged()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == AppCompatActivity.RESULT_OK) {

            val selectedImageUri = data?.data

            if (selectedImageUri != null) {



            }

        }
        else if (requestCode == FILE_PICKER2_EXTRACT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val uri = data?.data
            if (uri != null) {

                if (Utils.isPdfFile(uri, requireContext())) {


                }
                else {
                    // The selected file is not a PDF. You can display an error message to the user.
                    Toast.makeText(requireContext(), "The selected file is not a PDF", Toast.LENGTH_SHORT).show()
                }

            }

        }

    }

    companion object {
        private const val FILE_PICKER2_EXTRACT_REQUEST_CODE = 198
        private const val REQUEST_CODE_GALLERY = 334
    }

}