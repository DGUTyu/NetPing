package com.example.net.entity

import java.io.Serializable


data class PingEntity(
    var sendPackage: String = "",
    var receivePackage: String = "",
    var lostRate: String = "",
    var minRtt: String = "",
    var maxRtt: String = "",
    var aveRtt: String = ""
) : Serializable {


    fun copy(e: PingEntity) {
        sendPackage = e.sendPackage
        receivePackage = e.receivePackage
        lostRate = e.lostRate
        minRtt = e.minRtt
        maxRtt = e.maxRtt
        aveRtt = e.aveRtt
    }


    fun dnsAnalysisFailed() {
        sendPackage = ""
        receivePackage = ""
        lostRate = ""
        minRtt = ""
        maxRtt = ""
        aveRtt = ""
    }


    fun error() {
        sendPackage = "error"
        receivePackage = "error"
        lostRate = "error"
        minRtt = "error"
        maxRtt = "error"
        aveRtt = "error"
    }


    fun notReachable(ip: String) {
        sendPackage = "$ip is not reachable"
        receivePackage = "$ip is not reachable"
        lostRate = "$ip is not reachable"
        minRtt = "$ip is not reachable"
        maxRtt = "$ip is not reachable"
        aveRtt = "$ip is not reachable"
    }


    private val tvSendPackage ="发送包"
    private val tvReceivePackage ="接收包"
    private val tvLossRate = "丢包率"
    private val tvMinRtt ="最小RTT"
    private val tvMaxRtt="最大RTT"
    private val tvAvgRtt ="平均RTT"

    fun display() =
        "$tvSendPackage: $sendPackage\n$tvReceivePackage: $receivePackage\n$tvLossRate: $lostRate\n$tvMinRtt: $minRtt\n$tvMaxRtt: $maxRtt\n$tvAvgRtt: $aveRtt"

    fun displayRate() =
        "$tvSendPackage: $sendPackage\n$tvReceivePackage: $receivePackage\n$tvLossRate: $lostRate\n"


    fun displayStatistics() = "$tvMinRtt: $minRtt ms\n$tvMaxRtt: $maxRtt ms\n$tvAvgRtt: $aveRtt ms"
}
