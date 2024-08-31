package com.kohan.authentication.vo

import com.linecorp.armeria.common.multipart.MultipartFile
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

class SignUp(
    @Email(message = "이메일 형식이 아닙니다.")
    @NotEmpty(message = "이메일을 입력해주세요.")
    val email: String,
    @Size(min = 8, max = 256, message = "비밀번호는 8자 이상 256자 이하로 입력해주세요.")
    @NotEmpty(message = "비밀번호를 입력해주세요.")
    val password: String,
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    @NotEmpty(message = "닉네임을 입력해주세요.")
    val nickname: String,
    val profileImage: MultipartFile,
)
