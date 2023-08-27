package com.pcmiguel.easysign.services

import android.content.Context
import android.content.SharedPreferences

class Backend(
    val context: Context,
    val preferences: SharedPreferences
) {

    val backOffice: BackOffice = BackOffice(this)

}