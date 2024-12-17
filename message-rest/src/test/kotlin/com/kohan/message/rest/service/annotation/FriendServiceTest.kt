package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.friend.FriendRepository
import com.kohan.message.rest.repository.user.profile.UserProfileRepository
import com.kohan.message.rest.vo.friend.CreateFriendRequest
import com.kohan.shared.armeria.exception.BusinessException
import com.kohan.shared.collection.user.UserProfileCollection
import com.linecorp.armeria.server.ServiceRequestContext
import io.netty.util.AttributeKey
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.assertThrows
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat

@ExtendWith(MockitoExtension::class)
class FriendServiceTest {

    private val friendRepository: FriendRepository = mock()
    private val userProfileRepository: UserProfileRepository = mock()
    private val friendService = FriendService(friendRepository, userProfileRepository)

    private lateinit var ctx: ServiceRequestContext
    private lateinit var userId: ObjectId
    private lateinit var otherUserId: ObjectId

    @BeforeEach
    fun setUp() {
        userId = ObjectId.get()
        otherUserId = ObjectId.get()

        ctx = mock()
        val userIdKey = AttributeKey.valueOf<String>("userId")
        whenever(ctx.attr(userIdKey)).thenReturn(userId.toHexString())
    }

    @Test
    fun `createFriend should save friend and return UserProfileDto`() {
        // Arrange
        val createFriendRequest = CreateFriendRequest(userId.toHexString(), otherUserId.toHexString())
        val userProfile = UserProfileCollection(
            userId = otherUserId,
            nickname = "nickname",
            profileImageFileId = "profileImageFileId"
        )
        whenever(userProfileRepository.findByUserId(otherUserId)).thenReturn(userProfile)

        // Act
        val result = friendService.createFriend(createFriendRequest, ctx)

        // Assert
        verify(friendRepository).save(argThat {
            this.fromUserId == userId && this.toUserId == otherUserId
        })
        assertThat(result.nickname, `is`(userProfile.nickname))
        assertThat(result.profileImageFileId, `is`(userProfile.profileImageFileId))
    }

    @Test
    fun `createFriend should throw exception when user not found`() {
        // Arrange
        val createFriendRequest = CreateFriendRequest(userId.toHexString(), ObjectId().toHexString())
        whenever(userProfileRepository.findByUserId(otherUserId)).thenReturn(null)

        // Act & Assert
        val exception = assertThrows<BusinessException> {
            friendService.createFriend(createFriendRequest, ctx)
        }
        assertEquals(UserErrorCode.NOT_FOUND_USER.businessException, exception)
    }


    @Test
    fun `should throw USER_NOT_REQ_USER when userId does not match reqUserId`() {
        // Arrange
        val invalidUserId = ObjectId().toHexString()
        val request = CreateFriendRequest(invalidUserId, otherUserId.toHexString())

        // Act & Assert
        val exception = assertThrows<BusinessException> {
            friendService.createFriend(request, ctx)
        }
        assertEquals(UserErrorCode.USER_NOT_REQ_USER.businessException, exception)
    }
}
