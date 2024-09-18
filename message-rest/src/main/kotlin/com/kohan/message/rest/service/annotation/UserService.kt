package com.kohan.message.rest.service.annotation

import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
import org.springframework.stereotype.Service

@Service
class UserService {
    @Post("/user/nickname/{nickname}")
    @ProducesJson
    fun changeNickname(
        @Param("nickname") nickname: String,
    ) {
    }
}
