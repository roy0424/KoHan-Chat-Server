package com.kohan.shared.spring.converter.request.parser

import com.kohan.shared.collection.user.item.AccessDeviceInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AccessInfoParserTest {
    @Test
    fun normalParseTest() {
        val ip = "127.0.0.1"
        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

        val accessInfoParser = AccessInfoParser()

        val info = accessInfoParser.parse(ip, userAgent)
        val answer = AccessDeviceInfo(ip = "127.0.0.1", userAgent = "Chrome128", os = "Windows10", device = "Other")
        assertEquals(answer, info)
    }

    @Test
    fun unknownParseTest() {
        val ip = "127.0.0.1"
        val userAgent = "Unknown"

        val accessInfoParser = AccessInfoParser()

        val info = accessInfoParser.parse(ip, userAgent)
        val answer = AccessDeviceInfo(ip = "127.0.0.1", userAgent = "Othernull", os = "Othernull", device = "Other")
        assertEquals(answer, info)
    }
}
