package com.example.net.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.net.R
import com.example.net.adapter.NetworkDiagnosisAdapter
import com.example.net.config.StartUpBean
import com.example.net.entity.NetworkDiagnosisEntity
import com.example.net.entity.PingEntity
import com.example.net.interfaces.OnNetworkDiagnosisItemClickListener
import com.example.net.util.*
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class NetworkDiagnosisActivity : AppCompatActivity() {
    private var context: Context? = null
    private var deviceInfo: String = ""
    private fun getLayoutId() = R.layout.act_network_diagnosis

    private val activityJob = SupervisorJob()
    private val activityScope = CoroutineScope(Dispatchers.Main + activityJob)

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: NetworkDiagnosisAdapter
    private val mList = ArrayList<NetworkDiagnosisEntity>()

    private var DOMAIN: String = ""
    private lateinit var startUpBean: StartUpBean

    private val mSeqList = ArrayList<Int>()

    private var mIp = ""

    private var mReceiveCnt = 0

    private val mPingData = PingEntity()
    // 用 PingProcess.stop/run 替代原 ReentrantLock，避免诊断页与 PingActivity 双开 ping
    private val pingProcess = PingProcess()
    // Ping 列表项 UI 节流时间戳，避免每行结果都 notify 造成布局风暴
    @Volatile
    private var lastPingUiMs = 0L


    companion object {
        const val POSITION_DNS = 0
        const val POSITION_NET = 1
        const val POSITION_DEVICE = 2
        const val POSITION_PING = 3

        const val REFRESH = "refresh"
        const val INTENT_FLAG = "URL"
        const val START_BEAN = "StartUpBean"

        fun startNetworkDiagnosisActivity(fromActivity: Activity?, url: String?) {
            val bean = StartUpBean()
            startNetworkDiagnosisActivity(fromActivity, url, bean)
        }

        /**
         * 启动网络诊断活动
         *
         * @param fromActivity 启动活动的上下文
         * @param url 要诊断的URL
         */
        fun startNetworkDiagnosisActivity(fromActivity: Activity?, url: String?, bean: StartUpBean?) {
            fromActivity?.run {
                if (url.isNullOrEmpty() || url == "null") {
                    // 如果URL为空或者为"null"，则提示输入正确的域名
                    Toast.makeText(this, getString(R.string.string_enter_valid_domain), Toast.LENGTH_SHORT).show()
                } else {
                    // 创建意图并传递URL
                    val intent = Intent(this, NetworkDiagnosisActivity::class.java).apply {
                        putExtra(INTENT_FLAG, url)
                        putExtra(START_BEAN, bean)
                    }
                    // 启动网络诊断活动
                    startActivity(intent)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        // 隐藏原生标题栏
        supportActionBar?.hide()
        setContentView(getLayoutId())
        // 获取传递过来的startUpBean对象
        startUpBean = intent.getSerializableExtra(START_BEAN) as? StartUpBean ?: StartUpBean()
        // 如果需要添加自定义的 titleBarLayout，则加载它（细节见 TitleBarBinder）
        TitleBarBinder.attach(this, startUpBean, R.id.root_layout)
        // Retrieve URL from intent extras
        val url = intent.getStringExtra(INTENT_FLAG) ?: DOMAIN
        // Assign URL to DOMAIN variable
        DOMAIN = url
        initView()
    }

    val initView = {
        initRecyclerView()
        refresh()
    }


    private val initRecyclerView = {
        mRecyclerView = findViewById(R.id.id_recycler_view)

        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        mRecyclerView.layoutManager = manager


        mAdapter = NetworkDiagnosisAdapter(this, getData())
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        mAdapter.setOnNetworkDiagnosisItemClickListener(object :
            OnNetworkDiagnosisItemClickListener {
            override fun onItemClick(position: Int) {
                if (position == POSITION_PING) {
                    // 进入详情页前先停掉诊断页 ping，避免双进程
                    pingProcess.stop()
                    PingActivity.startPingActivity(this@NetworkDiagnosisActivity, DOMAIN, mIp, startUpBean)
                } else if (position == POSITION_DEVICE) {
                    Toast.makeText(context, getString(R.string.string_copied), Toast.LENGTH_SHORT).show()
                    CommonUtils.copy(deviceInfo, context)
                }
            }
        })
    }


    private fun refresh() {
        refreshDns()
        // Net / Device（含 VPN NetworkInterface 枚举）放到 IO，避免首帧卡主线程
        activityScope.launch(Dispatchers.IO) {
            val net = strNet()
            val device = strDevice()
            // 主线程中才可修改UI
            withContext(Dispatchers.Main) {
                if (isFinishing || mList.size <= POSITION_DEVICE) {
                    return@withContext
                }
                mList[POSITION_NET].content = net
                mList[POSITION_DEVICE].content = device
                deviceInfo = device
                mAdapter.notifyItemChanged(POSITION_NET, REFRESH)
                mAdapter.notifyItemChanged(POSITION_DEVICE, REFRESH)
            }
        }
    }


    /**
     * 刷新dns
     */
    private val refreshDns = {
        mList[POSITION_DNS].content = strDns()
        mAdapter.notifyItemChanged(POSITION_DNS, REFRESH)
        //Dispatchers.IO	        子线程, 适合执行磁盘或网络 I/O操作
        //launch是异步，不会阻塞主线程
        //async是同步，会阻塞主线程
        activityScope.launch(Dispatchers.IO) {
            val dns = strDns(getDeferredResult(::analysisDns))
            ping()
            // 主线程中才可修改UI
            withContext(Dispatchers.Main) {
                if (isFinishing || mList.size <= POSITION_DNS) {
                    return@withContext
                }
                mList[POSITION_DNS].content = dns
                mAdapter.notifyItemChanged(POSITION_DNS, REFRESH)
            }
        }
    }


    private fun ping() {
        if (TextUtils.isEmpty(mIp)) {
            mPingData.dnsAnalysisFailed()
            updatePingUi(true)
            return
        }
        // PingProcess 当前可用时再跑；stop/run 内部保证不会叠两个进程
        thread {
            mSeqList.clear()
            mReceiveCnt = 0
            val exitCode = pingProcess.run(mIp) { line ->
                // 仅解析并更新 mPingData；勿 append Unit 到 StringBuilder
                "$line\n".formatPingMsg()
            }
            if (exitCode != 0 && mReceiveCnt == 0) {
                mPingData.notReachable(mIp)
                updatePingUi(true)
            }
        }
    }


    private fun String.formatPingMsg() {
        try {
            if (!TextUtils.isEmpty(this)) {
                if (this.contains("from $mIp")) {
                    //64 bytes from 119.29.126.90: icmp_seq=1 ttl=52 time=7.96 ms
                    val icmpSeqEntity = KotlinUtils.analysisIcmp(this)
                    icmpSeqEntity?.run {
                        if (!mSeqList.contains(seq.toInt())) {
                            mSeqList.add(seq.toInt())
                            mPingData.sendPackage = "${seq}/${PingProcess.COUNT}"
                            mPingData.receivePackage = "${++mReceiveCnt}/${PingProcess.COUNT}"
                            val receive =
                                "%.2f".format((seq.toFloat() - mReceiveCnt) / seq.toFloat() * 100)
                            mPingData.lostRate = "${receive}%"
                            mPingData.minRtt = ""
                            mPingData.maxRtt = ""
                            mPingData.aveRtt = ""
                            updatePingUi()
                        }
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
                    }
                } else if (this.contains("max")) {
                    //rtt min/avg/max/mdev = 7.441/13.580/31.051/6.413 ms
                    val statisticsAvgEntity = KotlinUtils.analysisStatisticsAvg(this)
                    statisticsAvgEntity?.run {
                        mPingData.minRtt = min
                        mPingData.maxRtt = max
                        mPingData.aveRtt = avg
//                        runOnUiThread {
//                            dismissLoading(ParamsConstants.COMMON_LOADING)
//                        }
                        updatePingUi(true)
                    }
                }
            }
        } catch (e: Exception) {
            mPingData.error()
            updatePingUi(true)
        }
    }


    private fun getSentryUploadData(): String {
        val str = StringBuffer()
        mList.forEach {
            str.append("${it.content}\n\n")
        }
        return str.toString()
    }


    private fun updatePingUi(isNeedUploadSentry: Boolean = false) {
        mList[POSITION_PING].content = mPingData.display()
        // 非最终结果时节流刷新，减轻 AutoSize/RecyclerView 主线程压力
        val now = System.currentTimeMillis()
        if (!isNeedUploadSentry && now - lastPingUiMs < PingProcess.UI_THROTTLE_MS) {
            return
        }
        lastPingUiMs = now
        runOnUiThread {
            if (isFinishing) {
                return@runOnUiThread
            }
            if (isNeedUploadSentry) {
                val map: MutableMap<String, String> = HashMap()
                map["MessageData"] = getSentryUploadData()
            }
            mAdapter.notifyItemChanged(POSITION_PING, REFRESH)
        }
    }


    /**
     * 获取deferred
     */
    private suspend fun <T> getDeferredResult(susFun: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            susFun()
        }
    }


    private suspend fun analysisDns() = suspendCoroutine<String> {
        thread {
            try {
//                runOnUiThread {
//                    showLoading(R.string.string_one_minute, ParamsConstants.COMMON_LOADING)
//                }
                val uri = URI(DOMAIN)
                val host = uri.host
                val ipAddress = InetAddress.getByName(host)
                mIp = ipAddress.hostAddress
                Logger.d("NetworkDiagnosisActivity----------------", mIp)
                it.resume(ipAddress.hostAddress)
            } catch (e: Exception) {
//                runOnUiThread {
//                    dismissLoading(ParamsConstants.COMMON_LOADING)
//                }
                mIp = ""
                it.resume("$e")
                Logger.d("NetworkDiagnosisActivity----------------", e.message)
            }
        }
    }


    private fun getData(): ArrayList<NetworkDiagnosisEntity> {
        mList.clear()

        mList.add(NetworkDiagnosisEntity(DOMAIN, strDns()))

        // Net / Device 先占位，真正内容在 IO 线程刷新，避免 onCreate 卡顿
        val loading = getString(R.string.string_dns_resolving)
        mList.add(NetworkDiagnosisEntity("Net", loading))

        deviceInfo = ""
        mList.add(NetworkDiagnosisEntity("Device", loading))

        mList.add(NetworkDiagnosisEntity("Ping", mPingData.display(), true))

        return mList
    }


    private fun isUseProxy(): String {
        val proxyAddress = System.getProperty("http.proxyHost")
        val proxyStr = System.getProperty("http.proxyPort")
        val proxyPort = proxyStr.takeIf { it != null } ?: "-1"
        val f = (!TextUtils.isEmpty(proxyAddress)) && proxyPort.toInt() != -1
        return if (f) {
            getString(R.string.string_connected)
        } else {
            getString(R.string.string_not_enable)
        }
    }


    private fun isUseVPN(): String {
        try {
            Collections.list(NetworkInterface.getNetworkInterfaces()).forEach {
                if (TextUtils.equals(it.name, "tun0") || TextUtils.equals(it.name, "ppp0")) {
                    return getString(R.string.string_connected)
                }
            }
        } catch (e: Exception) {
        }

        return getString(R.string.string_not_enable)
    }


    private fun strDns(dns: String = getString(R.string.string_dns_resolving)) =
        "    ${getString(R.string.string_dns_resolution)}:\n    $DOMAIN\n    $dns"

    private fun strNet(): String {
        val netStatus =
            if (NetworkUtils.isConnected(this)) getString(R.string.string_available) else getString(R.string.string_unavailable)
        val netType = AppHelper.getNetState(this)
        val proxy = isUseProxy()
        val vpn = isUseVPN()
        return "${getString(R.string.string_network_status)}: $netStatus\n${getString(R.string.string_network_type)}: $netType\n${
            getString(R.string.string_proxy_status)
        }: $proxy\n${getString(R.string.string_vpn_status)}: $vpn"
    }


    private fun androidVersion() = when (android.os.Build.VERSION.SDK_INT) {
        19 -> {
            "4.4"
        }

        20 -> {
            "4.4W"
        }

        21 -> {
            "5"
        }

        22 -> {
            "5.1"
        }

        23 -> {
            "6"
        }

        24 -> {
            "7"
        }

        25 -> {
            "7.1.1"
        }

        26 -> {
            "8"
        }

        27 -> {
            "8.1"
        }

        28 -> {
            "9"
        }

        29 -> {
            "10"
        }

        30 -> {
            "11"
        }

        31 -> {
            "12"
        }

        32 -> {
            "12L"
        }

        33 -> {
            "13"
        }

        34 -> {
            "14"
        }

        else -> {
            ""
        }
    }


    private fun strDevice(): String {
        val versionNameString = getConfigInfoString(CommonUtils.getAppVersionName(context), R.string.string_app_version)
        val versionCodeString = getConfigInfoString(CommonUtils.getAppVersionCodeStr(context), R.string.string_app_version_code)
        val uuidString = getConfigInfoString(AppHelper.getUUID(), "AppHash")


        return "${getString(R.string.string_device_brand)}: ${Build.BRAND}\n" +
                "${getString(R.string.string_device_model)}: ${Build.MODEL}\n" +
                "${getString(R.string.string_device_system)}: ${androidVersion()}\n" +
                "${getString(R.string.string_rom_system_type)}: ${CustomOSUtils.getCustomOS(Build.BRAND)}\n" +
                "${getString(R.string.string_rom_system_version)}: ${CustomOSUtils.getCustomOSVersion(Build.BRAND)}\n" +
                "${getString(R.string.string_current_time)}: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}" +
                versionNameString +
                versionCodeString +
                uuidString
    }

    private fun getConfigInfoString(value: String?, stringResourceId: Int): String {
        return if (value.isNullOrEmpty() || value == "null") {
            ""
        } else {
            "\n${getString(stringResourceId)}: $value"
        }
    }

    private fun getConfigInfoString(value: String?, key: String): String {
        return if (value.isNullOrEmpty() || value == "null") {
            ""
        } else {
            "\n$key: $value"
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PingActivity.REQUEST_CODE && resultCode == PingActivity.RESULT_CODE) {
            data?.run {
                val res = getSerializableExtra(PingActivity.RESULT_DATA)
                res?.run {
                    mPingData.copy(this as PingEntity)
                    updatePingUi()
                }
            }
        }
    }

    override fun onDestroy() {
        // 离开页面必须停掉 ping，避免原生进程残留
        pingProcess.stop()
        activityJob.cancel()
        super.onDestroy()
    }
}
