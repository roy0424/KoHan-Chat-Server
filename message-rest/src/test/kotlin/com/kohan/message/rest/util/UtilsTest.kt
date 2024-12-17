package com.kohan.message.rest.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UtilsTest {
    @Test
    fun transformList() {
        val list = listOf(1, 2, 3)
        val result = transformList(list) { it.toString() }

        assertEquals(listOf("1", "2", "3"), result)
    }

    @Test
    fun addItemInList() {
        val list = listOf(1, 2, 3)
        val result = addItemInList(list, 4)

        assertEquals(listOf(1, 2, 3, 4), result)
    }

    @Test
    fun isExpiredAfterDuration() {
        val target = LocalDateTime.now().minusMinutes(5)
        val duration = 5L

        assertEquals(true, isExpiredAfterDuration(target, duration))
    }
}
