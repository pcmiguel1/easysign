package com.pcmiguel.easysign.fragments.onboarding

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dropbox.core.android.Auth
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.BuildConfig
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.databinding.FragmentOnBoardingBinding

class OnBoardingFragment : Fragment() {

    private var binding: FragmentOnBoardingBinding? = null

    private var titleList = mutableListOf<String>()
    private var descList = mutableListOf<String>()
    private var imagesList = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        postToList()

        binding!!.viewPager2.adapter = ViewPagerAdapter(titleList, descList, imagesList)
        binding!!.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        val indicator = binding!!.indicator
        indicator.setViewPager(binding!!.viewPager2)


        binding!!.signupBtn.setOnClickListener {
            //findNavController().navigate(R.id.action_onBoardingFragment_to_registerFragment)
        }

        binding!!.loginBtn.setOnClickListener {
            //findNavController().navigate(R.id.action_onBoardingFragment_to_loginFragment)
            findNavController().apply {
                popBackStack(R.id.onBoardingFragment, true)
                navigate(R.id.homeFragment2)
            }
        }

        binding!!.signinDropboxBtn.setOnClickListener {

            Auth.startOAuth2Authentication(requireContext(), BuildConfig.APP_KEY_DROPBOX)

        }

    }

    private fun addToList(title: String, description: String, image: Int) {
        titleList.add(title)
        descList.add(description)
        imagesList.add(image)
    }

    private fun postToList() {
        titleList.clear()
        descList.clear()
        imagesList.clear()
        addToList(getString(R.string.onboarding_1_title), getString(R.string.onboarding_1_desc), R.drawable.onboarding4)
        addToList(getString(R.string.onboarding_2_title), getString(R.string.onboarding_2_desc), R.drawable.onboarding5)
        addToList(getString(R.string.onboarding_3_title), getString(R.string.onboarding_3_desc), R.drawable.onboarding7)
        addToList(getString(R.string.onboarding_4_title), getString(R.string.onboarding_4_desc), R.drawable.onboarding8)
    }

    override fun onResume() {
        super.onResume()

        val accessToken = Auth.getOAuth2Token()
        if (accessToken != null) {
            Log.d("token", accessToken.toString())
            // User has successfully authenticated
            findNavController().apply {
                popBackStack(R.id.onBoardingFragment, true)
                navigate(R.id.homeFragment2)
            }
        }
    }

}