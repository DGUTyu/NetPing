package com.example.net.entity

import java.io.Serializable

data class IcmpSeqEntity(
    val bytes: String,
    val seq: String,
    val time: String
) : Serializable {
    fun display() = "$bytes bytes: icmp_seq=$seq time=$time ms\n"
}
