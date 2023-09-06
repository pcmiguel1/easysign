package com.pcmiguel.easysign.fragments.addrecipient.model

data class Recipient(
    val name: String,
    val email: String,
    val role: String,
    val me: Boolean
)