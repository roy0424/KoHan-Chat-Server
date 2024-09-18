package com.kohan.shared.collection.user.item

import com.kohan.shared.armeria.converter.request.result.AccessDeviceInfo
import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime

class TokenInfo(
    /** Access Token */
    @Indexed(unique = true)
    val token: String,
    /** Used at sign-in to distinguish whether the device is new or not. */
    val accessDeviceInfo: AccessDeviceInfo,
    /** The amount of millis before the token expires. */
    val expirationMillis: Long,
    /** Renewed when a user issues a token using the sign-in api. */
    val issuanceDate: LocalDateTime = LocalDateTime.now(),
    /** Token expiration date */
    val expirationDate: LocalDateTime = issuanceDate.plus(expirationMillis, java.time.temporal.ChronoUnit.MILLIS),
)
