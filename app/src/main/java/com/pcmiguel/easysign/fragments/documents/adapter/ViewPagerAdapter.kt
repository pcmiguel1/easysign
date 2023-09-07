package com.pcmiguel.easysign.fragments.documents.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.pcmiguel.easysign.fragments.documents.DocumentFragment

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
       return when(position) {

            0 -> {
                DocumentFragment.newInstance(0)
            }

            1 -> {
                DocumentFragment.newInstance(1)
            }

           2 -> {
               DocumentFragment.newInstance(2)
           }

           3 -> {
               DocumentFragment.newInstance(3)
           }

            else -> {
                Fragment()
            }

        }
    }

    // Add a method to select a specific tab programmatically
    fun selectTab(viewPager2: ViewPager2, tabIndex: Int, smoothScroll: Boolean = true) {
        if (tabIndex in 0 until itemCount) {
            // Set the selected tab
            viewPager2.setCurrentItem(tabIndex, smoothScroll)
        }
    }


}