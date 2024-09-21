package com.kohan.authentication.util

import com.kohan.authentication.vo.SignUp
import com.kohan.shared.collection.user.UserCollection
import org.springframework.stereotype.Component

@Component
class UserUtil(
    private val passwordUtil: PasswordUtil,
) {
    fun toUserCollection(signUp: SignUp): UserCollection =
        UserCollection(
            email = signUp.email,
            password = passwordUtil.hash(signUp.password),
            tokenInfos = mutableListOf(),
        )

    fun matches(
        password: String,
        hash: String,
    ): Boolean = passwordUtil.matches(password, hash)
}
