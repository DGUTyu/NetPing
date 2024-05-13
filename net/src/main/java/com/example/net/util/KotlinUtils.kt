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
        str?.run {
            //rtt min/avg/max/mdev = 7.441/13.580/31.051/6.413 m
            val rttPattern =
                Pattern.compile("""rtt min/avg/max/mdev = ([\d.]+)/([\d.]+)/([\d.]+)/([\d.]+)""")
            val rttMatcher = rttPattern.matcher(this)

            rttMatcher.find()

            val rttMin = rttMatcher.group(1)
            val rttAvg = rttMatcher.group(2)
            val rttMax = rttMatcher.group(3)

            return StatisticsAvgEntity(
                rttMax,
                rttMin,
                rttAvg
            )
        }
        return null
    }


    @Throws
    fun analysisStatistics(str: String?): StatisticsEntity? {
        str?.run {
            //10 packets transmitted, 10 received, +27 duplicates, 0% packet loss, time 9013ms
            val packetsPattern = Pattern.compile("""(\d+)\s+packets""")
            val receivedPattern = Pattern.compile("""(\d+)\s+received""")

            val packetsMatcher = packetsPattern.matcher(this)
            val receivedMatcher = receivedPattern.matcher(this)

            packetsMatcher.find()
            receivedMatcher.find()

            val sent = packetsMatcher.group(1)
            val received = receivedMatcher.group(1)

            return StatisticsEntity(
                sent,
                received
            )
        }
        return null
    }

    @Throws
    fun analysisIcmp(str: String?): IcmpSeqEntity? {
        //64 bytes from 119.29.126.90: icmp_seq=1 ttl=52 time=7.96 ms
        str?.run {
            val bytesPattern = Pattern.compile("""(\d+)\s+bytes""")
            val icmpSeqPattern = Pattern.compile("""icmp_seq=(\d+)""")
            val timePattern = Pattern.compile("""time=([\d.]+)\s+ms""")

            val bytesMatcher = bytesPattern.matcher(this)
            val icmpSeqMatcher = icmpSeqPattern.matcher(this)
            val timeMatcher = timePattern.matcher(this)

            bytesMatcher.find()
            icmpSeqMatcher.find()
            timeMatcher.find()

            val bytes = bytesMatcher.group(1)
            val icmpSeq = icmpSeqMatcher.group(1)
            val time = timeMatcher.group(1)

            return IcmpSeqEntity(
                bytes,
                icmpSeq,
                time
            )
        }
        return null
    }

    fun getString(context:Context,stringSentPackets: Int): String {
        return context.getString(stringSentPackets)
    }

}