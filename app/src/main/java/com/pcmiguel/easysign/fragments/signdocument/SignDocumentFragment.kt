package com.pcmiguel.easysign.fragments.signdocument

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.pcmiguel.easysign.App
import com.pcmiguel.easysign.R
import com.pcmiguel.easysign.databinding.FragmentSignDocumentBinding

class SignDocumentFragment : Fragment() {

    private var binding: FragmentSignDocumentBinding? = null

    private var signUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentSignDocumentBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        App.instance.mainActivity!!.findViewById<View>(R.id.bottombar).visibility = View.GONE
        App.instance.mainActivity!!.findViewById<View>(R.id.plus_btn).visibility = View.GONE

        if (arguments != null && requireArguments().containsKey("signUrl")) {

            signUrl = arguments?.getString("signUrl") ?: ""

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

        // Add a JavaScript interface to the WebView
        webView.addJavascriptInterface(JavaScriptInterface(), "Android")

        val url = "192.168.1.72:3000/sign?sign_url=${signUrl}"
        webView.loadUrl(url)

    }

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun onSubmit() {

            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "The document has been signed!", Toast.LENGTH_SHORT).show()

                val navController = findNavController()

                navController.popBackStack(navController.graph.startDestinationId, false)

                // Navigate to a new fragment
                navController.navigate(R.id.homeFragment2)
            }

        }

        @JavascriptInterface
        fun onCancel() {

            activity?.runOnUiThread {
                val navController = findNavController()

                navController.popBackStack(navController.graph.startDestinationId, false)

                // Navigate to a new fragment
                navController.navigate(R.id.homeFragment2)
            }

        }
    }

}