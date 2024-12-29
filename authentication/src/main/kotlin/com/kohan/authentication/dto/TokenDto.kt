package com.kohan.authentication.dto

import com.kohan.shared.collection.user.item.TokenInfo

class TokenDto(
    var token: String,
    var expiresAt: String,
    var issuanceDate: String,
) {
    companion object {
        fun from(tokenInfo: TokenInfo): TokenDto =
            TokenDto(
                token = tokenInfo.token,
                expiresAt = tokenInfo.expirationDate.toString(),
                issuanceDate = tokenInfo.issuanceDate.toString(),
            )
    }
}
