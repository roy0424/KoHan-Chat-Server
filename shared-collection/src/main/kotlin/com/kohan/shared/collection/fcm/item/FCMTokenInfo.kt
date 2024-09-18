package com.kohan.shared.collection.fcm.item

import com.kohan.proto.push.v1.FcmToken
import org.springframework.data.mongodb.core.index.Indexed
import java.time.LocalDateTime

class FCMTokenInfo(
    /** The FCM token. */
    @Indexed(unique = true)
    val token: String,
    /** The date and time the token was last accessed. */
    @Indexed
    var accessedAt: LocalDateTime,
) {
    companion object {
        fun to(fcmToken: FcmToken.RegisterFCMToken): FCMTokenInfo =
            FCMTokenInfo(
                token = fcmToken.fcmTokenInfo.token,
                accessedAt = LocalDateTime.parse(fcmToken.accessedAt),
            )
    }
}
