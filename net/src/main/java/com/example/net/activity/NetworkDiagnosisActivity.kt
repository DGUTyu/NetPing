package com.example.net.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.example.net.R
import com.example.net.adapter.NetworkDiagnosisAdapter
import com.example.net.config.NetConfig
import com.example.net.config.NetConfigUtils
import com.example.net.entity.NetworkDiagnosisEntity
import com.example.net.entity.PingEntity
import com.example.net.interfaces.OnNetworkDiagnosisItemClickListener
import com.example.net.util.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * @author lizheng.zhao
 * @date 20230421
 * @description 体验一下协程的魅力
 */
class NetworkDiagnosisActivity : AppCompatActivity() {
    private var context: Context? = null
    private var deviceInfo: String = ""
    private fun getLayoutId() = R.layout.act_network_diagnosis


    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: NetworkDiagnosisAdapter
    private val mList = ArrayList<NetworkDiagnosisEntity>()

    private var DOMAIN: String = NetConfigUtils.getDefaultPingUrl()


    private val mSeqList = ArrayList<Int>()

    //不可重入锁
    private val mUuReentrantLock by lazy {
        ReentrantLock(false)
    }

    private var mIp = ""

    private var mReceiveCnt = 0


    private val mPingData = PingEntity()


    companion object {
        const val POSITION_DNS = 0
        const val POSITION_NET = 1
        const val POSITION_DEVICE = 2
        const val POSITION_PING = 3

        const val REFRESH = "refresh"
        const val INTENT_FLAG = "URL"
        val config = NetConfigUtils.getDefaultPingUrl()

        fun startNetworkDiagnosisActivity(fromActivity: Activity?) {
            startNetworkDiagnosisActivity(fromActivity, config)
        }


        fun startNetworkDiagnosisActivity(fromActivity: Activity?, url: String?) {
            fromActivity?.run {
                val intent = Intent(this, NetworkDiagnosisActivity::class.java).apply {
                    // 使用传递的 URL，如果为空，则使用默认的域名
                    putExtra(INTENT_FLAG, if (url.isNullOrEmpty() || url == "null") config else url)
                }
                startActivity(intent)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        // 隐藏原生标题栏
        supportActionBar?.hide()
        setContentView(getLayoutId())
        // 如果需要添加自定义的 titleBarLayout，则加载它
        val customTitleBarLayoutId = NetConfigUtils.getTitleBarLayoutId()
        if (customTitleBarLayoutId != NetConfig.NOT_LAYOUT_ID) {
            val customTitleBarLayout = LayoutInflater.from(this).inflate(customTitleBarLayoutId, null)
            // 将自定义的 titleBarLayout 添加到布局中
            val rootView = findViewById<LinearLayout>(R.id.root_layout)
            val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            // 添加在第一个位置
            rootView.addView(customTitleBarLayout, 0, layoutParams)
            initListener(customTitleBarLayout)
        }
        // Retrieve URL from intent extras
        val url = intent.getStringExtra(INTENT_FLAG) ?: DOMAIN
        // Assign URL to DOMAIN variable
        DOMAIN = url
        initView()
    }

    private fun initListener(customTitleBarLayout: View?) {
        customTitleBarLayout?.setOnClickListener {
            // 处理点击事件，finish当前页面
            finish()
        }
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
                    PingActivity.startPingActivity(this@NetworkDiagnosisActivity, DOMAIN, mIp)
                } else if (position == POSITION_DEVICE) {
                    Toast.makeText(context, getString(R.string.string_copied), Toast.LENGTH_SHORT).show()
                    CommonUtils.copy(deviceInfo, context)
                }
            }
        })
    }


    private fun refresh() {
        refreshDns()
        refreshNet()
        refreshDevice()
    }


    /**
     * 刷新设备信息
     */
    private val refreshDevice = {
        mList[POSITION_DEVICE].content = strDevice()
        mAdapter.notifyItemChanged(POSITION_DEVICE, REFRESH)
    }


    /**
     * 刷新网络网络状态
     */
    private val refreshNet = {
        mList[POSITION_NET].content = strNet()
        mAdapter.notifyItemChanged(POSITION_NET, REFRESH)
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
        CoroutineScope(Dispatchers.IO).launch {
            mList[POSITION_DNS].content = strDns(getDeferredResult(::analysisDns))
            ping()
            //主线程中才可修改UI
            withContext(Dispatchers.Main) {
                mAdapter.notifyItemChanged(POSITION_DNS, REFRESH)
            }
        }
    }


    private val pingThread = {
        thread {
            mSeqList.clear()
            mUuReentrantLock.lock()
            mReceiveCnt = 0
            val command = "ping -c 10 $mIp"
            val process = Runtime.getRuntime().exec(command)
            val input = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            val pingOutput = StringBuilder()
            while (input.readLine().also { line = it } != null) {
                pingOutput.append("$line\n".formatPingMsg())
            }

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                mPingData.notReachable(mIp)
                updatePingUi(true)
            }
            mUuReentrantLock.unlock()
        }
    }

    private val ping = {
        if (TextUtils.isEmpty(mIp)) {
            mPingData.dnsAnalysisFailed()
            updatePingUi(true)
        } else {
            if (!mUuReentrantLock.isLocked) {
                // lock 对象当前可用，可以执行其他操作
                pingThread()
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
                        if (mSeqList.contains(seq.toInt())) {
                        } else {
                            mSeqList.add(seq.toInt())
                            mPingData.sendPackage = "${seq}/10"
                            mPingData.receivePackage = "${++mReceiveCnt}/10"
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
                        val lossRate = (10 - receive.toInt()) * 10f
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
            val map: MutableMap<String, String> = HashMap()
            map["message"] = e.message.toString()
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
        runOnUiThread {
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
        //Dispatchers.IO	        子线程, 适合执行磁盘或网络 I/O操作
        val deferred = CoroutineScope(Dispatchers.IO).async {
            susFun()
        }
        return deferred.await()
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

        mList.add(NetworkDiagnosisEntity("Net", strNet()))

        deviceInfo = strDevice()
        mList.add(NetworkDiagnosisEntity("Device", deviceInfo))


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
        val appTimeInfo = CommonUtils.getAppTimeInfo(context);
        val appDigest = CommonUtils.getAppDigest(context);
        val appInstallTimeString = getConfigInfoString(appTimeInfo[0], R.string.string_app_install_time)
        val appUpdateTimeString = getConfigInfoString(appTimeInfo[1], R.string.string_app_recent_update_time)
        val versionNameString = getConfigInfoString(CommonUtils.getAppVersionName(context), R.string.string_app_version)
        val versionCodeString = getConfigInfoString(CommonUtils.getAppVersionCodeStr(context), R.string.string_app_version_code)
        val appMD5String = getConfigInfoString(appDigest[0], "AppMD5")
        val appSHA1String = getConfigInfoString(appDigest[1], "SHA1")
        val appSHA256String = getConfigInfoString(appDigest[2], "SHA256")


        return "${getString(R.string.string_device_brand)}: ${Build.BRAND}\n" +
                "${getString(R.string.string_device_model)}: ${Build.MODEL}\n" +
                "${getString(R.string.string_device_system)}: ${androidVersion()}\n" +
                "${getString(R.string.string_rom_system_type)}: ${CustomOSUtils.getCustomOS(Build.BRAND)}\n" +
                "${getString(R.string.string_rom_system_version)}: ${CustomOSUtils.getCustomOSVersion(Build.BRAND)}\n" +
                "${getString(R.string.string_current_time)}: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}" +
                appInstallTimeString +
                appUpdateTimeString +
                versionNameString +
                versionCodeString +
                appMD5String +
                appSHA1String +
                appSHA256String
    }

    private fun getConfigInfoString(value: String?, stringResourceId: Int): String {
        return if (value != NetConfig.NOT_SET) {
            "\n${getString(stringResourceId)}: $value"
        } else {
            ""
        }
    }

    private fun getConfigInfoString(value: String?, key: String): String {
        return if (value != NetConfig.NOT_SET) {
            "\n$key: $value"
        } else {
            ""
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
}