package com.pcmiguel.easysign

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

}