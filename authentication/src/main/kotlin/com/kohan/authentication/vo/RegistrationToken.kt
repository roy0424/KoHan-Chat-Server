package com.kohan.authentication.vo

import jakarta.validation.constraints.NotBlank

class RegistrationToken(
    @field:NotBlank(message = "please enter token")
    val token: String,
    @field:NotBlank(message = "please enter registration token")
    val registrationToken: String,
)
