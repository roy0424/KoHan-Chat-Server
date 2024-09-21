package com.kohan.message.rest.service.grpc

import com.kohan.message.rest.repository.UserProfileRepository
import com.kohan.proto.rest.v1.UserProfileOuterClass
import com.kohan.proto.rest.v1.UserProfileServiceGrpcKt
import com.kohan.shared.collection.user.UserProfileCollection
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserProfileGrpcService(
    private val userProfileRepository: UserProfileRepository
): UserProfileServiceGrpcKt.UserProfileServiceCoroutineImplBase() {
    @Transactional
    override suspend fun initUserProfile(request: UserProfileOuterClass.UserProfile): UserProfileOuterClass.UserProfile {
        val userProfile = userProfileRepository.save(UserProfileCollection.to(request))
        return UserProfileOuterClass.UserProfile.newBuilder()
            .setUserId(userProfile.userId.toHexString())
            .setNickname(userProfile.nickname)
            .setProfileImageFileId(userProfile.profileImageFileId)
            .build()
    }
}