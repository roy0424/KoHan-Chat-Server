package com.kohan.message.rest.vo

import com.linecorp.armeria.common.multipart.MultipartFile
import com.linecorp.armeria.server.annotation.Param

class ProfileImage(
    @Param
    val profileImage: MultipartFile
)
