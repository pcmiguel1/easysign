package com.pcmiguel.easysign.fragments.addrecipient.model

data class Recipient(
    var name: String,
    var email: String,
    var role: String,
    var me: Boolean
)