package com.pcmiguel.easysign.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


    }

    private fun addRequestsToList() {

        requests.clear()

        recyclerViewRequests.visibility = View.VISIBLE

        requests.add(ApiInterface.Requests())
        requests.add(ApiInterface.Requests())
        requests.add(ApiInterface.Requests())

        requestsAdapter.notifyDataSetChanged()


    }

}