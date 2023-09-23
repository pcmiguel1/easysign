package com.pcmiguel.easysign.fragments.templates

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentTemplatesBinding
import com.pcmiguel.easysign.fragments.templates.adapter.TemplatesAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiInterface


class TemplatesFragment : Fragment() {

    private var binding: FragmentTemplatesBinding? = null

    private lateinit var recyclerViewTemplates: RecyclerView
    private var templates: MutableList<ApiInterface.Templates> = mutableListOf()
    private lateinit var templatesAdapter: TemplatesAdapter

    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentTemplatesBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        Utils.navigationBar(view, "Templates", requireActivity())

        recyclerViewTemplates = binding!!.templatesList
        recyclerViewTemplates.setHasFixedSize(true)
        recyclerViewTemplates.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        templatesAdapter = TemplatesAdapter(templates)
        recyclerViewTemplates.adapter = templatesAdapter


        templatesAdapter.onEditClickListener(object : TemplatesAdapter.onEditClickListener {
            override fun onItemClick(position: Int) {

                val item = templatesAdapter.getItem(position)

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