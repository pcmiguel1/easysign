package com.pcmiguel.easysign.fragments.onboarding

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.google.gson.JsonObject
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.BuildConfig
import com.pcmiguel.easysign.LoadingActivity
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.Utils.openActivity
import com.pcmiguel.easysign.databinding.FragmentOnBoardingBinding
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.services.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class OnBoardingFragment : Fragment() {

    private var binding: FragmentOnBoardingBinding? = null

    private var titleList = mutableListOf<String>()
    private var descList = mutableListOf<String>()
    private var imagesList = mutableListOf<Int>()

    private lateinit var loadingDialog: LoadingDialog
    private var login = false

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

        loadingDialog = LoadingDialog(requireContext())

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
            login = true
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

        if (accessToken != null && login) {

            loadingDialog.startLoading()

            // Execute fetchUserEmail in a background thread
            GlobalScope.launch(Dispatchers.IO) {
                fetchUserEmail(accessToken)
            }

        }
    }

    private suspend fun fetchUserEmail(accessToken: String) {

        withContext(Dispatchers.IO) {

            val client = DbxClientV2(DbxRequestConfig.newBuilder("easysignapp").build(), accessToken)

            try {

                val accountInfo = client.users().currentAccount

                val userId = accountInfo.accountId
                val userEmail = accountInfo.email
                val userName = accountInfo.name.displayName
                val profilePicUrl = accountInfo.profilePhotoUrl

                // Switch to the main thread for UI-related operations
                withContext(Dispatchers.Main) {

                    val json = JsonObject()
                    json.addProperty("email_address", userEmail)

                    App.instance.backOffice.createAccount(object : Listener<Any> {
                        override fun onResponse(response: Any?) {

                            if (isAdded) {

                                if (response != null) {

                                    if (response is ApiInterface.AccountResponse) { // se der ok, quer dizer que criou conta e vai para Home Screen

                                        loadingDialog.isDismiss()

                                        App.instance.preferences.edit().putString("UserId", userId).apply()
                                        App.instance.preferences.edit().putString("AccessToken", accessToken).apply()
                                        App.instance.preferences.edit().putString("Email", userEmail).apply()
                                        App.instance.preferences.edit().putString("Name", userName).apply()
                                        App.instance.preferences.edit().putString("ProfilePhotoUrl", profilePicUrl).apply()

                                        App.instance.preferences.edit().putString("AccountId", response.account!!.accountId).apply()

                                        // User has successfully authenticated
                                        findNavController().apply {
                                            popBackStack(R.id.onBoardingFragment, true)
                                            navigate(R.id.homeFragment2)
                                        }

                                    }
                                    else if (response is String) {

                                        when (response) {

                                            "Account already exists." -> { // se a conta já estiver criada, vai chamar o serviço do get account para saber o accountId e depois vai para a Home Screen

                                                App.instance.backOffice.getAccount(object : Listener<Any> {
                                                    override fun onResponse(response: Any?) {

                                                        loadingDialog.isDismiss()

                                                        if (isAdded) {

                                                            if (response != null && response is ApiInterface.AccountResponse) {

                                                                App.instance.preferences.edit().putString("UserId", userId).apply()
                                                                App.instance.preferences.edit().putString("AccessToken", accessToken).apply()
                                                                App.instance.preferences.edit().putString("Email", userEmail).apply()
                                                                App.instance.preferences.edit().putString("Name", userName).apply()
                                                                App.instance.preferences.edit().putString("ProfilePhotoUrl", profilePicUrl).apply()

                                                                App.instance.preferences.edit().putString("AccountId", response.account!!.accountId).apply()

                                                                // User has successfully authenticated
                                                                findNavController().apply {
                                                                    popBackStack(R.id.onBoardingFragment, true)
                                                                    navigate(R.id.homeFragment2)
                                                                }

                                                            }
                                                            else if (response != null && response is String) {

                                                                val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_error_message, null)

                                                                val builder = AlertDialog.Builder(requireContext())
                                                                    .setView(mDialogView)
                                                                    .setCancelable(false)

                                                                val dialog = builder.create()
                                                                dialog.window?.setBackgroundDrawable(
                                                                    ColorDrawable(
                                                                        Color.TRANSPARENT)
                                                                )

                                                                val errorMessage = mDialogView.findViewById<TextView>(R.id.message)
                                                                val okBtn = mDialogView.findViewById<View>(R.id.okBtn)

                                                                errorMessage.text = response

                                                                okBtn.setOnClickListener {
                                                                    dialog.dismiss()
                                                                }

                                                                dialog.show()

                                                            }

                                                        }

                                                    }

                                                }, userEmail)

                                            }

                                            "Please confirm your email to continue with sign up." -> { //se já estiver criada mas ainda não foi confirmada, vai surgir um popup de aviso

                                                loadingDialog.isDismiss()

                                                val mDialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_error_message, null)

                                                val builder = AlertDialog.Builder(requireContext())
                                                    .setView(mDialogView)
                                                    .setCancelable(false)

                                                val dialog = builder.create()
                                                dialog.window?.setBackgroundDrawable(
                                                    ColorDrawable(
                                                        Color.TRANSPARENT)
                                                )

                                                val errorMessage = mDialogView.findViewById<TextView>(R.id.message)
                                                val okBtn = mDialogView.findViewById<View>(R.id.okBtn)

                                                errorMessage.text = response

                                                okBtn.setOnClickListener {
                                                    dialog.dismiss()
                                                }

                                                dialog.show()

                                            }

                                        }

                                    }

                                }

                            }

                        }

                    }, json)

                }

            } catch (e: DbxException) {
                e.printStackTrace()
            }

        }

    }

}