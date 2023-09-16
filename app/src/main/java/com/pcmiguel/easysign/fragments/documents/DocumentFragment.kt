package com.pcmiguel.easysign.fragments.documents

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.databinding.FragmentDocumentBinding
import com.pcmiguel.easysign.fragments.home.adapter.RequestsAdapter
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiInterface

private const val ARG_TYPE = "ARG_TYPE"

class DocumentFragment : Fragment() {

    private var binding: FragmentDocumentBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var requests: MutableList<ApiInterface.SignatureRequest> = mutableListOf()
    private lateinit var requestsAdapter: RequestsAdapter

    private lateinit var loadingDialog: LoadingDialog

    private var type: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(ARG_TYPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentDocumentBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.VISIBLE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.VISIBLE

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        swipeRefresh = binding!!.swipeRefresh

        recyclerView = binding!!.documents
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        requestsAdapter = RequestsAdapter(requests)
        recyclerView.adapter = requestsAdapter

        requestsAdapter.setOnItemClickListener(object : RequestsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = requestsAdapter.getItem(position)

                val bundle = Bundle().apply {
                    putParcelable("item", item)
                }

                findNavController().navigate(R.id.action_documentsFragment2_to_documentDetailsFragment, bundle)

            }

        })

        //addRequestsToList(filter)
        //setSwipeRefresh(filter)

    }

    override fun onResume() {
        super.onResume()

        var filter = ""

        when (type) {

            0 -> {
                binding!!.emptyText.text = "Documents for you to sign will be displayed here."
                filter = "needAction"
            }

            1 -> {
                binding!!.emptyText.text = "Documents waiting for others to complete signing will be displayed here."
                filter = "waitingOther"
            }

            2 -> {
                binding!!.emptyText.text = "No completed documents yet."
                filter = "completed"
            }

            3 -> {
                binding!!.emptyText.text = "No canceled documents yet."
                filter = "canceled"
            }

        }

        addRequestsToList(filter)
        setSwipeRefresh(filter)

    }

    private fun addRequestsToList(filter: String) {

        loadingDialog.startLoading()
        requests.clear()

        App.instance.backOffice.listSignatureRequests(object : Listener<Any> {
            override fun onResponse(response: Any?) {

                loadingDialog.isDismiss()

                if (isAdded) {

                    if (response != null && response is ApiInterface.SignatureRequests) {

                        val list = response.signatureRequests

                        if (list!!.isNotEmpty()) {

                            val finalList = ArrayList<ApiInterface.SignatureRequest>()

                            for (sig in list) {

                                val isComplete = sig.isComplete
                                val isDeclined = sig.isDeclined

                                when (filter) {

                                    "needAction" -> {
                                        if (!isComplete && !isDeclined && sig.signatures != null) {
                                            val signers = sig.signatures?.toMutableList() ?: mutableListOf()

                                            var needsAction = false
                                            for (signer in signers) {
                                                if (signer.signerEmailAddress == App.instance.preferences.getString("Email", "") && signer.statusCode == "awaiting_signature") {
                                                    needsAction = true
                                                    break
                                                }
                                            }
                                            if (needsAction) {
                                                finalList.add(sig)
                                            }
                                        }
                                    }

                                    "waitingOther" -> {
                                        if (!isComplete && !isDeclined && sig.signatures != null) {
                                            val signers = sig.signatures?.toMutableList() ?: mutableListOf()

                                            var needsAction = false
                                            for (signer in signers) {
                                                if (signer.signerEmailAddress == App.instance.preferences.getString("Email", "") && signer.statusCode == "awaiting_signature") {
                                                    needsAction = true
                                                    break
                                                }
                                            }
                                            if (!needsAction) {
                                                finalList.add(sig)
                                            }
                                        }
                                    }

                                    "completed" -> {
                                        if (isComplete) finalList.add(sig)
                                    }

                                    "canceled" -> {
                                        if (isDeclined) finalList.add(sig)
                                    }

                                }


                            }

                            recyclerView.visibility = View.VISIBLE
                            binding!!.empty.visibility = View.GONE
                            requests.addAll(finalList)
                            requestsAdapter.notifyDataSetChanged()

                        }
                        else {
                            recyclerView.visibility = View.GONE
                            binding!!.empty.visibility = View.VISIBLE
                        }

                    }
                    else {
                        recyclerView.visibility = View.GONE
                        binding!!.empty.visibility = View.VISIBLE
                    }

                }

            }

        },1, 100, "")

        if (requests.isEmpty()) {
            recyclerView.visibility = View.GONE
            binding!!.empty.visibility = View.VISIBLE
        }
        else {
            recyclerView.visibility = View.VISIBLE
            binding!!.empty.visibility = View.GONE
        }

        requestsAdapter.notifyDataSetChanged()

    }

    private fun setSwipeRefresh(filter: String) {

        swipeRefresh.setOnRefreshListener {

            addRequestsToList(filter)
            swipeRefresh.isRefreshing = false

        }

    }

    companion object {
        fun newInstance(value: Int) =
            DocumentFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TYPE, value)
                }
            }

    }

}