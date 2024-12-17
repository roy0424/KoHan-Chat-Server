package com.kohan.shared.collection.user

import com.kohan.shared.collection.base.BaseCollection
import com.kohan.shared.collection.user.item.TokenInfo
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user")
data class UserCollection(
    /** Used as user login ID */
    @Indexed(unique = true)
    var email: String,
    /** Hashed password */
    var password: String,
    /** Access tokens used for user authentication
     *
     *  A single user can use multiple devices with the same account */
    var tokenInfos: MutableList<TokenInfo>,
) : BaseCollection() {
    fun removeToken(token: String) {
        tokenInfos.removeIf { it.token == token }
    }
}
