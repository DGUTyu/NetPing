package com.example.net.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class PingProcessTest {

    @Test
    fun commandUsesCountTenAndDeadlineTwenty() {
        assertArrayEquals(
            arrayOf("ping", "-c", "10", "-w", "20", "1.2.3.4"),
            PingProcess.command("1.2.3.4")
        )
    }

    @Test
    fun constantsMatchPlan() {
        assertEquals(10, PingProcess.COUNT)
        assertEquals(20, PingProcess.DEADLINE_SECONDS)
        assertEquals(500L, PingProcess.UI_THROTTLE_MS)
    }

    @Test
    fun stopOnIdleProcessDoesNotThrow() {
        PingProcess().stop()
    }
}
