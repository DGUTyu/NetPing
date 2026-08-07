package com.example.net.util

import android.content.Context
import com.example.net.entity.IcmpSeqEntity
import com.example.net.entity.StatisticsAvgEntity
import com.example.net.entity.StatisticsEntity
import java.util.regex.Pattern


object KotlinUtils {

    const val TAG = "KotlinUtils"

    const val EXTERNAL_STORAGE_REQUEST_PERMISSION = 6666
    const val EXTERNAL_STORAGE_REQUEST_PERMISSION_PAY = 6667
    const val EXTERNAL_STORAGE_REQUEST_PERMISSION_SETTING = 6668

    const val APP_INSTALL_REQUEST_PERMISSION = 9999
    const val APP_INSTALL_REQUEST_PERMISSION_PAY = 9998
    const val APP_INSTALL_REQUEST_PERMISSION_SETTING = 9997


    @Throws
    fun analysisStatisticsAvg(str: String?): StatisticsAvgEntity? {
        if (str.isNullOrEmpty()) {
            return null
        }
        //rtt min/avg/max/mdev = 7.441/13.580/31.051/6.413 m
        val rttPattern =
            Pattern.compile("""rtt min/avg/max/mdev = ([\d.]+)/([\d.]+)/([\d.]+)/([\d.]+)""")
        val rttMatcher = rttPattern.matcher(str)
        if (!rttMatcher.find()) {
            return null
        }
        val rttMin = rttMatcher.group(1) ?: return null
        val rttAvg = rttMatcher.group(2) ?: return null
        val rttMax = rttMatcher.group(3) ?: return null
        return StatisticsAvgEntity(
            rttMax,
            rttMin,
            rttAvg
        )
    }


    @Throws
    fun analysisStatistics(str: String?): StatisticsEntity? {
        if (str.isNullOrEmpty()) {
            return null
        }
        //10 packets transmitted, 10 received, +27 duplicates, 0% packet loss, time 9013ms
        val packetsPattern = Pattern.compile("""(\d+)\s+packets""")
        val receivedPattern = Pattern.compile("""(\d+)\s+received""")

        val packetsMatcher = packetsPattern.matcher(str)
        val receivedMatcher = receivedPattern.matcher(str)

        if (!packetsMatcher.find() || !receivedMatcher.find()) {
            return null
        }

        val sent = packetsMatcher.group(1) ?: return null
        val received = receivedMatcher.group(1) ?: return null

        return StatisticsEntity(
            sent,
            received
        )
    }

    @Throws
    fun analysisIcmp(str: String?): IcmpSeqEntity? {
        //64 bytes from 119.29.126.90: icmp_seq=1 ttl=52 time=7.96 ms
        if (str.isNullOrEmpty()) {
            return null
        }
        val bytesPattern = Pattern.compile("""(\d+)\s+bytes""")
        val icmpSeqPattern = Pattern.compile("""icmp_seq=(\d+)""")
        val timePattern = Pattern.compile("""time=([\d.]+)\s+ms""")

        val bytesMatcher = bytesPattern.matcher(str)
        val icmpSeqMatcher = icmpSeqPattern.matcher(str)
        val timeMatcher = timePattern.matcher(str)

        if (!bytesMatcher.find() || !icmpSeqMatcher.find() || !timeMatcher.find()) {
            return null
        }

        val bytes = bytesMatcher.group(1) ?: return null
        val icmpSeq = icmpSeqMatcher.group(1) ?: return null
        val time = timeMatcher.group(1) ?: return null

        return IcmpSeqEntity(
            bytes,
            icmpSeq,
            time
        )
    }

    fun getString(context: Context, stringSentPackets: Int): String {
        return context.getString(stringSentPackets)
    }

}
