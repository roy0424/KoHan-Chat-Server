package com.kohan.authentication.service

import com.kohan.authentication.collection.item.TokenInfo
import com.kohan.authentication.repository.UserRepository
import com.kohan.authentication.service.annotation.UserService
import com.kohan.authentication.util.TokenGenerator
import com.kohan.authentication.util.UserUtil
import com.kohan.authentication.vo.SignUp
import com.kohan.shared.armeria.converter.request.result.AccessDeviceInfo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Transactional
@SpringBootTest
@ExtendWith(SpringExtension::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UserServiceTest(
    userRepository: UserRepository,
    userUtil: UserUtil,
    tokenGenerator: TokenGenerator,
) : UserService(userRepository, userUtil, tokenGenerator) {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userUtil: UserUtil

    @Autowired
    private lateinit var tokenGenerator: TokenGenerator

    @Test
    fun duplicateToken() {
        val signup1 = SignUp("test1", "test", "test")
        val signup2 = SignUp("test2", "test", "test")
        val accessDeviceInfo = AccessDeviceInfo(ip = "127.0.0.1", userAgent = "Chrome128", os = "Windows10", device = "Other")
        val token = TokenInfo("test", accessDeviceInfo, 1000000, LocalDateTime.now())

        val user1 = userUtil.toUserCollection(signup1)
        user1.tokenInfos.add(token)
        val user2 = userUtil.toUserCollection(signup2)
        user2.tokenInfos.add(token)

        assertThrows<DuplicateKeyException> {
            userRepository.save(user1)
            userRepository.save(user2)
        }
    }

    @Test
    fun duplicateEmail() {
        val signup = SignUp("test", "test", "test")
        val accessDeviceInfo = AccessDeviceInfo(ip = "127.0.0.1", userAgent = "Chrome128", os = "Windows10", device = "Other")
        val token1 = tokenGenerator.generate(accessDeviceInfo)
        val token2 = tokenGenerator.generate(accessDeviceInfo)

        val user1 = userUtil.toUserCollection(signup)
        user1.tokenInfos.add(token1)
        val user2 = userUtil.toUserCollection(signup)
        user2.tokenInfos.add(token2)

        assertThrows<DuplicateKeyException> {
            userRepository.save(user1)
            userRepository.save(user2)
        }
    }

    @DisplayName("Save new user test")
    @RepeatedTest(value = 100, name = "{displayName} {currentRepetition}/{totalRepetitions}")
    fun saveUserWithNewTokenTest() {
        assertDoesNotThrow {
            saveUserWithNewToken(
                userUtil.toUserCollection(SignUp(UUID.randomUUID().toString(), "test", "test")),
                AccessDeviceInfo(ip = "127.0.0.1", userAgent = "Chrome128", os = "Windows10", device = "Other"),
            )
        }
    }
}
