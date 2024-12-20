package com.kohan.message.rest.dto.chat

import com.kohan.shared.collection.user.UserProfileCollection

class UserProfileDto(
    var id: String,
    var nickname: String,
    var profileImageFileId: String,
    var statusMessage: String? = null,
) {
    companion object {
        fun from(userProfile: UserProfileCollection): UserProfileDto =
            UserProfileDto(
                id = userProfile._id.toHexString(),
                nickname = userProfile.nickname,
                profileImageFileId = userProfile.profileImageFileId,
                statusMessage = userProfile.statusMessage,
            )
    }
}
