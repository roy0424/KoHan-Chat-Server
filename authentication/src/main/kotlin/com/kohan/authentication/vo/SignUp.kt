package com.kohan.authentication.vo

import com.linecorp.armeria.common.multipart.MultipartFile
import com.linecorp.armeria.server.annotation.Param
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

class SignUp(
    @Param
    @field:Email(message = "Invalid email format..")
    @field:NotBlank(message = "Please enter email")
    val email: String,

    @Param
    @field:Size(min = 8, max = 256, message = "Your password must be at least 8 characters and no more than 256 characters.")
    @field:NotBlank(message = "Please enter password")
    val password: String,

    @Param
    @field:Size(min = 2, max = 20, message = "Please enter a nickname of at least 2 characters and no more than 10 characters.")
    @field:NotBlank(message = "Please enter a nickname.")
    val nickname: String,

    @Param
    val profileImage: MultipartFile
)
