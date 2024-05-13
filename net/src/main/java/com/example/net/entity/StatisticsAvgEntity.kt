package com.example.net.entity

import java.io.Serializable

data class StatisticsAvgEntity(
    val max: String,
    val min: String,
    val avg: String
) : Serializable
