package com.example.net.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.net.R
import com.example.net.activity.NetworkDiagnosisActivity.Companion.START_BEAN
import com.example.net.config.StartUpBean
import com.example.net.entity.PingEntity
import com.example.net.util.KotlinUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class PingActivity : AppCompatActivity() {

    private fun getLayoutId() = R.layout.act_ping


    private lateinit var mTvDomain: TextView
    private lateinit var mTvDNS: TextView
    private lateinit var mTvPing: TextView

    private val mSeqList = ArrayList<Int>()


    private var mDomain = ""
    private var mIp = ""
    private lateinit var startUpBean: StartUpBean

    private var mIsPingFinish = false

    private val mPingData = PingEntity()

    //不可重入锁
    private val mUuReentrantLock by lazy {
        ReentrantLock(false)
    }


    companion object {
        const val DATA_DOMAIN = "DOMAIN"
        const val DATA_IP = "IP"
        const val REQUEST_CODE = 1001
        const val RESULT_CODE = 1002
        const val RESULT_DATA = "result_data"

        fun startPingActivity(
                fromActivity: Activity?,
                domain: String? = "",
                ip: String? = "",
                startUpBean: StartUpBean
        ) {
            fromActivity?.run {
                val intent = Intent(fromActivity, PingActivity::class.java)
                intent.putExtra(DATA_DOMAIN, domain)
                intent.putExtra(DATA_IP, ip)
                intent.putExtra(START_BEAN, startUpBean)
                startActivityForResult(intent, REQUEST_CODE)
            }
        }
    }

    private fun setDomain(domain: String) {
        mDomain = domain
    }

    private fun setIp(ip: String) {
        mIp = ip
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 隐藏原生标题栏
        supportActionBar?.hide()
        setContentView(getLayoutId())
        // 获取传递过来的startUpBean对象
        startUpBean = intent.getSerializableExtra(START_BEAN) as? StartUpBean ?: StartUpBean()
        // 如果需要添加自定义的 titleBarLayout，则加载它
        val customTitleBarLayoutId = startUpBean.titleBarLayoutId
        if (customTitleBarLayoutId != StartUpBean.NOT_LAYOUT_ID) {
            val customTitleBarLayout = LayoutInflater.from(this).inflate(customTitleBarLayoutId, null)
            // 将自定义的 titleBarLayout 添加到布局中
            val rootView = findViewById<LinearLayout>(R.id.root_layout)
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            // 添加在第一个位置
            rootView.addView(customTitleBarLayout, 0, layoutParams)
            val backView = customTitleBarLayout.findViewById<View>(startUpBean.backId)
            // 设置点击事件，如果 backView 为空则设置 customTitleBarLayout 的点击事件，否则设置 backView 的点击事件
            (backView ?: customTitleBarLayout).setOnClickListener {
                // 处理点击事件，finish当前页面
                finish()
            }
        }
        intent?.run {
            getStringExtra(DATA_DOMAIN)?.let(::setDomain)
            getStringExtra(DATA_IP)?.let(::setIp)
        }
        if (!TextUtils.isEmpty(mDomain)) {
            initView()
        }
    }

    private fun initView() {
        mTvDomain = R.id.id_tv_domain.getView()
        mTvDNS = R.id.id_tv_dns.getView()
        mTvPing = R.id.id_tv_ping.getView()

        //文字内容可滚动
        mTvPing.movementMethod = ScrollingMovementMethod.getInstance()


        mTvDomain.text = "${getString(R.string.string_domain)}: $mDomain"
        mTvDNS.text = strDns()


        refresh()
    }


    private fun refresh() {
        if (!TextUtils.isEmpty(mIp)) {
            //域名解析成功才可ping
            if (!mUuReentrantLock.isLocked) {
                mTvPing.text = ""
                ping()
            }
        }
    }


    private fun ping() {
        thread {
            mSeqList.clear()
            mUuReentrantLock.lock()
            val command = "ping -c 10 $mIp"
            val process = Runtime.getRuntime().exec(command)
            val input = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            val pingOutput = StringBuilder()
            while (input.readLine().also { line = it } != null) {
                pingOutput.append("$line\n".formatPingMsg())
                runOnUiThread {
                    mTvPing.text = pingOutput.toString()
                }
            }

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                mPingData.notReachable(mIp)
                mUuReentrantLock.unlock()
                runOnUiThread {
                    mTvPing.text = "$mIp is not reachable"
                }
            }
        }
    }


    private fun String.formatPingMsg(): String {
        try {
            if (!TextUtils.isEmpty(this)) {
                if (this.contains("($mIp)")) {
                    val endIndex = this.indexOf("($mIp)")
                    return "${this.substring(0, endIndex)}\n\n"
                } else if (this.contains("from $mIp")) {
                    //64 bytes from 119.29.126.90: icmp_seq=1 ttl=52 time=7.96 ms
                    val icmpSeqEntity = KotlinUtils.analysisIcmp(this)
                    icmpSeqEntity?.run {
                        if (mSeqList.contains(seq.toInt())) {
                            return ""
                        } else {
                            mSeqList.add(seq.toInt())
                        }
                        return display()
                    }
                } else if (this.contains("transmitted")) {
                    //10 packets transmitted, 10 received, +27 duplicates, 0% packet loss, time 9013ms
                    val statisticsEntity = KotlinUtils.analysisStatistics(this)
                    statisticsEntity?.run {
                        mPingData.sendPackage = sent
                        mPingData.receivePackage = receive
                        val lossRate = (10 - receive.toInt()) * 10f
                        val rate = String.format("%.2f", lossRate)
                        mPingData.lostRate = "${rate}%"
                        return mPingData.displayRate()
                    }
                } else if (this.contains("max")) {
                    //rtt min/avg/max/mdev = 7.441/13.580/31.051/6.413 ms
                    mIsPingFinish = true
                    mUuReentrantLock.unlock()
                    val statisticsAvgEntity = KotlinUtils.analysisStatisticsAvg(this)
                    statisticsAvgEntity?.run {
                        mPingData.minRtt = min
                        mPingData.maxRtt = max
                        mPingData.aveRtt = avg
                        return mPingData.displayStatistics()
                    }
                }
            }
        } catch (e: Exception) {
            /*SentryUtils.uploadTryCatchException(
                e,
                SentryUtils.getClassNameAndMethodName()
            )*/
            mUuReentrantLock.unlock()
            mPingData.error()
        }
        return this
    }


    private fun strDns() =
            if (TextUtils.isEmpty(mIp)) "${getString(R.string.string_dns_resolution)}:\n$mDomain\n" else "${
                getString(
                        R.string.string_dns_resolution
                )
            }:\n$mDomain\n$mIp"


    fun Int.getView(): TextView = findViewById<TextView>(this)
}