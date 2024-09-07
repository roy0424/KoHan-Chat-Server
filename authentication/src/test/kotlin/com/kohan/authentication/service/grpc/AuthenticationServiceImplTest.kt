package com.kohan.authentication.service.grpc

import com.kohan.authentication.collection.UserCollection
import com.kohan.authentication.collection.item.TokenInfo
import com.kohan.authentication.repository.UserRepository
import com.kohan.authentication.util.TokenGenerator
import com.kohan.authentication.util.UserUtil
import com.kohan.authentication.vo.SignUp
import com.kohan.shared.armeria.authentication.v1.Authentication.UserToken
import com.kohan.shared.armeria.authentication.v1.AuthenticationServiceGrpc.AuthenticationServiceBlockingStub
import com.kohan.shared.armeria.converter.request.result.AccessDeviceInfo
import com.linecorp.armeria.client.grpc.GrpcClients
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals

@SpringBootTest
@ExtendWith(SpringExtension::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class AuthenticationServiceImplTest(
    private val userRepository: UserRepository,
    private val userUtil: UserUtil,
    private val tokenGenerator: TokenGenerator,
) {
    private lateinit var user: UserCollection
    private lateinit var token: TokenInfo

    @BeforeEach
    fun setUp() {
        val userData = SignUp("test@test.com", "test1234", "test")
        val accessDeviceInfo =
            AccessDeviceInfo(ip = "127.0.0.1", userAgent = "Chrome128", os = "Windows10", device = "Other")
        token = tokenGenerator.generate(accessDeviceInfo)

        user = userUtil.toUserCollection(userData)
        user.tokenInfos.add(token)
        userRepository.save(user)
    }

    @Test
    fun authenticateUser() {
        val client =
            GrpcClients.newClient(
                "http://127.0.0.1:8080/grpc/v1/",
                AuthenticationServiceBlockingStub::class.java,
            )
        val userToken = UserToken.newBuilder().setToken(token.token).build()
        val responseUser = client.authenticateUser(userToken)

        assertEquals(user.email, responseUser.email)
        assertEquals(user.nickname, responseUser.nickname)
    }

    @AfterEach
    fun tearDown() {
        userRepository.delete(user)
    }
}
