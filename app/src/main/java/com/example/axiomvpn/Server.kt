package com.example.axiomvpn

data class Server(
    val id: Int,
    val flagEmoji: String,
    val country: String,
    val ip: String,
    val ping: Int,
    val load: Int,
)