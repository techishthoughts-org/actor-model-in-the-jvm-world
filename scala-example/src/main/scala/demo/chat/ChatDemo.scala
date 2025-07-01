package demo.chat

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import demo.chat.messages.*
import demo.chat.models.*

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.util.Failure
import scala.util.Success

/**
 * Chat System Demonstration
 * Shows Slack-like multi-user chat functionality using actors
 */
class ChatDemo {

  implicit val timeout: Timeout = Timeout(5.seconds)

  /**
   * Demonstrate complete chat system workflow
   */
  def demonstrateChatSystem(system: ActorSystem): Unit = {
    implicit val ec: ExecutionContext = system.dispatcher

    println("🗣️  Chat System Demo Starting...")

    // Create chat system actor
    val chatSystem: ActorRef = system.actorOf(ChatSystem.props(), "demo-chat-system")

    try {
      // Step 1: Create users
      println("\n👥 Creating Users...")
      val alice = createUser(chatSystem, "Alice")
      val bob = createUser(chatSystem, "Bob")
      val charlie = createUser(chatSystem, "Charlie")

      // Step 2: Create rooms
      println("\n🏠 Creating Chat Rooms...")
      val generalRoom = createRoom(chatSystem, "General")
      val projectRoom = createRoom(chatSystem, "Project-Alpha")

      // Step 3: Users join rooms
      println("\n🚪 Users Joining Rooms...")
      joinRoom(chatSystem, alice.userId, generalRoom.roomId)
      joinRoom(chatSystem, bob.userId, generalRoom.roomId)
      joinRoom(chatSystem, charlie.userId, projectRoom.roomId)
      joinRoom(chatSystem, alice.userId, projectRoom.roomId)

      // Step 4: Send messages
      println("\n💬 Sending Messages...")
      sendMessage(chatSystem, alice.userId, generalRoom.roomId, "Hello everyone! 👋")
      sendMessage(chatSystem, bob.userId, generalRoom.roomId, "Hi Alice! How's the project going?")
      sendMessage(chatSystem, alice.userId, projectRoom.roomId, "Project Alpha is on track!")
      sendMessage(chatSystem, charlie.userId, projectRoom.roomId, "Great news! 🎉")

      // Step 5: List current state
      println("\n📋 Current System State...")
      listUsers(chatSystem)
      listRooms(chatSystem)

      println("\n✅ Chat System Demo Completed Successfully!")

    } catch {
      case exception: Exception =>
        println(s"❌ Chat Demo failed: ${exception.getMessage}")
        exception.printStackTrace()
    }
  }

  private def createUser(chatSystem: ActorRef, username: String)(implicit ec: ExecutionContext): ChatUser = {
    val future = chatSystem ? CreateUser(username)
    Await.result(future, 3.seconds) match {
      case UserCreated(user) =>
        println(s"   ✅ User created: ${user.username} (ID: ${user.userId})")
        user
      case other =>
        throw new RuntimeException(s"Failed to create user $username: $other")
    }
  }

  private def createRoom(chatSystem: ActorRef, roomName: String)(implicit ec: ExecutionContext): ChatRoom = {
    val future = chatSystem ? CreateRoom(roomName)
    Await.result(future, 3.seconds) match {
      case RoomCreated(room) =>
        println(s"   ✅ Room created: ${room.name} (ID: ${room.roomId})")
        room
      case other =>
        throw new RuntimeException(s"Failed to create room $roomName: $other")
    }
  }

  private def joinRoom(chatSystem: ActorRef, userId: String, roomId: String)(implicit ec: ExecutionContext): Unit = {
    val future = chatSystem ? JoinRoom(userId, roomId)
    Await.result(future, 3.seconds) match {
      case RoomJoined(user, room) =>
        println(s"   ✅ ${user.username} joined room ${room.name}")
      case UserNotFound(id) =>
        println(s"   ❌ User not found: $id")
      case RoomNotFound(id) =>
        println(s"   ❌ Room not found: $id")
      case other =>
        println(s"   ❌ Failed to join room: $other")
    }
  }

  private def sendMessage(chatSystem: ActorRef, userId: String, roomId: String, content: String)(implicit ec: ExecutionContext): Unit = {
    val future = chatSystem ? SendMessage(userId, roomId, content)
    Await.result(future, 3.seconds) match {
      case MessageSent(message) =>
        println(s"   ✅ Message sent: $content")
      case UserNotFound(id) =>
        println(s"   ❌ User not found: $id")
      case RoomNotFound(id) =>
        println(s"   ❌ Room not found: $id")
      case UserNotInRoom(uId, rId) =>
        println(s"   ❌ User $uId is not in room $rId")
      case other =>
        println(s"   ❌ Failed to send message: $other")
    }
  }

  private def listUsers(chatSystem: ActorRef)(implicit ec: ExecutionContext): Unit = {
    val future = chatSystem ? ListUsers
    Await.result(future, 3.seconds) match {
      case UserList(users) =>
        println(s"   📊 Total Users: ${users.length}")
        users.foreach { user =>
          val status = if (user.connected) "🟢 Online" else "🔴 Offline"
          println(s"      ${user.username} ($status)")
        }
      case other =>
        println(s"   ❌ Failed to list users: $other")
    }
  }

  private def listRooms(chatSystem: ActorRef)(implicit ec: ExecutionContext): Unit = {
    val future = chatSystem ? ListRooms
    Await.result(future, 3.seconds) match {
      case RoomList(rooms) =>
        println(s"   📊 Total Rooms: ${rooms.length}")
        rooms.foreach { room =>
          println(s"      ${room.name} (${room.participants.length} participants)")
        }
      case other =>
        println(s"   ❌ Failed to list rooms: $other")
    }
  }
}
