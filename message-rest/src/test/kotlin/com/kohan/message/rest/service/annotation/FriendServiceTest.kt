package com.kohan.message.rest.service.annotation

import com.kohan.message.rest.exception.code.UserErrorCode
import com.kohan.message.rest.repository.friend.FriendRepository
import com.kohan.message.rest.repository.user.profile.UserProfileRepository
import com.kohan.message.rest.vo.friend.CreateFriendRequest
import com.kohan.message.rest.vo.friend.FriendStatusVo
import com.kohan.shared.armeria.exception.BusinessException
import com.kohan.shared.collection.friend.FriendCollection
import com.kohan.shared.collection.friend.FriendStatus
import com.kohan.shared.collection.user.UserProfileCollection
import com.linecorp.armeria.server.ServiceRequestContext
import io.netty.util.AttributeKey
import org.bson.types.ObjectId
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import kotlin.test.assertEquals

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
        val userProfile =
            UserProfileCollection(
                userId = otherUserId,
                nickname = "nickname",
                profileImageFileId = "profileImageFileId",
            )
        whenever(userProfileRepository.findById(otherUserId)).thenReturn(Optional.of(userProfile))

        // Act
        val result = friendService.createFriend(createFriendRequest, ctx)

        // Assert
        verify(friendRepository).save(
            argThat {
                this.fromUserId == userId && this.toUserId == otherUserId
            },
        )
        assertThat(result.nickname, `is`(userProfile.nickname))
        assertThat(result.profileImageFileId, `is`(userProfile.profileImageFileId))
    }

    @Test
    fun `createFriend should throw exception when user not found`() {
        // Arrange
        val createFriendRequest = CreateFriendRequest(userId.toHexString(), ObjectId().toHexString())
        whenever(userProfileRepository.findByUserId(otherUserId)).thenReturn(null)

        // Act & Assert
        val exception =
            assertThrows<BusinessException> {
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
        val exception =
            assertThrows<BusinessException> {
                friendService.createFriend(request, ctx)
            }
        assertEquals(UserErrorCode.USER_NOT_REQ_USER.businessException, exception)
    }

    @Test
    fun `getFriends should return sorted friends by nickname`() {
        val friends =
            listOf(
                FriendCollection(userId, ObjectId(), FriendStatus.NORMAL),
                FriendCollection(userId, ObjectId(), FriendStatus.FAVORITE),
            )
        val toUserIds = friends.map { it.toUserId }
        val userProfiles =
            listOf(
                UserProfileCollection(toUserIds[1], "Bob", "1234"),
                UserProfileCollection(toUserIds[0], "Alice", "1234"),
            )

        whenever(
            friendRepository.findAllByDeleteAtIsNullAndFromUserIdAndStatusIn(
                eq(userId),
                eq(listOf(FriendStatus.NORMAL, FriendStatus.FAVORITE)),
            ),
        ).thenReturn(friends)

        whenever(userProfileRepository.findAllByIdInAndDeleteAtIsNullOrderByNicknameAsc(toUserIds))
            .thenReturn(userProfiles.sortedBy { it.nickname })

        val result = friendService.getFriends(userId.toHexString(), ctx)

        assertEquals(2, result.size)
        assertEquals("Alice", result[0].nickname)
        assertEquals("Bob", result[1].nickname)

        verify(friendRepository).findAllByDeleteAtIsNullAndFromUserIdAndStatusIn(
            eq(userId),
            eq(listOf(FriendStatus.NORMAL, FriendStatus.FAVORITE)),
        )
        verify(userProfileRepository).findAllByIdInAndDeleteAtIsNullOrderByNicknameAsc(toUserIds)
    }

    @Test
    fun `getFriends should throw exception if user validation fails`() {
        val exception =
            assertThrows<BusinessException> {
                friendService.getFriends(otherUserId.toHexString(), ctx)
            }

        assertEquals(UserErrorCode.USER_NOT_REQ_USER.businessException, exception)
    }

    @Test
    fun `updateFriendStatus should return changed friend`() {
        val friendStatusVo = FriendStatusVo(ObjectId().toString(), FriendStatus.BLOCK.toString())
        val friend = FriendCollection(userId, otherUserId, FriendStatus.NORMAL)
        whenever(friendRepository.findById(ObjectId(friendStatusVo.id))).thenReturn(Optional.of(friend))

        friend.status = FriendStatus.BLOCK
        whenever(friendRepository.save(friend)).thenReturn(friend)

        val result = friendService.updateFriendStatus(friendStatusVo, ctx)

        assertEquals(FriendStatus.BLOCK, result.status)

        verify(friendRepository).findById(ObjectId(friendStatusVo.id))
        verify(friendRepository).save(friend)
    }

    @Test
    fun `updateFriendStatus should throw exception if user validation fails`() {
        val friendStatusVo = FriendStatusVo(ObjectId().toString(), FriendStatus.BLOCK.toString())
        val friend = FriendCollection(otherUserId, userId, FriendStatus.NORMAL)
        whenever(friendRepository.findById(ObjectId(friendStatusVo.id))).thenReturn(Optional.of(friend))

        val exception =
            assertThrows<BusinessException> {
                friendService.updateFriendStatus(friendStatusVo, ctx)
            }

        assertEquals(UserErrorCode.USER_NOT_REQ_USER.businessException, exception)
    }
}
