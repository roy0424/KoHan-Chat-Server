package com.kohan.message.rest.dto

import com.kohan.shared.collection.user.UserProfileCollection

class UserProfileDto(
    var nickname: String,
    var profileImageFileId: String,
    var statusMessage: String? = null,
) {
    companion object {
        fun from(userProfile: UserProfileCollection): UserProfileDto =
            UserProfileDto(
                nickname = userProfile.nickname,
                profileImageFileId = userProfile.profileImageFileId,
                statusMessage = userProfile.statusMessage,
            )
    }
}
