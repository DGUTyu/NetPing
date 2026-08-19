package com.example.net.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * 可销毁、带总截止时间的 ping。
 * 命令为 `ping -c 10 -w 20`，避免联迪 POS 上无限 readLine / 双开打满。
 * [run] 必须在后台线程调用；[stop] 可在任意线程调用。
 */
class PingProcess {

    @Volatile
    private var process: Process? = null
    private val stopped = AtomicBoolean(false)
    // 递增代数，避免上一轮 watchdog 误杀本轮进程
    private val generation = AtomicInteger(0)

    companion object {
        /** 发包次数 */
        const val COUNT = 10
        /** ping 总截止秒数（-w），比单包 -W 更跨 busybox/toybox 兼容 */
        const val DEADLINE_SECONDS = 20
        /** 主线程刷新节流间隔 */
        const val UI_THROTTLE_MS = 200L
        private const val WATCHDOG_EXTRA_MS = 2000L
        private const val EXIT_WAIT_MS = 1500L

        @JvmStatic
        fun command(ip: String): Array<String> {
            return arrayOf(
                "ping",
                "-c",
                COUNT.toString(),
                "-w",
                DEADLINE_SECONDS.toString(),
                ip
            )
        }
    }

    /**
     * 关流 + destroy，打断可能阻塞的 readLine。
     */
    fun stop() {
        stopped.set(true)
        val running = process
        process = null
        if (running != null) {
            closeQuietly(running.inputStream)
            closeQuietly(running.errorStream)
            try {
                running.destroy()
            } catch (ignored: Exception) {
            }
        }
    }

    /**
     * 阻塞直到 ping 结束、被 [stop]、或 watchdog 超时。
     * @return 进程 exit code；未启动或中断时返回 -1
     */
    fun run(ip: String, onLine: (String) -> Unit): Int {
        val gen = generation.incrementAndGet()
        stop()
        stopped.set(false)
        if (ip.isBlank()) {
            return -1
        }
        val started = try {
            Runtime.getRuntime().exec(command(ip))
        } catch (e: Exception) {
            return -1
        }
        process = started
        // 排空 stderr，避免管道塞满导致 ping 卡住
        drainErrorStream(started)
        // 截止后再多等一会儿，兜底 destroy（不依赖 stopped 状态）
        val watchdog = Thread(Runnable {
            try {
                Thread.sleep(DEADLINE_SECONDS * 1000L + WATCHDOG_EXTRA_MS)
                if (generation.get() == gen) {
                    stop()
                }
            } catch (ignored: InterruptedException) {
            }
        })
        watchdog.isDaemon = true
        watchdog.start()
        try {
            val input = BufferedReader(InputStreamReader(started.inputStream))
            var line: String? = null
            while (!stopped.get() && input.readLine().also { line = it } != null) {
                val text = line
                if (text != null) {
                    onLine(text)
                }
            }
            // 有界等待 exit，避免 destroy 后 waitFor 永久挂起
            return waitForExit(started)
        } finally {
            watchdog.interrupt()
            if (generation.get() == gen) {
                stop()
            }
        }
    }

    private fun drainErrorStream(started: Process) {
        val err = Thread(Runnable {
            try {
                val reader = BufferedReader(InputStreamReader(started.errorStream))
                while (reader.readLine() != null) {
                    // discard stderr so the pipe cannot stall ping
                }
            } catch (ignored: Exception) {
            }
        })
        err.isDaemon = true
        err.start()
    }

    private fun waitForExit(p: Process): Int {
        val deadline = System.currentTimeMillis() + EXIT_WAIT_MS
        while (System.currentTimeMillis() < deadline) {
            try {
                return p.exitValue()
            } catch (e: IllegalThreadStateException) {
                try {
                    Thread.sleep(50)
                } catch (ie: InterruptedException) {
                    return -1
                }
            }
        }
        try {
            p.destroy()
        } catch (ignored: Exception) {
        }
        return -1
    }

    private fun closeQuietly(stream: InputStream?) {
        if (stream == null) {
            return
        }
        try {
            stream.close()
        } catch (ignored: Exception) {
        }
    }
}
