package demo.http.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.util.Timeout
import demo.chat.messages._
import demo.http.models._
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * WebSocket Chat Routes
 * Provides WebSocket endpoints for real-time chat functionality
 */
class ChatRoutes(chatSystem: ActorRef)(implicit system: ActorSystem, ec: ExecutionContext) {

  implicit val timeout: Timeout = Timeout(5.seconds)

  val routes: Route = {
    pathPrefix("chat") {
      concat(
        // WebSocket endpoint for chat
        path("ws" / Segment) { username =>
          get {
            handleWebSocketMessages(createChatFlow(username))
          }
        },
        // Static file serving for chat interface
        pathPrefix("static") {
          getFromResourceDirectory("chat-ui")
        },
        // Chat interface
        pathSingleSlash {
          getFromResource("chat-ui/index.html")
        }
      )
    }
  }

  private def createChatFlow(username: String): Flow[Message, Message, Any] = {
    // Create user and get connection
    val userFuture = chatSystem ? CreateUser(username)

    Flow[Message]
      .collect {
        case TextMessage.Strict(text) => parseMessage(text)
      }
      .via(Flow.fromFunction { msg =>
        handleChatMessage(msg, username)
      })
      .mapAsync(1)(identity)
      .map(response => TextMessage(response))
      .via(addHeartbeat)
  }

  private def addHeartbeat: Flow[TextMessage, TextMessage, Any] = {
    val heartbeatSource = Source.tick(30.seconds, 30.seconds, TextMessage("""{"type":"heartbeat"}"""))
    Flow[TextMessage].merge(heartbeatSource)
  }

  private def parseMessage(text: String): ChatWebSocketMessage = {
    try {
      import demo.http.models.ChatWebSocketJsonProtocol._
      text.parseJson.convertTo[ChatWebSocketMessage]
    } catch {
      case _: Exception =>
        UnknownMessage(text)
    }
  }

  private def handleChatMessage(msg: ChatWebSocketMessage, username: String): Future[String] = {
    msg match {
      case JoinRoomMessage(roomName) =>
        for {
          // First ensure user exists
          userResponse <- (chatSystem ? CreateUser(username)).mapTo[ChatSystemResponse]
          userId = userResponse match {
            case UserCreated(user) => user.userId
            case _ => username // fallback
          }
          // Create or join room
          roomResponse <- (chatSystem ? CreateRoom(roomName)).mapTo[ChatSystemResponse].recover {
            case _ => RoomNotFound(roomName) // Room might already exist
          }
          // Join the room
          joinResponse <- (chatSystem ? JoinRoom(userId, roomName)).mapTo[ChatSystemResponse]
        } yield {
          s"""{"type":"joinedRoom","roomName":"$roomName","message":"$username joined $roomName"}"""
        }

      case SendChatMessage(roomName, content) =>
        for {
          userResponse <- (chatSystem ? CreateUser(username)).mapTo[ChatSystemResponse]
          userId = userResponse match {
            case UserCreated(user) => user.userId
            case _ => username // fallback
          }
          messageResponse <- (chatSystem ? SendMessage(userId, roomName, content)).mapTo[ChatSystemResponse]
        } yield {
          s"""{"type":"messageSent","roomName":"$roomName","username":"$username","content":"$content","timestamp":${System.currentTimeMillis()}}"""
        }

      case ListRoomsMessage =>
        (chatSystem ? ListRooms).mapTo[ChatSystemResponse].map {
          case RoomList(rooms) =>
            val roomNames = rooms.map(_.name).map(name => s""""$name"""").mkString("[", ",", "]")
            s"""{"type":"roomList","rooms":$roomNames}"""
          case _ =>
            """{"type":"error","message":"Failed to list rooms"}"""
        }

      case ListUsersMessage =>
        (chatSystem ? ListUsers).mapTo[ChatSystemResponse].map {
          case UserList(users) =>
            val userNames = users.map(_.username).map(name => s""""$name"""").mkString("[", ",", "]")
            s"""{"type":"userList","users":$userNames}"""
          case _ =>
            """{"type":"error","message":"Failed to list users"}"""
        }

      case UnknownMessage(text) =>
        Future.successful(s"""{"type":"error","message":"Unknown message: $text"}""")
    }
  }
}
