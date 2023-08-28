package com.pcmiguel.easysign

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.multidex.MultiDexApplication
import com.pcmiguel.easysign.services.BackOffice
import com.pcmiguel.easysign.services.Backend

class App : MultiDexApplication(), DefaultLifecycleObserver {

    private lateinit var requests: Backend
    lateinit var preferences: SharedPreferences
    var mainActivity: MainActivity? = null

    val backOffice: BackOffice
        get() = requests.backOffice

    override fun onCreate() {
        super<MultiDexApplication>.onCreate()
        instance = this
        preferences = this.getSharedPreferences("easysign", Context.MODE_PRIVATE)

        initBackend()

    }

    fun initBackend() {
        requests = Backend(this, preferences)
    }

    companion object {
        @get:Synchronized
        lateinit var instance: App
    }

}