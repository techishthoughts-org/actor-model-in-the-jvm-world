package demo.chat.models

/**
 * Chat User model representing a user in the chat system
 */
case class ChatUser(
  userId: String,
  username: String,
  connected: Boolean,
  lastSeen: Long
)

/**
 * Chat Room model representing a chat room/channel
 */
case class ChatRoom(
  roomId: String,
  name: String,
  participants: List[String], // List of user IDs
  created: Long
)

/**
 * Chat Message model representing a message in a room
 */
case class ChatMessage(
  messageId: String,
  roomId: String,
  fromUserId: String,
  content: String,
  timestamp: Long
)

/**
 * Request models for HTTP API
 */
case class CreateUserRequest(username: String)
case class CreateRoomRequest(roomName: String)
case class JoinRoomRequest(userId: String, roomId: String)
case class SendMessageRequest(userId: String, roomId: String, content: String)
