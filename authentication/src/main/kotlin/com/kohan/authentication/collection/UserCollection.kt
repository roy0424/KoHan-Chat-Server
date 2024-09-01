package com.kohan.authentication.collection

import com.kohan.authentication.collection.item.TokenInfo
import com.kohan.shared.spring.mongo.collection.base.BaseCollection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collation = "user")
class UserCollection(
    /** Used as user login ID */
    @Indexed(unique = true)
    var email: String,
    /** Hashed password */
    var password: String,
    /** Name as seen by other users */
    var nickname: String,
    /** File server request URL*/
    var profileImageUrl: String,
    /** Access tokens used for user authentication <br>
     *
     *  A single user can use multiple devices with the same account */
    var tokenInfos: MutableList<TokenInfo>,
) : BaseCollection()
