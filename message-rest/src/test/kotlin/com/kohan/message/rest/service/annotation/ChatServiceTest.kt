package com.kohan.message.rest.service

import com.kohan.message.rest.repository.chat.room.ChatRoomRepository
import com.kohan.message.rest.repository.message.MessageRepository
import com.kohan.message.rest.service.annotation.ChatService
import com.kohan.message.rest.vo.chat.room.CreateChatRoom
import com.kohan.message.rest.vo.chat.room.InitMessage
import com.kohan.message.rest.vo.chat.room.InviteChatRoom
import com.kohan.message.rest.vo.chat.room.LeaveChatRoom
import com.kohan.message.rest.vo.chat.room.UpdateChatRoomName
import com.kohan.message.rest.vo.message.AddReaction
import com.kohan.message.rest.vo.message.DeleteMessage
import com.kohan.message.rest.vo.message.DeleteReaction
import com.kohan.message.rest.vo.message.SendMessage
import com.kohan.shared.armeria.exception.BusinessException
import com.kohan.shared.collection.chatRoom.ChatRoomCollection
import com.kohan.shared.collection.message.MessageCollection
import com.kohan.shared.collection.chatRoom.ChatRoomType
import com.kohan.shared.collection.message.item.ReactionType
import com.linecorp.armeria.server.ServiceRequestContext
import io.netty.util.AttributeKey
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@EnableAutoConfiguration
@AutoConfigureDataMongo
@DirtiesContext
class ChatServiceTest
    @Autowired
    constructor(
        private val chatService: ChatService,
        private val chatRoomRepository: ChatRoomRepository,
        private val messageRepository: MessageRepository,
    ) {
        private lateinit var ctx: ServiceRequestContext
        private lateinit var userId: ObjectId
        private lateinit var otherUserId: ObjectId
        private lateinit var chatRoom: ChatRoomCollection
        private lateinit var chatRoomWithoutTester: ChatRoomCollection

        @BeforeEach
        fun setUp() {
            userId = ObjectId.get()
            otherUserId = ObjectId.get()

            ctx = Mockito.mock(ServiceRequestContext::class.java)
            val userIdKey = AttributeKey.valueOf<String>("userId")
            whenever(ctx.attr(userIdKey)).thenReturn(userId.toHexString())

            chatRoom =
                chatRoomRepository.save(
                    ChatRoomCollection(
                        "room",
                        ObjectId.get(),
                        ChatRoomType.GROUP,
                        mutableListOf(userId, ObjectId.get(), ObjectId.get()),
                    ),
                )

            chatRoomWithoutTester =
                chatRoomRepository.save(
                    ChatRoomCollection(
                        "NoTesterRoom",
                        ObjectId.get(),
                        ChatRoomType.GROUP,
                        mutableListOf(ObjectId.get()),
                    ),
                )
        }

        @Test
        fun `test getChatRoomListWithLatestMessage`() {
            messageRepository.save(
                MessageCollection(
                    "hi",
                    userId,
                    chatRoom._id,
                    reactions = mutableListOf(),
                    readUsers = mutableListOf(userId, otherUserId),
                ),
            )
            messageRepository.save(
                MessageCollection(
                    "hello",
                    otherUserId,
                    chatRoom._id,
                    reactions = mutableListOf(),
                    readUsers = mutableListOf(otherUserId),
                ),
            )
            val latestMessage =
                messageRepository.save(
                    MessageCollection(
                        "bye",
                        otherUserId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(otherUserId),
                    ),
                )

            val chatRoomList = chatService.getChatRoomList(0, 10, ctx)

            assertTrue(!chatRoomList.isEmpty)
            assertEquals(1, chatRoomList.numberOfElements)
            assertEquals(chatRoom._id.toHexString(), chatRoomList.get().toList()[0].id)
            assertEquals(
                2,
                chatRoomList
                    .get()
                    .toList()[0]
                    .latestMessageInfo.unreadCount,
            )
            assertEquals(
                latestMessage.content,
                chatRoomList
                    .get()
                    .toList()[0]
                    .latestMessageInfo.content,
            )
        }

        @Test
        fun `test getChatRoom`() {
            val chatRoom =
                chatRoomRepository.save(
                    ChatRoomCollection(
                        "room1",
                        ObjectId.get(),
                        ChatRoomType.GROUP,
                        mutableListOf(userId, ObjectId.get(), ObjectId.get()),
                    ),
                )
            val foundChatRoom = chatService.getChatRoom(chatRoom._id.toHexString(), ctx)

            assertEquals(chatRoom._id.toHexString(), foundChatRoom.id)
            assertEquals(chatRoom.name, foundChatRoom.name)
            assertEquals(chatRoom.type, foundChatRoom.type)
            assertEquals(chatRoom.userList.map { item -> item.toHexString() }, foundChatRoom.userList)
        }

        @Test
        fun `test getChatRoomMessages`() {
            val readMessageByPhoneAndComputer =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        userId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )
            val readMessageByPhone =
                messageRepository.save(
                    MessageCollection(
                        "hello",
                        otherUserId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )
            val lastMessage =
                messageRepository.save(
                    MessageCollection(
                        "bye",
                        otherUserId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(otherUserId),
                    ),
                )

            val messagesFromPhone =
                chatService.getChatRoomMessages(
                    chatRoom._id.toHexString(),
                    readMessageByPhone._id.toHexString(),
                    ctx,
                )

            val messageFromComputer =
                chatService.getChatRoomMessages(
                    chatRoom._id.toHexString(),
                    readMessageByPhoneAndComputer._id.toHexString(),
                    ctx,
                )

            // assert Phone
            assertEquals(lastMessage._id.toHexString(), messagesFromPhone[0].id)
            // assert Computer
            assertEquals(readMessageByPhone._id.toHexString(), messageFromComputer[0].id)
        }

        @Test
        fun `test getChatRoomMessages with not in chat room`() {
            assertThrows<BusinessException> {
                chatService.getChatRoomMessages(chatRoomWithoutTester._id.toHexString(), ObjectId.get().toHexString(), ctx)
            }
        }

        @Test
        fun `test createChatRoom`() {
            val createChatRoom =
                CreateChatRoom(
                    "",
                    ObjectId.get().toHexString(),
                    ChatRoomType.PERSONAL.toString(),
                    listOf(userId.toHexString(), ObjectId.get().toHexString()),
                    InitMessage("hi", userId.toHexString()),
                )

            val chatRoom = chatService.createChatRoom(createChatRoom, ctx)

            assertEquals(chatRoom.name, "")
            assertEquals(chatRoom.type, ChatRoomType.PERSONAL)
            assertEquals(chatRoom.userList.size, 2)

            // todo kafka
        }

        @Test
        fun `test createChatRoom with request user not in chat room`() {
            val createChatRoom =
                CreateChatRoom(
                    "",
                    ObjectId.get().toHexString(),
                    ChatRoomType.GROUP.toString(),
                    listOf(ObjectId.get().toHexString()),
                    InitMessage("hi", userId.toHexString()),
                )

            assertThrows<BusinessException> {
                chatService.createChatRoom(createChatRoom, ctx)
            }
        }

        @Test
        fun `test updateChatRoomName`() {
            val updateChatRoom =
                UpdateChatRoomName(
                    chatRoom._id.toHexString(),
                    "newName",
                )

            val updatedChatRoom = chatService.updateChatRoomName(updateChatRoom, ctx)

            assertEquals(updateChatRoom.name, updatedChatRoom.name)
        }

//    @Test
//    fun `test updateChatRoomProfileImage`() {
//        val updateChatRoomProfileImage = UpdateChatRoomProfileImage(
//            chatRoom._id.toHexString(),
//
//        )
//
//        val updatedChatRoom = chatService.updateChatRoomName(updateChatRoomProfileImage, ctx)
//
//        assertEquals(updateChatRoomProfileImage.name, updatedChatRoom.name)
//    }

        @Test
        fun `test inviteChatRoom`() {
            val invitedUser = ObjectId.get()

            val inviteChatRoom =
                InviteChatRoom(
                    chatRoom._id.toHexString(),
                    invitedUser.toHexString(),
                )

            val updatedChatRoom = chatService.inviteChatRoom(inviteChatRoom, ctx)

            assertEquals(chatRoom.userList.size + 1, updatedChatRoom.userList.size)
            assertTrue(updatedChatRoom.userList.contains(invitedUser.toHexString()))
        }

        @Test
        fun `test inviteChatRoom with not group chat room`() {
            val invitedUser = ObjectId.get()

            val chatRoom =
                chatRoomRepository.save(
                    ChatRoomCollection(
                        "room1",
                        ObjectId.get(),
                        ChatRoomType.PERSONAL,
                        mutableListOf(userId, ObjectId.get()),
                    ),
                )

            val inviteChatRoom =
                InviteChatRoom(
                    chatRoom._id.toHexString(),
                    invitedUser.toHexString(),
                )

            assertThrows<BusinessException> {
                chatService.inviteChatRoom(inviteChatRoom, ctx)
            }
        }

        @Test
        fun `test inviteChatRoom with already in chat room`() {
            val inviteChatRoom =
                InviteChatRoom(
                    chatRoom._id.toHexString(),
                    userId.toHexString(),
                )

            assertThrows<BusinessException> {
                chatService.inviteChatRoom(inviteChatRoom, ctx)
            }
        }

        @Test
        fun `test leaveChatRoom`() {
            val leaveChatRoom =
                LeaveChatRoom(
                    chatRoom._id.toHexString(),
                    userId.toHexString(),
                )

            val updatedChatRoom = chatService.leaveChatRoom(leaveChatRoom, ctx)

            assertEquals(chatRoom.userList.size - 1, updatedChatRoom.userList.size)
            assertTrue(!updatedChatRoom.userList.contains(userId.toHexString()))
        }

        @Test
        fun `test leaveChatRoom when only one user`() {
            val chatRoom =
                chatRoomRepository.save(
                    ChatRoomCollection(
                        "room1",
                        ObjectId.get(),
                        ChatRoomType.PERSONAL,
                        mutableListOf(userId),
                    ),
                )

            val leaveChatRoom =
                LeaveChatRoom(
                    chatRoom._id.toHexString(),
                    userId.toHexString(),
                )
            chatService.leaveChatRoom(leaveChatRoom, ctx)

            val leavedChatRoom = chatRoomRepository.findById(chatRoom._id)
            assertTrue(leavedChatRoom.isPresent)
            assertTrue(leavedChatRoom.get().deleteAt != null)
        }

        @Test
        fun `test leaveChatRoom with user not leaving user`() {
            val leaveChatRoom =
                LeaveChatRoom(
                    chatRoom._id.toHexString(),
                    otherUserId.toHexString(),
                )

            assertThrows<BusinessException> {
                chatService.leaveChatRoom(leaveChatRoom, ctx)
            }
        }

        @Test
        fun `test sendMessage`() {
            val sendMessage =
                SendMessage(
                    "hi",
                    userId.toHexString(),
                    chatRoom._id.toHexString(),
                )

            val message = chatService.sendMessage(sendMessage, ctx)

            assertEquals(sendMessage.content, message.content)
            assertEquals(sendMessage.sender, message.sender)
            assertEquals(sendMessage.chatRoomId, message.chatRoomId)
        }

        @Test
        fun `test deleteMessage`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        userId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )

            chatService.deleteMessage(DeleteMessage(message._id.toHexString()), ctx)

            assertTrue(messageRepository.findById(message._id).isPresent)
            assertTrue(messageRepository.findById(message._id).get().deleteAt != null)
        }

        @Test
        fun `test deleteMessage with not found message`() {
            assertThrows<BusinessException> {
                chatService.deleteMessage(DeleteMessage(ObjectId.get().toHexString()), ctx)
            }
        }

        @Test
        fun `test deleteMessage with user not sender`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        otherUserId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )

            assertThrows<BusinessException> {
                chatService.deleteMessage(DeleteMessage(message._id.toHexString()), ctx)
            }
        }

        @Test
        fun `test deleteChatRoom with message not deletable`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        userId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )
            message.createAt = LocalDateTime.now().minusMinutes(10)
            messageRepository.save(message)

            assertThrows<BusinessException> {
                chatService.deleteMessage(DeleteMessage(chatRoom._id.toHexString()), ctx)
            }
        }

        @Test
        fun `test getValidChatRoomWithUserId with not found chat room by public fun`() {
            assertThrows<BusinessException> {
                chatService.getChatRoom(ObjectId.get().toHexString(), ctx)
            }
        }

        @Test
        fun `test getValidChatRoomWithUserId with not in chat room by private fun`() {
            assertThrows<BusinessException> {
                chatService.getChatRoom(chatRoomWithoutTester._id.toHexString(), ctx)
            }
        }

        @Test
        fun `test addReaction`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        userId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )

            val reaction =
                AddReaction(
                    message._id.toHexString(),
                    "SAD",
                    userId.toHexString(),
                )

            val updatedMessage = chatService.addReaction(reaction, ctx)

            assertEquals(1, updatedMessage.reactions.size)
            assertEquals(ReactionType.valueOf(reaction.type), updatedMessage.reactions[0].type)
            assertEquals(reaction.sender, updatedMessage.reactions[0].senderUser)
        }

        @Test
        fun `test addReaction with not found message`() {
            val reaction =
                AddReaction(
                    ObjectId.get().toHexString(),
                    "SAD",
                    userId.toHexString(),
                )

            assertThrows<BusinessException> {
                chatService.addReaction(reaction, ctx)
            }
        }

        @Test
        fun `test add Reaction with user not sender`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        otherUserId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )

            val reaction =
                AddReaction(
                    message._id.toHexString(),
                    "SAD",
                    otherUserId.toHexString(),
                )

            assertThrows<BusinessException> {
                chatService.addReaction(reaction, ctx)
            }
        }

        @Test
        fun `test add other Reaction`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        userId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )

            val reaction =
                AddReaction(
                    message._id.toHexString(),
                    "SAD",
                    userId.toHexString(),
                )

            chatService.addReaction(reaction, ctx)

            val otherReaction =
                AddReaction(
                    message._id.toHexString(),
                    "LIKE",
                    userId.toHexString(),
                )

            val updatedMessage = chatService.addReaction(otherReaction, ctx)

            assertEquals(1, updatedMessage.reactions.size)
            assertEquals(ReactionType.valueOf(otherReaction.type), updatedMessage.reactions[0].type)
            assertEquals(otherReaction.sender, updatedMessage.reactions[0].senderUser)
        }

        @Test
        fun `test deleteReaction`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        userId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )

            val reaction =
                AddReaction(
                    message._id.toHexString(),
                    "SAD",
                    userId.toHexString(),
                )

            val deleteReaction =
                DeleteReaction(
                    message._id.toHexString(),
                    userId.toHexString(),
                )

            chatService.addReaction(reaction, ctx)

            val deletedReaction = chatService.deleteReaction(deleteReaction, ctx)

            assertEquals(0, deletedReaction.reactions.size)
        }

        @Test
        fun `test deleteReaction with not found message`() {
            val deleteReaction =
                DeleteReaction(
                    ObjectId.get().toHexString(),
                    userId.toHexString(),
                )

            assertThrows<BusinessException> {
                chatService.deleteReaction(deleteReaction, ctx)
            }
        }

        @Test
        fun `test deleteReaction with user not sender`() {
            val message =
                messageRepository.save(
                    MessageCollection(
                        "hi",
                        otherUserId,
                        chatRoom._id,
                        reactions = mutableListOf(),
                        readUsers = mutableListOf(userId, otherUserId),
                    ),
                )

            val deleteReaction =
                DeleteReaction(
                    message._id.toHexString(),
                    otherUserId.toHexString(),
                )

            assertThrows<BusinessException> {
                chatService.deleteReaction(deleteReaction, ctx)
            }
        }
    }
