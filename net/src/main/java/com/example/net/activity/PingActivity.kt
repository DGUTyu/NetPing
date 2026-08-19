package com.example.net.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.net.R
import com.example.net.activity.NetworkDiagnosisActivity.Companion.START_BEAN
import com.example.net.config.StartUpBean
import com.example.net.entity.PingEntity
import com.example.net.util.KotlinUtils
import com.example.net.util.PingProcess
import com.example.net.util.TitleBarBinder
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
    // 用 PingProcess.stop/run 替代原 ReentrantLock，避免与诊断页双开 ping
    private val pingProcess = PingProcess()
    private val pingOutput = StringBuilder()
    // UI 节流时间戳，避免每行 ping 输出都跑主线程 setText
    @Volatile
    private var lastUiMs = 0L

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
        // 强制不透明主题，避免继承宿主透明 Theme 导致首帧黑屏
        setTheme(R.style.NetPing_Activity)
        super.onCreate(savedInstanceState)
        // 隐藏原生标题栏
        supportActionBar?.hide()
        setContentView(getLayoutId())
        // 获取传递过来的startUpBean对象
        startUpBean = intent.getSerializableExtra(START_BEAN) as? StartUpBean ?: StartUpBean()
        // 如果需要添加自定义的 titleBarLayout，则加载它（细节见 TitleBarBinder）
        TitleBarBinder.attach(this, startUpBean, R.id.root_layout)
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
            //域名解析成功才可ping；等首帧后再起，减轻进页 ANR
            mTvPing.text = ""
            mTvPing.postDelayed({
                if (!isFinishing) {
                    ping()
                }
            }, 300L)
        }
    }

    private fun ping() {
        thread {
            mSeqList.clear()
            pingOutput.setLength(0)
            lastUiMs = 0L
            val exitCode = pingProcess.run(mIp) { line ->
                pingOutput.append("$line\n".formatPingMsg())
                flushPingUi(false)
            }
            // 收尾强制刷一次完整输出
            flushPingUi(true)
            if (exitCode != 0 && pingOutput.isEmpty()) {
                mPingData.notReachable(mIp)
                runOnUiThread {
                    if (!isFinishing) {
                        mTvPing.text = "$mIp is not reachable"
                    }
                }
            }
        }
    }

    /**
     * 将 ping 输出刷到 TextView；非 force 时按 [PingProcess.UI_THROTTLE_MS] 节流。
     */
    private fun flushPingUi(force: Boolean) {
        val now = System.currentTimeMillis()
        if (!force && now - lastUiMs < PingProcess.UI_THROTTLE_MS) {
            return
        }
        lastUiMs = now
        val text = pingOutput.toString()
        runOnUiThread {
            if (!isFinishing) {
                mTvPing.text = text
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
                        val lossRate = (PingProcess.COUNT - receive.toInt()) * (100f / PingProcess.COUNT)
                        val rate = String.format("%.2f", lossRate)
                        mPingData.lostRate = "${rate}%"
                        return mPingData.displayRate()
                    }
                } else if (this.contains("max")) {
                    //rtt min/avg/max/mdev = 7.441/13.580/31.051/6.413 ms
                    mIsPingFinish = true
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

    override fun onDestroy() {
        // 离开页面必须停掉 ping，避免原生进程残留
        pingProcess.stop()
        super.onDestroy()
    }
}
