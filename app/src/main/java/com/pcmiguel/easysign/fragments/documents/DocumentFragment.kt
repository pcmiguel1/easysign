package com.pcmiguel.easysign.fragments.documents

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.databinding.FragmentDocumentBinding
import com.pcmiguel.easysign.fragments.home.adapter.RequestsAdapter
import com.pcmiguel.easysign.services.ApiInterface

private const val ARG_TYPE = "ARG_TYPE"

class DocumentFragment : Fragment() {

    private var binding: FragmentDocumentBinding? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var requests: MutableList<ApiInterface.Requests> = mutableListOf()
    private lateinit var requestsAdapter: RequestsAdapter

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

        swipeRefresh = binding!!.swipeRefresh

        recyclerView = binding!!.documents
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        requestsAdapter = RequestsAdapter(requests)
        recyclerView.adapter = requestsAdapter

        requestsAdapter.setOnItemClickListener(object : RequestsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val item = requestsAdapter.getItem(position)

            }

        })

        when (type) {

            0 -> {
                binding!!.emptyText.text = "Documents for you to sign will be displayed here."
            }

            1 -> {
                binding!!.emptyText.text = "Documents waiting for others to complete signing will be displayed here."
            }

            2 -> {
                binding!!.emptyText.text = "No completed documents yet."
            }

            3 -> {
                binding!!.emptyText.text = "No canceled documents yet."
            }

        }

        addRequestsToList()
        setSwipeRefresh()

    }

    private fun addRequestsToList() {

        requests.clear()

        requests.add(ApiInterface.Requests())
        requests.add(ApiInterface.Requests())
        requests.add(ApiInterface.Requests())

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

    private fun setSwipeRefresh() {

        swipeRefresh.setOnRefreshListener {

            addRequestsToList()
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