package com.kohan.authentication.vo

import jakarta.validation.constraints.NotEmpty

class SignIn(
    @NotEmpty(message = "이메일을 입력해주세요.")
    val email: String,
    @NotEmpty(message = "비밀번호를 입력해주세요.")
    val password: String,
)
