package com.kohan.authentication.collection.item

import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime

class TokenInfo(
    /** 토큰 */
    @Indexed(unique = true)
    val token: String,
    /** 토큰 만료기한 */
    val expirationDate: LocalDateTime,
)
