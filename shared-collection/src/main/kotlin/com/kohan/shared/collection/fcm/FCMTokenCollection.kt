package com.kohan.shared.collection.fcm

import com.kohan.shared.armeria.push.v1.FcmToken
import com.kohan.shared.collection.base.BaseCollection
import com.kohan.shared.collection.fcm.item.FCMTokenInfo
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document("fcm_tokens")
data class FCMTokenCollection(
    @Indexed(unique = true)
    val userId: ObjectId,
    val tokens: MutableList<FCMTokenInfo>,
) : BaseCollection() {
    companion object {
        fun to(fcmToken: FcmToken.RegisterFCMToken): FCMTokenCollection =
            FCMTokenCollection(
                ObjectId(fcmToken.userId),
                mutableListOf(
                    FCMTokenInfo.to(fcmToken),
                ),
            )
    }
}
