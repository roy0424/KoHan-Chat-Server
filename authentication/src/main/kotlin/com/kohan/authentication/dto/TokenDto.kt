package com.kohan.authentication.dto

import com.kohan.shared.collection.user.item.TokenInfo
import java.time.LocalDateTime

class TokenDto(
    var token: String,
    var expiresAt: LocalDateTime,
    var issuanceDate: LocalDateTime,
) {
    companion object {
        fun from(tokenInfo: TokenInfo): TokenDto {
            return TokenDto(
                token = tokenInfo.token,
                expiresAt = tokenInfo.expirationDate,
                issuanceDate = tokenInfo.issuanceDate,
            )
        }
    }
}
