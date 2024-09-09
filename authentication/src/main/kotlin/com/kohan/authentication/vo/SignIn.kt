package com.kohan.authentication.vo

import jakarta.validation.constraints.NotBlank

class SignIn(
    @field:NotBlank(message = "Please enter email")
    val email: String,
    @field:NotBlank(message = "Please enter password")
    val password: String,
)
