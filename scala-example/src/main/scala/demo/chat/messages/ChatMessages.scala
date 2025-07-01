package demo.chat.messages

import akka.actor.ActorRef
import demo.chat.models.*

/**
 * Base trait for all chat system messages
 */
sealed trait ChatSystemMessage

/**
 * User Management Messages
 */
case class CreateUser(username: String) extends ChatSystemMessage
case class ConnectUser(userId: String, connectionRef: ActorRef) extends ChatSystemMessage
case class DisconnectUser(userId: String) extends ChatSystemMessage
case class GetUser(userId: String) extends ChatSystemMessage
case object ListUsers extends ChatSystemMessage

/**
 * Room Management Messages
 */
case class CreateRoom(roomName: String) extends ChatSystemMessage
case class JoinRoom(userId: String, roomId: String) extends ChatSystemMessage
case class LeaveRoom(userId: String, roomId: String) extends ChatSystemMessage
case class GetRoom(roomId: String) extends ChatSystemMessage
case object ListRooms extends ChatSystemMessage

/**
 * Message Handling
 */
case class SendMessage(userId: String, roomId: String, content: String) extends ChatSystemMessage
case class GetRoomMessages(roomId: String, limit: Int = 50) extends ChatSystemMessage

/**
 * Response Messages
 */
sealed trait ChatSystemResponse

// User Responses
case class UserCreated(user: ChatUser) extends ChatSystemResponse
case class UserConnected(user: ChatUser) extends ChatSystemResponse
case class UserDisconnected(user: ChatUser) extends ChatSystemResponse
case class UserFound(user: ChatUser) extends ChatSystemResponse
case class UserNotFound(userId: String) extends ChatSystemResponse
case class UserList(users: List[ChatUser]) extends ChatSystemResponse

// Room Responses
case class RoomCreated(room: ChatRoom) extends ChatSystemResponse
case class RoomJoined(user: ChatUser, room: ChatRoom) extends ChatSystemResponse
case class RoomLeft(user: ChatUser, room: ChatRoom) extends ChatSystemResponse
case class RoomFound(room: ChatRoom) extends ChatSystemResponse
case class RoomNotFound(roomId: String) extends ChatSystemResponse
case class RoomList(rooms: List[ChatRoom]) extends ChatSystemResponse

// Message Responses
case class MessageSent(message: ChatMessage) extends ChatSystemResponse
case class RoomMessages(roomId: String, messages: List[ChatMessage]) extends ChatSystemResponse
case class UserNotInRoom(userId: String, roomId: String) extends ChatSystemResponse

/**
 * Notification Messages (sent to user connections)
 */
sealed trait ChatNotification

case class MessageReceived(message: ChatMessage, fromUser: ChatUser, inRoom: ChatRoom) extends ChatNotification
case class UserJoinedRoom(user: ChatUser, room: ChatRoom) extends ChatNotification
case class UserLeftRoom(user: ChatUser, room: ChatRoom) extends ChatNotification
case class RoomUpdated(room: ChatRoom) extends ChatNotification
