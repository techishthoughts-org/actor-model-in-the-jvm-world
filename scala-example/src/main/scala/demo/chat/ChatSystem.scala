package demo.chat

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import demo.chat.messages.*
import demo.chat.models.*

import scala.collection.mutable
import scala.concurrent.duration.*

/**
 * Chat System Manager Actor
 * Manages chat rooms, users, and message routing for a Slack-like experience
 */
class ChatSystem extends Actor with ActorLogging {

  import ChatSystem._

  private val users = mutable.Map[String, ChatUser]()
  private val rooms = mutable.Map[String, ChatRoom]()
  private val userConnections = mutable.Map[String, ActorRef]()
  private val roomParticipants = mutable.Map[String, mutable.Set[String]]().withDefaultValue(mutable.Set())

  override def preStart(): Unit = {
    log.info("ChatSystem started")
    // Create default general room
    val generalRoom = ChatRoom(
      roomId = "general",
      name = "General",
      participants = List.empty,
      created = System.currentTimeMillis()
    )
    rooms += "general" -> generalRoom
    roomParticipants += "general" -> mutable.Set()
  }

  override def receive: Receive = {
    // User Management
    case CreateUser(username) =>
      val userId = generateUserId()
      val user = ChatUser(
        userId = userId,
        username = username,
        connected = false,
        lastSeen = System.currentTimeMillis()
      )
      users += userId -> user
      sender() ! UserCreated(user)
      log.info(s"User created: $username ($userId)")

    case ConnectUser(userId, connectionRef) =>
      users.get(userId) match {
        case Some(user) =>
          val updatedUser = user.copy(connected = true, lastSeen = System.currentTimeMillis())
          users += userId -> updatedUser
          userConnections += userId -> connectionRef
          sender() ! UserConnected(updatedUser)
          log.info(s"User connected: ${user.username} ($userId)")
        case None =>
          sender() ! UserNotFound(userId)
      }

    case DisconnectUser(userId) =>
      users.get(userId) match {
        case Some(user) =>
          val updatedUser = user.copy(connected = false, lastSeen = System.currentTimeMillis())
          users += userId -> updatedUser
          userConnections.remove(userId)
          // Remove from all rooms
          roomParticipants.foreach { case (roomId, participants) =>
            participants.remove(userId)
          }
          sender() ! UserDisconnected(updatedUser)
          log.info(s"User disconnected: ${user.username} ($userId)")
        case None =>
          sender() ! UserNotFound(userId)
      }

    case GetUser(userId) =>
      users.get(userId) match {
        case Some(user) => sender() ! UserFound(user)
        case None => sender() ! UserNotFound(userId)
      }

    case ListUsers =>
      sender() ! UserList(users.values.toList)

    // Room Management
    case CreateRoom(roomName) =>
      val roomId = generateRoomId()
      val room = ChatRoom(
        roomId = roomId,
        name = roomName,
        participants = List.empty,
        created = System.currentTimeMillis()
      )
      rooms += roomId -> room
      roomParticipants += roomId -> mutable.Set()
      sender() ! RoomCreated(room)
      log.info(s"Room created: $roomName ($roomId)")

    case JoinRoom(userId, roomId) =>
      (users.get(userId), rooms.get(roomId)) match {
        case (Some(user), Some(room)) =>
          val participants = roomParticipants(roomId)
          participants += userId
          val updatedRoom = room.copy(participants = participants.toList)
          rooms += roomId -> updatedRoom

          // Notify all participants about new user
          participants.foreach { participantId =>
            userConnections.get(participantId).foreach { connectionRef =>
              connectionRef ! UserJoinedRoom(user, updatedRoom)
            }
          }

          sender() ! RoomJoined(user, updatedRoom)
          log.info(s"User ${user.username} joined room ${room.name}")

        case (None, _) => sender() ! UserNotFound(userId)
        case (_, None) => sender() ! RoomNotFound(roomId)
      }

    case LeaveRoom(userId, roomId) =>
      (users.get(userId), rooms.get(roomId)) match {
        case (Some(user), Some(room)) =>
          val participants = roomParticipants(roomId)
          participants.remove(userId)
          val updatedRoom = room.copy(participants = participants.toList)
          rooms += roomId -> updatedRoom

          // Notify remaining participants
          participants.foreach { participantId =>
            userConnections.get(participantId).foreach { connectionRef =>
              connectionRef ! UserLeftRoom(user, updatedRoom)
            }
          }

          sender() ! RoomLeft(user, updatedRoom)
          log.info(s"User ${user.username} left room ${room.name}")

        case (None, _) => sender() ! UserNotFound(userId)
        case (_, None) => sender() ! RoomNotFound(roomId)
      }

    case GetRoom(roomId) =>
      rooms.get(roomId) match {
        case Some(room) => sender() ! RoomFound(room)
        case None => sender() ! RoomNotFound(roomId)
      }

    case ListRooms =>
      sender() ! RoomList(rooms.values.toList)

    // Message Handling
    case SendMessage(userId, roomId, content) =>
      (users.get(userId), rooms.get(roomId)) match {
        case (Some(user), Some(room)) =>
          val participants = roomParticipants(roomId)
          if (participants.contains(userId)) {
            val message = ChatMessage(
              messageId = generateMessageId(),
              roomId = roomId,
              fromUserId = userId,
              content = content,
              timestamp = System.currentTimeMillis()
            )

            // Send message to all participants
            participants.foreach { participantId =>
              userConnections.get(participantId).foreach { connectionRef =>
                connectionRef ! MessageReceived(message, user, room)
              }
            }

            sender() ! MessageSent(message)
            log.info(s"Message sent from ${user.username} to room ${room.name}: $content")
          } else {
            sender() ! UserNotInRoom(userId, roomId)
          }

        case (None, _) => sender() ! UserNotFound(userId)
        case (_, None) => sender() ! RoomNotFound(roomId)
      }

    case GetRoomMessages(roomId, limit) =>
      // In a real system, you would store and retrieve messages from a database
      // For demo purposes, we'll return an empty list
      sender() ! RoomMessages(roomId, List.empty)
  }

  private def generateUserId(): String = s"user_${System.currentTimeMillis()}_${scala.util.Random.nextInt(1000)}"
  private def generateRoomId(): String = s"room_${System.currentTimeMillis()}_${scala.util.Random.nextInt(1000)}"
  private def generateMessageId(): String = s"msg_${System.currentTimeMillis()}_${scala.util.Random.nextInt(1000)}"
}

object ChatSystem {
  def props(): Props = Props(new ChatSystem())
}

/**
 * User Connection Actor
 * Manages individual user's connection and message routing
 */
class UserConnection(userId: String, chatSystem: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  override def preStart(): Unit = {
    log.info(s"UserConnection started for user: $userId")
    chatSystem ! ConnectUser(userId, self)
  }

  override def postStop(): Unit = {
    log.info(s"UserConnection stopped for user: $userId")
    chatSystem ! DisconnectUser(userId)
  }

  override def receive: Receive = {
    case message: ChatSystemMessage =>
      // Forward all chat system messages to the chat system
      chatSystem ! message

    case notification: ChatNotification =>
      // Handle notifications from chat system
      log.info(s"User $userId received notification: $notification")
      // In a real system, you would send this to the user's WebSocket connection

    case unknown =>
      log.warning(s"UserConnection received unknown message: $unknown")
  }
}

object UserConnection {
  def props(userId: String, chatSystem: ActorRef): Props =
    Props(new UserConnection(userId, chatSystem))
}
