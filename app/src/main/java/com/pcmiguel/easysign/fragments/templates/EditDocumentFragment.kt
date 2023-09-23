package com.pcmiguel.easysign.fragments.templates

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.databinding.FragmentEditDocumentBinding

class EditDocumentFragment : Fragment() {

    private var binding: FragmentEditDocumentBinding? = null

    private var editUrl = ""
    private var isRedirected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentEditDocumentBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        if (arguments != null && requireArguments().containsKey("editUrl")) {

            editUrl = arguments?.getString("editUrl") ?: ""

        }

        return fragmentBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = binding!!.webview

        webView.settings.javaScriptEnabled = true

        // Adjust viewport settings
        val webSettings = webView.settings
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        // Enable zoom controls (optional)
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        //webView.webChromeClient = WebChromeClient()

        // Add a JavaScript interface to the WebView
        webView.addJavascriptInterface(JavaScriptInterface(), "Android")

        // Load your web page in the WebView
        webView.loadUrl("https://www.example.com") // Replace with your form URL

        val url = "192.168.1.72:3000/template/edit?edit_url=${editUrl}"
        webView.loadUrl(url)

    }

    private var originalOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    override fun onResume() {
        super.onResume()

        // Store the original screen orientation
        originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // Set the screen orientation to landscape when the fragment is active
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onPause() {
        super.onPause()

        // Reset the screen orientation to the original value when the fragment is paused
        activity?.requestedOrientation = originalOrientation
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Reset the screen orientation to the original value when the fragment is removed
        activity?.requestedOrientation = originalOrientation
    }

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun onSubmit() {

            Toast.makeText(requireContext(), "Template created!", Toast.LENGTH_SHORT).show()

            val navController = findNavController()

            navController.popBackStack(navController.graph.startDestinationId, false)

            // Navigate to a new fragment
            navController.navigate(R.id.action_editDocumentFragment_to_templatesFragment)
        }

        @JavascriptInterface
        fun onCancel() {

            val navController = findNavController()

            navController.popBackStack(navController.graph.startDestinationId, false)

            // Navigate to a new fragment
            activity!!.onBackPressed()
        }
    }

}