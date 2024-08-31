package com.kohan.authentication.collection.item

import com.kohan.authentication.dto.TokenDto
import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime

class TokenInfo(
    /** 토큰 */
    @Indexed(unique = true)
    var token: String,
    /** 토큰 만료기한 */
    var expirationDate: LocalDateTime,
//    /** 마지막 접속 IP */
//    var lastAccessIp: String,
//    /** 마지막 접속 일시 */
//    var lastAccessDate: LocalDateTime,
//    /** 기기 MAC 주소 */
//    var macAddress: String,
) {
    fun toDto(): TokenDto {
        return TokenDto(
            token = token,
            expiresAt = expirationDate,
        )
    }
}
