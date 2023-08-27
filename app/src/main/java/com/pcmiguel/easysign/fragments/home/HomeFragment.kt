package com.pcmiguel.easysign.fragments.home

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.card.MaterialCardView
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.databinding.FragmentHomeBinding
import com.pcmiguel.easysign.fragments.home.adapter.RequestsAdapter
import com.pcmiguel.easysign.services.ApiInterface

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    private lateinit var recyclerViewRequests: RecyclerView
    private var requests: MutableList<ApiInterface.Requests> = mutableListOf()
    private lateinit var requestsAdapter: RequestsAdapter

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

        recyclerViewRequests = binding!!.requests
        recyclerViewRequests.setHasFixedSize(true)
        recyclerViewRequests.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        requestsAdapter = RequestsAdapter(requests)
        recyclerViewRequests.adapter = requestsAdapter

        requestsAdapter.setOnItemClickListener(object : RequestsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = requestsAdapter.getItem(position)

            }

        })

        addRequestsToList()

        binding!!.createSignatureBtn.setOnClickListener {

            createSignature()

        }


    }

    private fun addRequestsToList() {

        requests.clear()

        recyclerViewRequests.visibility = View.VISIBLE

        requests.add(ApiInterface.Requests())
        requests.add(ApiInterface.Requests())
        requests.add(ApiInterface.Requests())

        requestsAdapter.notifyDataSetChanged()


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
            val signatureBitmap = signaturePad.signatureBitmap
            val signature = binding!!.signature

            signature.setImageBitmap(signatureBitmap)

            dialog.dismiss()

        }

        dialog.show()


    }

}