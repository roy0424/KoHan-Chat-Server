package com.kohan.authentication.util

import com.kohan.authentication.client.grpc.FileClient
import com.kohan.authentication.vo.SignUp
import org.springframework.stereotype.Component
import com.kohan.shared.collection.user.UserCollection

@Component
class UserUtil(
    private val passwordUtil: PasswordUtil,
    private val fileClient: FileClient,
) {
    fun toUserCollection(signUp: SignUp): UserCollection =
        UserCollection(
            email = signUp.email,
            password = passwordUtil.hash(signUp.password),
            nickname = signUp.nickname,
            profileImageUrl = fileClient.upload(),
            statusMessage = null,
            tokenInfos = mutableListOf(),
        )

    fun matches(
        password: String,
        hash: String,
    ): Boolean = passwordUtil.matches(password, hash)
}
