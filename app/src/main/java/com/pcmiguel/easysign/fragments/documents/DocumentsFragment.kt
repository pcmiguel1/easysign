package com.pcmiguel.easysign.fragments.documents

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.databinding.FragmentDocumentsBinding
import com.pcmiguel.easysign.fragments.documents.adapter.ViewPagerAdapter
import java.io.File

class DocumentsFragment : Fragment() {

    private var binding: FragmentDocumentsBinding? = null

    var tabPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentDocumentsBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.VISIBLE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.VISIBLE

        if (arguments != null && requireArguments().containsKey("tabPosition")) {
            tabPosition = arguments?.getInt("tabPosition") ?: 0
        }

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding!!.tabLayout
        val viewPager2 = binding!!.viewPager2

        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle)

        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {

                0 -> {
                    tab.text = "Need action"
                }

                1 -> {
                    tab.text = "Waiting for other"
                }

                2 -> {
                    tab.text = "Completed"
                }

                3 -> {
                    tab.text = "Rejected"
                }

            }
        }.attach()

        if (tabPosition != 0)
            adapter.selectTab(viewPager2, tabPosition, smoothScroll = false)


    }

}