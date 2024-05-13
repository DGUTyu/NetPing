package com.example.net.entity

import java.io.Serializable

data class StatisticsEntity(
    val sent: String,
    val receive: String
) : Serializable
