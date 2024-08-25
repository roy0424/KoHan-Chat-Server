package com.kohan.authentication.collection

import com.kohan.authentication.collection.item.TokenInfo
import com.kohan.shared.spring.mongo.collection.base.BaseCollection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collation = "user")
class UserCollection(
    /** 이메일 */
    @Indexed(unique = true)
    var email: String,
    /** 해쉬된 페스워드 */
    var password: String,
    /** 닉네임 */
    var nickname: String,
    /** 프로필 이미지 경로 */
    var profileImageUrl: String,
    /** 인증 토큰 정보 */
    var tokenInfo: TokenInfo,
) : BaseCollection()
