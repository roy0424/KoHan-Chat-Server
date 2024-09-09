package com.kohan.authentication.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordUtil(
    @Value("\${kohan.authentication.password.hash.saltLength}")
    private val saltLength: Int,
    @Value("\${kohan.authentication.password.hash.hashLength}")
    private val hashLength: Int,
    @Value("\${kohan.authentication.password.hash.parallelism}")
    private val parallelism: Int,
    @Value("\${kohan.authentication.password.hash.memory}")
    private val memory: Int,
    @Value("\${kohan.authentication.password.hash.iterations}")
    private val iterations: Int,
) {
    private val argon2PasswordEncoder = Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations)

    fun hash(password: String): String = argon2PasswordEncoder.encode(password)

    fun matches(
        password: String,
        hash: String,
    ): Boolean = argon2PasswordEncoder.matches(password, hash)
}
