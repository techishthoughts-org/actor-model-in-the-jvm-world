package demo.http.models

import akka.actor.ActorRef
import spray.json._
import DefaultJsonProtocol._

/**
 * WebSocket User representation
 */
case class WebSocketUser(
  username: String,
  userId: String,
  connectionRef: ActorRef,
  joinedRooms: Set[String] = Set.empty
)

/**
 * WebSocket Message Types
 */
sealed trait ChatWebSocketMessage

case class JoinRoomMessage(roomName: String) extends ChatWebSocketMessage
case class SendChatMessage(roomName: String, content: String) extends ChatWebSocketMessage
case object ListRoomsMessage extends ChatWebSocketMessage
case object ListUsersMessage extends ChatWebSocketMessage
case class UnknownMessage(raw: String) extends ChatWebSocketMessage

/**
 * WebSocket Response Types
 */
sealed trait ChatWebSocketResponse

case class JoinedRoomResponse(roomName: String, message: String) extends ChatWebSocketResponse
case class MessageSentResponse(roomName: String, username: String, content: String, timestamp: Long) extends ChatWebSocketResponse
case class MessageReceivedResponse(roomName: String, username: String, content: String, timestamp: Long) extends ChatWebSocketResponse
case class RoomListResponse(rooms: List[String]) extends ChatWebSocketResponse
case class UserListResponse(users: List[String]) extends ChatWebSocketResponse
case class ErrorResponse(error: String) extends ChatWebSocketResponse
case class UserJoinedResponse(roomName: String, username: String) extends ChatWebSocketResponse
case class UserLeftResponse(roomName: String, username: String) extends ChatWebSocketResponse

/**
 * JSON Protocol for WebSocket Messages
 */
object ChatWebSocketJsonProtocol extends DefaultJsonProtocol {

  // Request Messages
  implicit object ChatWebSocketMessageFormat extends RootJsonFormat[ChatWebSocketMessage] {
    def write(msg: ChatWebSocketMessage): JsValue = msg match {
      case JoinRoomMessage(roomName) =>
        JsObject(
          "type" -> JsString("joinRoom"),
          "roomName" -> JsString(roomName)
        )
      case SendChatMessage(roomName, content) =>
        JsObject(
          "type" -> JsString("sendMessage"),
          "roomName" -> JsString(roomName),
          "content" -> JsString(content)
        )
      case ListRoomsMessage =>
        JsObject("type" -> JsString("listRooms"))
      case ListUsersMessage =>
        JsObject("type" -> JsString("listUsers"))
      case UnknownMessage(raw) =>
        JsObject(
          "type" -> JsString("unknown"),
          "raw" -> JsString(raw)
        )
    }

    def read(json: JsValue): ChatWebSocketMessage = {
      json.asJsObject.getFields("type") match {
        case Seq(JsString("joinRoom")) =>
          json.asJsObject.getFields("roomName") match {
            case Seq(JsString(roomName)) => JoinRoomMessage(roomName)
            case _ => throw DeserializationException("Expected roomName field")
          }
        case Seq(JsString("sendMessage")) =>
          json.asJsObject.getFields("roomName", "content") match {
            case Seq(JsString(roomName), JsString(content)) => SendChatMessage(roomName, content)
            case _ => throw DeserializationException("Expected roomName and content fields")
          }
        case Seq(JsString("listRooms")) => ListRoomsMessage
        case Seq(JsString("listUsers")) => ListUsersMessage
        case _ => UnknownMessage(json.toString)
      }
    }
  }

  // Response Messages
  implicit object ChatWebSocketResponseFormat extends RootJsonFormat[ChatWebSocketResponse] {
    def write(response: ChatWebSocketResponse): JsValue = response match {
      case JoinedRoomResponse(roomName, message) =>
        JsObject(
          "type" -> JsString("joinedRoom"),
          "roomName" -> JsString(roomName),
          "message" -> JsString(message)
        )
      case MessageSentResponse(roomName, username, content, timestamp) =>
        JsObject(
          "type" -> JsString("messageSent"),
          "roomName" -> JsString(roomName),
          "username" -> JsString(username),
          "content" -> JsString(content),
          "timestamp" -> JsNumber(timestamp)
        )
      case MessageReceivedResponse(roomName, username, content, timestamp) =>
        JsObject(
          "type" -> JsString("messageReceived"),
          "roomName" -> JsString(roomName),
          "username" -> JsString(username),
          "content" -> JsString(content),
          "timestamp" -> JsNumber(timestamp)
        )
      case RoomListResponse(rooms) =>
        JsObject(
          "type" -> JsString("roomList"),
          "rooms" -> JsArray(rooms.map(JsString(_)).toVector)
        )
      case UserListResponse(users) =>
        JsObject(
          "type" -> JsString("userList"),
          "users" -> JsArray(users.map(JsString(_)).toVector)
        )
      case ErrorResponse(error) =>
        JsObject(
          "type" -> JsString("error"),
          "error" -> JsString(error)
        )
      case UserJoinedResponse(roomName, username) =>
        JsObject(
          "type" -> JsString("userJoined"),
          "roomName" -> JsString(roomName),
          "username" -> JsString(username)
        )
      case UserLeftResponse(roomName, username) =>
        JsObject(
          "type" -> JsString("userLeft"),
          "roomName" -> JsString(roomName),
          "username" -> JsString(username)
        )
    }

    def read(json: JsValue): ChatWebSocketResponse = {
      // We typically don't need to read responses, but implementing for completeness
      json.asJsObject.getFields("type") match {
        case Seq(JsString("error")) =>
          json.asJsObject.getFields("error") match {
            case Seq(JsString(error)) => ErrorResponse(error)
            case _ => throw DeserializationException("Expected error field")
          }
        case _ => throw DeserializationException(s"Unknown response type: ${json}")
      }
    }
  }
}
