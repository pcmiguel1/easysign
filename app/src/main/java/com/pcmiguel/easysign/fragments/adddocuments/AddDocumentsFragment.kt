package com.pcmiguel.easysign.fragments.adddocuments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.LoadingActivity
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.Utils.openActivity
import com.pcmiguel.easysign.databinding.FragmentAddDocumentsBinding
import com.pcmiguel.easysign.fragments.adddocuments.adapter.DocumentsAdapter
import com.pcmiguel.easysign.fragments.adddocuments.model.Document
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

                dialog.show()


            }

        })

        addDocumentsToList()

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

}