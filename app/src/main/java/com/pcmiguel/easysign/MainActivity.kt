package com.pcmiguel.easysign

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.WindowManager.LayoutParams
import android.widget.EditText
import android.widget.Spinner
import androidx.core.content.FileProvider
import com.blongho.country_data.World
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.services.ApiAIInterface
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val TIME_INTERVAL = 2000 // # milliseconds, desired time passed between two back presses.
    private var mBackPressed: Long = 0

    private var isExpanded = false

    private val fromBottomFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_fab)
    }

    private val toBottomFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_fab)
    }

    private val rotateClockWiseFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_clock_wise)
    }

    private val rotateAntiClockWiseFabAnim : Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_anti_clock_wise)
    }

    private val fromBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim)
    }
    private val toBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim)
    }

    private lateinit var navController: NavController

    private lateinit var plusBtn : FloatingActionButton
    private lateinit var signBtn : FloatingActionButton
    private lateinit var sendBtn : FloatingActionButton
    private lateinit var aiBtn : FloatingActionButton
    private lateinit var signText : TextView
    private lateinit var sendText : TextView
    private lateinit var aiText : TextView

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.instance.mainActivity = this

        World.init(applicationContext)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)

        plusBtn = findViewById(R.id.plus_btn)
        signBtn = findViewById(R.id.sign)
        sendBtn = findViewById(R.id.send)
        aiBtn = findViewById(R.id.ai)
        signText = findViewById(R.id.sign_text)
        sendText = findViewById(R.id.send_text)
        aiText = findViewById(R.id.ai_text)


        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment2)
                }

                R.id.documentsFragment -> {
                    navController.navigate(R.id.documentsFragment2)
                }

                R.id.settingsFragment -> {

                    val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_settings, null)
                    val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)
                    dialog.setContentView(view)
                    dialog.setCancelable(true)

                    dialog.behavior.peekHeight = 750

                    dialog.show()

                }

            }

            true

        }

        navController.addOnDestinationChangedListener {_, destination, _ ->

            when (destination.id) {

                R.id.homeFragment2 -> {
                    bottomNavigationView.menu.findItem(R.id.homeFragment).isChecked = true
                }
                R.id.documentsFragment2 -> {
                    bottomNavigationView.menu.findItem(R.id.documentsFragment).isChecked = true
                }

            }
        }


        plusBtn.setOnClickListener {

            if (isExpanded) {
                shrinkFab()
            } else {
                expandFab()
            }

        }

        signBtn.setOnClickListener {
            optionsDocument()
        }

        sendBtn.setOnClickListener {
            optionsDocument()
        }

        aiBtn.setOnClickListener {
            optionsAI()
        }


    }

    private fun shrinkFab() {

        plusBtn.startAnimation(rotateAntiClockWiseFabAnim)
        sendBtn.startAnimation(toBottomFabAnim)
        signBtn.startAnimation(toBottomFabAnim)
        aiBtn.startAnimation(toBottomFabAnim)
        sendText.startAnimation(toBottomFabAnim)
        signText.startAnimation(toBottomFabAnim)
        aiText.startAnimation(toBottomFabAnim)

        sendBtn.isClickable = false
        signBtn.isClickable = false
        aiBtn.isClickable = false

        isExpanded = !isExpanded

    }

    private fun expandFab() {

        plusBtn.startAnimation(rotateClockWiseFabAnim)
        sendBtn.startAnimation(fromBottomFabAnim)
        signBtn.startAnimation(fromBottomFabAnim)
        aiBtn.startAnimation(fromBottomFabAnim)
        sendText.startAnimation(fromBottomFabAnim)
        signText.startAnimation(fromBottomFabAnim)
        aiText.startAnimation(fromBottomFabAnim)

        sendBtn.isClickable = true
        signBtn.isClickable = true
        aiBtn.isClickable = true

        isExpanded = !isExpanded

    }

    private fun optionsDocument() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_options_documents, null)

        val builder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()
        shrinkFab()


    }

    private fun optionsAI() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_options_ai, null)

        val builder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val generateBtn = mDialogView.findViewById<View>(R.id.generateBtn)
        val translationBtn = mDialogView.findViewById<View>(R.id.translationBtn)
        val legalBtn = mDialogView.findViewById<View>(R.id.legalBtn)

        generateBtn.setOnClickListener {

            dialog.dismiss()

            val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_generate_ai, null)
            val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

            dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            dialog.setContentView(view)
            dialog.setCancelable(true)

            val loadingBtn = dialog.findViewById<View>(R.id.loading)
            val message = dialog.findViewById<EditText>(R.id.message)
            val generate = dialog.findViewById<View>(R.id.generateBtn)

            generate!!.setOnClickListener {

                if (message!!.text.toString().isNotEmpty()) {

                    generate.visibility = View.GONE
                    loadingBtn!!.visibility = View.VISIBLE

                    val json = JsonObject()
                    json.addProperty("model", "gpt-3.5-turbo")

                    val messages = JsonArray()
                    val msg = JsonObject()
                    msg.addProperty("role", "user")
                    msg.addProperty("content", message!!.text.toString())
                    messages.add(msg)

                    json.add("messages", messages)

                    message.setText("")

                    App.instance.backOffice.chatAI(object : Listener<Any> {
                        override fun onResponse(response: Any?) {

                            generate.visibility = View.VISIBLE
                            loadingBtn.visibility = View.GONE

                            if (response != null && response is ApiAIInterface.ChatAI) {

                                val choices = response.choices
                                val text = choices!![0].message!!.content.toString()

                                val pdfFile = createPdfFromString(text)
                                if (pdfFile != null) {
                                    // Open the PDF file
                                    openPdf(pdfFile)
                                }

                            }

                        }

                    }, json)

                }

            }

            dialog.show()

        }

        translationBtn.setOnClickListener {

            val languages = ArrayList<Pair<Int, String>>()
            val countries = World.getAllCountries()

            languages.clear()

            for (country in countries) {

                val language = World.getLanguagesFrom(country.name)[0]
                if (language != null && language != "")
                    languages.add(Pair(country.flagResource, language))

            }

            dialog.dismiss()

            val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_translation_ai, null)
            val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

            dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            dialog.setContentView(view)
            dialog.setCancelable(true)

            val languagesSpinner = dialog.findViewById<Spinner>(R.id.languages)

            val adapter = LanguageAdapter(this, languages)

            languagesSpinner!!.adapter = adapter

            dialog.show()

        }

        legalBtn.setOnClickListener {

            val countries = World.getAllCountries()

            dialog.dismiss()

            val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_legal_ai, null)
            val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

            dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            dialog.setContentView(view)
            dialog.setCancelable(true)

            val countriesSpinner = dialog.findViewById<Spinner>(R.id.countries)

            val adapter = CountryAdapter(this, countries)

            countriesSpinner!!.adapter = adapter

            dialog.show()

        }

        dialog.show()
        shrinkFab()


    }

    override fun onBackPressed() {

        Utils.hideKeyboard(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = navHostFragment?.findNavController()

        val currentFragment = navController?.currentDestination?.id

        if (currentFragment == R.id.homeFragment2 || currentFragment == R.id.documentsFragment2) {

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
                super.onBackPressed()
            else {
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
                mBackPressed = System.currentTimeMillis()
            }

        }
        else navController!!.popBackStack()

    }

    private fun createPdfFromString(text: String): File? {
        try {
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            storageDir?.mkdirs()

            val pdfFile = File(storageDir, "sample.pdf")

            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            document.add(Paragraph(text))

            document.close()
            pdfWriter.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun openPdf(pdfFile: File) {
        val pdfUri = FileProvider.getUriForFile(this, "com.pcmiguel.easysign.provider", pdfFile)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

}