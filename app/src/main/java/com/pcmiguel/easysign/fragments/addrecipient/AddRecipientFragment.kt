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

        addRecipientsToList()

        binding!!.addRecipient.setOnClickListener {

            val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_add_recipient, null)
            val dialog = BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme)
            dialog.setContentView(view)
            dialog.setCancelable(true)

            val name = dialog.findViewById<EditText>(R.id.name)
            val email = dialog.findViewById<EditText>(R.id.email)
            val saveBtn = dialog.findViewById<View>(R.id.saveBtn)
            val assigntomeBtn = dialog.findViewById<View>(R.id.assigntomeBtn)

            assigntomeBtn!!.setOnClickListener {

                name!!.setText(App.instance.preferences.getString("Name", ""))
                email!!.setText(App.instance.preferences.getString("Email", ""))

            }



            dialog.show()

        }

    }

    private  fun addRecipientsToList() {

        recipients.clear()

        recipients.add(Recipient("", "", ""))
        recipients.add(Recipient("", "", ""))

        recipientsAdapter.notifyDataSetChanged()

    }

}