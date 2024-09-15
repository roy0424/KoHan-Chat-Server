package com.kohan.authentication.collection

import com.kohan.authentication.collection.item.TokenInfo
import com.kohan.shared.armeria.authentication.v1.Authentication.UserDto
import com.kohan.shared.spring.mongo.collection.base.BaseCollection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user")
data class UserCollection(
    /** Used as user login ID */
    @Indexed(unique = true)
    var email: String,
    /** Hashed password */
    var password: String,
    /** Name as seen by other users */
    var nickname: String,
    /** File server request URL*/
    var profileImageUrl: String,
    /** Access tokens used for user authentication
     *
     *  A single user can use multiple devices with the same account */
    var tokenInfos: MutableList<TokenInfo>,
) : BaseCollection() {
    fun toDto(): UserDto =
        UserDto
            .newBuilder()
            .setObjectId(id.toString())
            .setEmail(email)
            .setNickname(nickname)
            .setProfileImageUrl(profileImageUrl)
            .build()

    fun removeToken(token: String) {
        tokenInfos.removeIf { it.token == token }
    }
}
