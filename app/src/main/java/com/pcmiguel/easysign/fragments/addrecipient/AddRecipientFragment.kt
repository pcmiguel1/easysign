package com.pcmiguel.easysign.fragments.addrecipient

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils
import com.pcmiguel.easysign.databinding.FragmentAddRecipientBinding
import com.pcmiguel.easysign.fragments.adddocuments.adapter.DocumentsAdapter
import com.pcmiguel.easysign.fragments.addrecipient.adapter.RecipientsAdapter
import com.pcmiguel.easysign.fragments.addrecipient.model.Recipient

class AddRecipientFragment : Fragment() {

    private var binding: FragmentAddRecipientBinding? = null

    private lateinit var recyclerViewRecipients: RecyclerView
    private var recipients: MutableList<Recipient> = mutableListOf()
    private lateinit var recipientsAdapter: RecipientsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentAddRecipientBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Utils.navigationBar(view, "Add Recipients", requireActivity())

        recyclerViewRecipients = binding!!.recipients
        recyclerViewRecipients.setHasFixedSize(true)
        recyclerViewRecipients.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        recipientsAdapter = RecipientsAdapter(recipients)
        recyclerViewRecipients.adapter = recipientsAdapter

        recipientsAdapter.setOnItemClickListener(object : RecipientsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = recipientsAdapter.getItem(position)

            }

        })

        recipientsAdapter.onOptionsClickListener(object : RecipientsAdapter.onOptionsClickListener {
            override fun onItemClick(position: Int) {

                val item = recipientsAdapter.getItem(position)

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

    }

}