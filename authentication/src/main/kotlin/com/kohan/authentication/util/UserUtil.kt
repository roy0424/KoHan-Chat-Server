package com.kohan.authentication.util

import com.kohan.authentication.client.grpc.FileClient
import com.kohan.authentication.collection.UserCollection
import com.kohan.authentication.vo.SignUp
import org.springframework.stereotype.Component

@Component
class UserUtil(
    private val passwordUtil: PasswordUtil,
    private val fileClient: FileClient,
) {
    fun toUserCollection(signUp: SignUp): UserCollection {
        return UserCollection(
            email = signUp.email,
            password = passwordUtil.hash(signUp.password),
            nickname = signUp.nickname,
            profileImageUrl = fileClient.upload(),
            tokenInfos = mutableListOf(),
        )
    }

    fun matches(
        password: String,
        hash: String,
    ): Boolean {
        return passwordUtil.matches(password, hash)
    }
}
