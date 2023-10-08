package com.pcmiguel.easysign

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.view.WindowManager.LayoutParams
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.blongho.country_data.World
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.pawcare.pawcare.services.Listener
import com.pcmiguel.easysign.Utils.openActivity
import com.pcmiguel.easysign.libraries.LoadingDialog
import com.pcmiguel.easysign.libraries.scanner.activity.ScanActivity
import com.pcmiguel.easysign.libraries.scanner.constants.ScanConstants
import com.pcmiguel.easysign.libraries.scanner.util.ScanUtils
import com.pcmiguel.easysign.services.ApiAIInterface
import com.pcmiguel.easysign.services.ApiInterface
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val TIME_INTERVAL = 2000 // # milliseconds, desired time passed between two back presses.
    private var mBackPressed: Long = 0

    private var isExpanded = false
    private var fileUploaded = false
    private var extractedText = ""

    private var noRecipients = false

    private lateinit var loadingDialog: LoadingDialog

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

    private lateinit var uploadImg : ImageView
    private lateinit var uploadText: TextView

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        App.instance.mainActivity = this

        loadingDialog = LoadingDialog(this)

        World.init(applicationContext)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)

        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav)

        plusBtn = findViewById(R.id.plus_btn)
        signBtn = findViewById(R.id.sign)
        sendBtn = findViewById(R.id.send)
        aiBtn = findViewById(R.id.ai)
        signText = findViewById(R.id.sign_text)
        sendText = findViewById(R.id.send_text)
        aiText = findViewById(R.id.ai_text)

        if (App.instance.preferences.getString("UserId", "") != "") {
            graph.setStartDestination(R.id.homeFragment2)
        }
        else graph.setStartDestination(R.id.onBoardingFragment)

        navController.graph = graph

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

                    val nameText = dialog.findViewById<TextView>(R.id.name)
                    val emailText = dialog.findViewById<TextView>(R.id.email)
                    val profilePic = dialog.findViewById<ImageView>(R.id.image)

                    val templatesBtn = dialog.findViewById<View>(R.id.templatesBtn)
                    val logoutBtn = dialog.findViewById<View>(R.id.logoutBtn)

                    nameText!!.text = App.instance.preferences.getString("Name", "")
                    emailText!!.text = App.instance.preferences.getString("Email", "")

                    val photoUrl = App.instance.preferences.getString("ProfilePhotoUrl", "")

                    if (photoUrl != "") {

                        Picasso.get()
                            .load(photoUrl)
                            .placeholder(R.drawable.profile_template)
                            .error(R.drawable.profile_template)
                            .into(profilePic, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {

                                }

                                override fun onError(e: java.lang.Exception?) {

                                }

                            })

                    }

                    templatesBtn!!.setOnClickListener {

                        dialog.dismiss()

                        navController.navigate(R.id.templatesFragment)

                    }

                    logoutBtn!!.setOnClickListener {

                        dialog.dismiss()

                        App.instance.preferences.edit().clear().apply()

                        val accessToken = Auth.getOAuth2Token()

                        if (accessToken != null) {

                            GlobalScope.launch(Dispatchers.IO) {

                                val client = DbxClientV2(DbxRequestConfig.newBuilder("easysignapp").build(), accessToken)

                                try {
                                    // Revoke the access token
                                    client.auth().tokenRevoke()

                                    openActivity(LoadingActivity::class.java)

                                } catch (e: DbxException) {
                                    e.printStackTrace()
                                }

                            }

                        }
                        else {
                            openActivity(LoadingActivity::class.java)
                        }

                    }

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
            optionsDocument(true)
        }

        sendBtn.setOnClickListener {
            optionsDocument(false)
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

    private fun optionsDocument(yourself : Boolean) {

        noRecipients = yourself

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_options_documents, null)

        val builder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val templateButton = mDialogView.findViewById<View>(R.id.templateBtn)
        val scanButton = mDialogView.findViewById<View>(R.id.scanBtn)
        val photoButton = mDialogView.findViewById<View>(R.id.photoBtn)
        val browseButton = mDialogView.findViewById<View>(R.id.browseBtn)

        templateButton.setOnClickListener {

            dialog.dismiss()

            //Check if you have created templates
            loadingDialog.startLoading()

            App.instance.backOffice.listTemplates(object : Listener<Any> {
                override fun onResponse(response: Any?) {

                    loadingDialog.isDismiss()

                    if (response != null && response is ApiInterface.TemplateRequests) {

                        val list = response.templates

                        if (list!!.isNotEmpty()) {

                            val bundle = Bundle().apply {
                                putBoolean("selectTemplate", true)
                            }
                            navController.navigate(R.id.templatesFragment, bundle)

                        }
                        else {
                            noTemplatesDialog()
                        }

                    }
                    else {
                        noTemplatesDialog()
                    }

                }
            }, 1, 100, "")

        }

        scanButton.setOnClickListener {

            dialog.dismiss()

            val intent = Intent(this, ScanActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)

        }

        photoButton.setOnClickListener {

            dialog.dismiss()

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_GALLERY)

        }

        browseButton.setOnClickListener {

            dialog.dismiss()

            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf" // Limit the selection to PDF files
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, FILE_PICKER2_EXTRACT_REQUEST_CODE)

        }

        dialog.show()
        shrinkFab()


    }

    private fun noTemplatesDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_no_templates, null)

        val builder = AlertDialog.Builder(this)
            .setView(mDialogView)
            .setCancelable(true)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val addTemplate = mDialogView.findViewById<View>(R.id.addTemplate)

        addTemplate.setOnClickListener {

            dialog.dismiss()
            navController.navigate(R.id.templatesFragment)

        }

        dialog.show()

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

            var languageSelected = ""

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

            val uploadBtn = dialog.findViewById<View>(R.id.uploadBtn)
            val translateBtn = dialog.findViewById<View>(R.id.translateBtn)
            val loadingBtn = dialog.findViewById<View>(R.id.loading)
            uploadImg = dialog.findViewById<ImageView>(R.id.upload_img)!!
            uploadText = dialog.findViewById<TextView>(R.id.upload_text)!!
            val languagesSpinner = dialog.findViewById<Spinner>(R.id.languages)

            val adapter = LanguageAdapter(this, languages)

            languagesSpinner!!.adapter = adapter

            languagesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    languageSelected = languages[position].second

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }

            uploadBtn!!.setOnClickListener {

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf" // Limit the selection to PDF files
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, FILE_PICKER_EXTRACT_REQUEST_CODE)

            }

            translateBtn!!.setOnClickListener {

                if (fileUploaded) {

                    translateBtn.visibility = View.GONE
                    loadingBtn!!.visibility = View.VISIBLE

                    val json = JsonObject()
                    json.addProperty("model", "gpt-3.5-turbo")

                    val messages = JsonArray()
                    val msg = JsonObject()
                    msg.addProperty("role", "user")
                    msg.addProperty("content", "$extractedText translate to $languageSelected")
                    messages.add(msg)

                    json.add("messages", messages)

                    App.instance.backOffice.chatAI(object : Listener<Any> {
                        override fun onResponse(response: Any?) {

                            translateBtn.visibility = View.VISIBLE
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
                else
                    Toast.makeText(this, "No selected file", Toast.LENGTH_SHORT).show()

            }

            dialog.show()

        }

        legalBtn.setOnClickListener {

            val countries = World.getAllCountries()

            var countrySelected = ""

            dialog.dismiss()

            val view : View = layoutInflater.inflate(R.layout.item_bottom_sheet_legal_ai, null)
            val dialog = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme)

            dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

            dialog.setContentView(view)
            dialog.setCancelable(true)

            val uploadBtn = dialog.findViewById<View>(R.id.uploadBtn)
            val checkBtn = dialog.findViewById<View>(R.id.checkBtn)
            val loadingBtn = dialog.findViewById<View>(R.id.loading)
            uploadImg = dialog.findViewById<ImageView>(R.id.upload_img)!!
            uploadText = dialog.findViewById<TextView>(R.id.upload_text)!!
            val countriesSpinner = dialog.findViewById<Spinner>(R.id.countries)
            val improvementsCard = dialog.findViewById<View>(R.id.improvements_card)
            val improvementsText = dialog.findViewById<TextView>(R.id.improvements)

            val adapter = CountryAdapter(this, countries)

            countriesSpinner!!.adapter = adapter

            countriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    countrySelected = countries[position].name

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}

            }

            uploadBtn!!.setOnClickListener {

                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf" // Limit the selection to PDF files
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, FILE_PICKER_LEGAL_REQUEST_CODE)

            }

            checkBtn!!.setOnClickListener {

                if (fileUploaded) {

                    checkBtn.visibility = View.GONE
                    loadingBtn!!.visibility = View.VISIBLE

                    val json = JsonObject()
                    json.addProperty("model", "gpt-3.5-turbo")

                    val messages = JsonArray()
                    val msg = JsonObject()
                    msg.addProperty("role", "user")
                    msg.addProperty("content", "$extractedText what I can improve? $countrySelected")
                    messages.add(msg)

                    json.add("messages", messages)

                    App.instance.backOffice.chatAI(object : Listener<Any> {
                        override fun onResponse(response: Any?) {

                            checkBtn.visibility = View.VISIBLE
                            loadingBtn.visibility = View.GONE

                            if (response != null && response is ApiAIInterface.ChatAI) {

                                val choices = response.choices
                                val text = choices!![0].message!!.content.toString()

                                improvementsCard!!.visibility = View.VISIBLE
                                improvementsText!!.text = text

                            }

                        }

                    }, json)

                }
                else
                    Toast.makeText(this, "No selected file", Toast.LENGTH_SHORT).show()

            }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK) {

            val selectedImageUri = data?.data

            if (selectedImageUri != null) {

                val bundle = Bundle().apply {
                    putParcelable("imageUri", selectedImageUri)
                    putBoolean("noRecipients", noRecipients)
                }

                //navController.navigate(R.id.addDocumentsFragment)
                navController.navigate(R.id.createDocumentFragment, bundle)
            }

        }
        else if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                // Check if the selected file is a PDF
                if (Utils.isPdfFile(uri, applicationContext)) {
                    // The selected file is a PDF, you can now proceed with the upload.
                    // uri contains the URI of the selected file.
                    val contentResolver = applicationContext.contentResolver
                    val cursor = contentResolver.query(uri, null, null, null, null)

                    fileUploaded = true

                    uploadImg.visibility = View.GONE

                    if (cursor != null) {
                        val displayNameColumnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                        if (displayNameColumnIndex != -1 && cursor.moveToFirst()) {
                            val displayName = cursor.getString(displayNameColumnIndex)
                            cursor.close()

                            uploadText.text = displayName
                        } else {
                            // Handle the case where DISPLAY_NAME is not available
                            // You can use a default name or display an error message
                            uploadText.text = uri.path
                        }
                    }

                } else {
                    // The selected file is not a PDF. You can display an error message to the user.
                    Toast.makeText(this, "The selected file is not a PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else if (requestCode == FILE_PICKER_EXTRACT_REQUEST_CODE || requestCode == FILE_PICKER_LEGAL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val uri = data?.data
            if (uri != null) {
                // Check if the selected file is a PDF
                if (Utils.isPdfFile(uri, applicationContext)) {
                    // The selected file is a PDF, you can now proceed with the upload.
                    // uri contains the URI of the selected file.
                    val contentResolver = applicationContext.contentResolver
                    val cursor = contentResolver.query(uri, null, null, null, null)

                    fileUploaded = true
                    extractedText = Utils.extractTextFromPdf(contentResolver, uri)

                    uploadImg.visibility = View.GONE

                    if (cursor != null) {
                        val displayNameColumnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                        if (displayNameColumnIndex != -1 && cursor.moveToFirst()) {
                            val displayName = cursor.getString(displayNameColumnIndex)
                            cursor.close()

                            uploadText.text = displayName
                        } else {
                            // Handle the case where DISPLAY_NAME is not available
                            // You can use a default name or display an error message
                            uploadText.text = uri.path
                        }
                    }

                } else {
                    // The selected file is not a PDF. You can display an error message to the user.
                    Toast.makeText(this, "The selected file is not a PDF", Toast.LENGTH_SHORT).show()
                }
            }

        }
        else if (requestCode == FILE_PICKER2_EXTRACT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val uri = data?.data
            if (uri != null) {

                if (Utils.isPdfFile(uri, applicationContext)) {

                    val filePdf = Utils.uriToPdfFile(this, uri)

                    val bundle = Bundle().apply {
                        putSerializable("pdfFile", filePdf)
                        putBoolean("noRecipients", noRecipients)
                    }

                    navController.navigate(R.id.addDocumentsFragment, bundle)

                }
                else {
                    // The selected file is not a PDF. You can display an error message to the user.
                    Toast.makeText(this, "The selected file is not a PDF", Toast.LENGTH_SHORT).show()
                }

            }

        }
        else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data != null && data.extras != null) {

                val filePath = data.getStringExtra(ScanConstants.SCANNED_RESULT)
                val baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME)
                val uri = Utils.bitmapToUri(this, baseBitmap)
                if (uri != null) {

                    val bundle = Bundle().apply {
                        putParcelable("imageUri", uri)
                        putBoolean("noRecipients", noRecipients)
                    }

                    navController.navigate(R.id.createDocumentFragment, bundle)

                }

            }

        }
    }

    private fun checkPermission() : Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {

            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                }
            }

        }
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 123
        private const val FILE_PICKER_EXTRACT_REQUEST_CODE = 124
        private const val FILE_PICKER2_EXTRACT_REQUEST_CODE = 198
        private const val FILE_PICKER_LEGAL_REQUEST_CODE = 125
        private const val REQUEST_CODE_GALLERY = 334
        private const val REQUEST_CODE = 101
    }

}