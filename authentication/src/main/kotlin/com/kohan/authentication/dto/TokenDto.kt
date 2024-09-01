package com.kohan.authentication.dto

import java.time.LocalDateTime

class TokenDto(
    var token: String,
    var expiresAt: LocalDateTime,
    var issuanceDate: LocalDateTime,
)
