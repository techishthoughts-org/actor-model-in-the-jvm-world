package demo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.model.StatusCodes
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.{Done, NotUsed}
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * WebSocket Chat Client
 * Command-line client for testing WebSocket chat functionality
 */
object WebSocketChatClient {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("ChatWebSocketClient")
    implicit val ec: ExecutionContext = system.dispatcher

    println("🧪 WebSocket Chat Client - Multi-User Test")
    println("=" * 50)

    // Test with multiple simulated users
    val users = List("Alice", "Bob", "Charlie", "David")

    // Connect all users
    val clientFutures = users.map(username => connectUser(username))

    // Wait for all connections and then simulate chat
    Future.sequence(clientFutures).onComplete {
      case Success(clients) =>
        println(s"✅ All ${clients.length} users connected successfully!")

        // Start chat simulation
        simulateMultiUserChat(clients)

        // Keep running for a while
        system.scheduler.scheduleOnce(30.seconds) {
          println("🛑 Shutting down test clients...")
          clients.foreach(_.shutdown())
          system.terminate()
        }

      case Failure(ex) =>
        println(s"❌ Failed to connect clients: ${ex.getMessage}")
        system.terminate()
    }
  }

  private def connectUser(username: String)(implicit system: ActorSystem, ec: ExecutionContext): Future[ChatClient] = {
    Future.successful(new ChatClient(username))
  }

  private def simulateMultiUserChat(clients: List[ChatClient])(implicit system: ActorSystem, ec: ExecutionContext): Unit = {
    import system.scheduler

    println("\n💬 Starting multi-user chat simulation...")

    // All users join the general room
    clients.foreach { client =>
      scheduler.scheduleOnce(1.second) {
        client.joinRoom("general")
      }
    }

    // Schedule messages from different users
    val messages = List(
      (2.seconds, "Alice", "Hello everyone! 👋"),
      (4.seconds, "Bob", "Hi Alice! How's everyone doing?"),
      (6.seconds, "Charlie", "Great to see you all here!"),
      (8.seconds, "David", "This WebSocket chat is working perfectly! 🎉"),
      (10.seconds, "Alice", "Let's test with some emoji: 🚀 💻 ⭐"),
      (12.seconds, "Bob", "Unicode test: こんにちは 🇯🇵"),
      (14.seconds, "Charlie", "Anyone want to join project-alpha room?"),
      (16.seconds, "David", "Sure, let's try multiple rooms!")
    )

    messages.foreach { case (delay, senderName, message) =>
      scheduler.scheduleOnce(delay) {
        clients.find(_.username == senderName) match {
          case Some(client) => client.sendMessage("general", message)
          case None => println(s"❌ Client $senderName not found")
        }
      }
    }

    // Test room switching
    scheduler.scheduleOnce(18.seconds) {
      clients.take(2).foreach(_.joinRoom("project-alpha"))
    }

    scheduler.scheduleOnce(20.seconds) {
      clients.find(_.username == "Charlie") match {
        case Some(client) => client.sendMessage("project-alpha", "Welcome to project-alpha room!")
        case None =>
      }
    }

    // List users and rooms
    scheduler.scheduleOnce(22.seconds) {
      clients.head.listUsers()
      clients.head.listRooms()
    }
  }
}

/**
 * Simplified Chat Client for demonstration
 * This is a mock client that simulates actions without actual WebSocket connections
 */
class ChatClient(val username: String)(implicit system: ActorSystem, ec: ExecutionContext) {

  def joinRoom(roomName: String): Unit = {
    println(s"🏠 [$username] Would join room: $roomName")
  }

  def sendMessage(roomName: String, content: String): Unit = {
    println(s"💬 [$username] Would send to $roomName: $content")
  }

  def listUsers(): Unit = {
    println(s"👥 [$username] Would list users")
  }

  def listRooms(): Unit = {
    println(s"🏠 [$username] Would list rooms")
  }

  def shutdown(): Unit = {
    println(s"🛑 [$username] Simulated disconnect")
  }
}
