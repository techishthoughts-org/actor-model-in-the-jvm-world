package demo

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import demo.chat.ChatSystem
import demo.http.routes.ChatRoutes

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import scala.util.{Failure, Success}

/**
 * WebSocket Chat Server
 * HTTP server with WebSocket support for real-time multi-user chat testing
 */
object WebSocketChatServer {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("ChatWebSocketSystem")
    implicit val ec: ExecutionContext = system.dispatcher

    // Create chat system actor
    val chatSystem: ActorRef = system.actorOf(Props[ChatSystem](), "chat-system")

    // Create routes
    val chatRoutes = new ChatRoutes(chatSystem)

    val routes: Route = concat(
      chatRoutes.routes,
      // API routes for testing
      pathPrefix("api") {
        concat(
          path("health") {
            get {
              complete(HttpEntity(ContentTypes.`application/json`, """{"status":"healthy"}"""))
            }
          },
          path("info") {
            get {
              complete(HttpEntity(ContentTypes.`application/json`,
                """{"service":"WebSocket Chat Server","version":"1.0.0"}"""))
            }
          }
        )
      },
      // Serve simple HTML page for testing
      pathSingleSlash {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, createTestPage))
      }
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)

    bindingFuture.onComplete {
      case Success(binding) =>
        println(s"🚀 WebSocket Chat Server online at http://localhost:8080/")
        println(s"📡 WebSocket endpoint: ws://localhost:8080/chat/ws/{username}")
        println(s"🌐 Test page: http://localhost:8080/")
        println(s"📊 Health check: http://localhost:8080/api/health")
        println(s"💬 Ready for multi-user chat testing!")
        println(s"Press RETURN to stop...")

        // Wait for user input to stop
        StdIn.readLine()

        binding
          .unbind()
          .onComplete { _ =>
            println("🛑 Server stopped")
            system.terminate()
          }

      case Failure(ex) =>
        println(s"❌ Failed to bind server: ${ex.getMessage}")
        system.terminate()
    }
  }

  private def createTestPage: String = {
    """<!DOCTYPE html>
      |<html lang="en">
      |<head>
      |    <meta charset="UTF-8">
      |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
      |    <title>WebSocket Chat Test</title>
      |    <style>
      |        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
      |        .container { max-width: 800px; margin: 0 auto; }
      |        .chat-container { background: white; border-radius: 8px; padding: 20px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
      |        .connection-status { padding: 10px; border-radius: 4px; margin: 10px 0; }
      |        .connected { background-color: #d4edda; color: #155724; }
      |        .disconnected { background-color: #f8d7da; color: #721c24; }
      |        .messages { height: 300px; overflow-y: auto; border: 1px solid #ddd; padding: 10px; margin: 10px 0; background: #f9f9f9; }
      |        .message { margin: 5px 0; padding: 8px; border-radius: 4px; }
      |        .message.sent { background: #e3f2fd; text-align: right; }
      |        .message.received { background: #f1f8e9; }
      |        .message.system { background: #fff3e0; font-style: italic; }
      |        input, button { padding: 8px; margin: 5px; border: 1px solid #ddd; border-radius: 4px; }
      |        button { background: #007bff; color: white; cursor: pointer; }
      |        button:hover { background: #0056b3; }
      |        button:disabled { background: #ccc; cursor: not-allowed; }
      |        .input-group { display: flex; margin: 10px 0; }
      |        .input-group input { flex: 1; }
      |    </style>
      |</head>
      |<body>
      |    <div class="container">
      |        <h1>🚀 WebSocket Chat Test</h1>
      |        <p>Test the chat system with multiple users by opening this page in multiple browser tabs/windows.</p>
      |
      |        <div class="chat-container">
      |            <div class="input-group">
      |                <input type="text" id="username" placeholder="Enter username" value="User1">
      |                <button id="connect">Connect</button>
      |                <button id="disconnect" disabled>Disconnect</button>
      |            </div>
      |
      |            <div id="status" class="connection-status disconnected">
      |                Status: Disconnected
      |            </div>
      |
      |            <div class="input-group">
      |                <input type="text" id="roomName" placeholder="Room name" value="general">
      |                <button id="joinRoom" disabled>Join Room</button>
      |                <button id="listRooms" disabled>List Rooms</button>
      |                <button id="listUsers" disabled>List Users</button>
      |            </div>
      |
      |            <div id="messages" class="messages"></div>
      |
      |            <div class="input-group">
      |                <input type="text" id="messageInput" placeholder="Type your message..." disabled>
      |                <button id="sendMessage" disabled>Send</button>
      |            </div>
      |        </div>
      |
      |        <div class="chat-container">
      |            <h3>📋 Instructions for Multi-User Testing</h3>
      |            <ol>
      |                <li>Open this page in multiple browser tabs/windows</li>
      |                <li>Set different usernames in each tab (e.g., Alice, Bob, Charlie)</li>
      |                <li>Click "Connect" in each tab</li>
      |                <li>Join the same room (e.g., "general") in all tabs</li>
      |                <li>Start sending messages from different tabs</li>
      |                <li>Observe real-time message delivery across all connected users</li>
      |            </ol>
      |        </div>
      |    </div>
      |
      |    <script>
      |        let socket = null;
      |        let username = '';
      |        let currentRoom = '';
      |
      |        const elements = {
      |            username: document.getElementById('username'),
      |            connect: document.getElementById('connect'),
      |            disconnect: document.getElementById('disconnect'),
      |            status: document.getElementById('status'),
      |            roomName: document.getElementById('roomName'),
      |            joinRoom: document.getElementById('joinRoom'),
      |            listRooms: document.getElementById('listRooms'),
      |            listUsers: document.getElementById('listUsers'),
      |            messages: document.getElementById('messages'),
      |            messageInput: document.getElementById('messageInput'),
      |            sendMessage: document.getElementById('sendMessage')
      |        };
      |
      |        function addMessage(message, type = 'system') {
      |            const messageEl = document.createElement('div');
      |            messageEl.className = `message ${type}`;
      |            messageEl.textContent = message;
      |            elements.messages.appendChild(messageEl);
      |            elements.messages.scrollTop = elements.messages.scrollHeight;
      |        }
      |
      |        function updateStatus(connected) {
      |            elements.status.textContent = connected ? `Status: Connected as ${username}` : 'Status: Disconnected';
      |            elements.status.className = `connection-status ${connected ? 'connected' : 'disconnected'}`;
      |
      |            elements.connect.disabled = connected;
      |            elements.disconnect.disabled = !connected;
      |            elements.joinRoom.disabled = !connected;
      |            elements.listRooms.disabled = !connected;
      |            elements.listUsers.disabled = !connected;
      |            elements.messageInput.disabled = !connected;
      |            elements.sendMessage.disabled = !connected;
      |        }
      |
      |        function connect() {
      |            username = elements.username.value.trim();
      |            if (!username) {
      |                alert('Please enter a username');
      |                return;
      |            }
      |
      |            socket = new WebSocket(`ws://localhost:8080/chat/ws/${encodeURIComponent(username)}`);
      |
      |            socket.onopen = function() {
      |                updateStatus(true);
      |                addMessage(`Connected as ${username}`, 'system');
      |            };
      |
      |            socket.onmessage = function(event) {
      |                try {
      |                    const data = JSON.parse(event.data);
      |                    handleMessage(data);
      |                } catch (e) {
      |                    addMessage(`Raw message: ${event.data}`, 'system');
      |                }
      |            };
      |
      |            socket.onclose = function() {
      |                updateStatus(false);
      |                addMessage('Disconnected', 'system');
      |            };
      |
      |            socket.onerror = function(error) {
      |                addMessage(`Error: ${error}`, 'system');
      |            };
      |        }
      |
      |        function disconnect() {
      |            if (socket) {
      |                socket.close();
      |                socket = null;
      |            }
      |        }
      |
      |        function handleMessage(data) {
      |            switch (data.type) {
      |                case 'joinedRoom':
      |                    currentRoom = data.roomName;
      |                    addMessage(`Joined room: ${data.roomName}`, 'system');
      |                    break;
      |                case 'messageSent':
      |                    addMessage(`You: ${data.content}`, 'sent');
      |                    break;
      |                case 'messageReceived':
      |                    addMessage(`${data.username}: ${data.content}`, 'received');
      |                    break;
      |                case 'roomList':
      |                    addMessage(`Rooms: ${data.rooms.join(', ')}`, 'system');
      |                    break;
      |                case 'userList':
      |                    addMessage(`Users: ${data.users.join(', ')}`, 'system');
      |                    break;
      |                case 'error':
      |                    addMessage(`Error: ${data.error}`, 'system');
      |                    break;
      |                case 'heartbeat':
      |                    // Ignore heartbeat messages
      |                    break;
      |                default:
      |                    addMessage(`Unknown message: ${JSON.stringify(data)}`, 'system');
      |            }
      |        }
      |
      |        function joinRoom() {
      |            const roomName = elements.roomName.value.trim();
      |            if (!roomName || !socket) return;
      |
      |            socket.send(JSON.stringify({
      |                type: 'joinRoom',
      |                roomName: roomName
      |            }));
      |        }
      |
      |        function sendMessage() {
      |            const message = elements.messageInput.value.trim();
      |            if (!message || !socket || !currentRoom) return;
      |
      |            socket.send(JSON.stringify({
      |                type: 'sendMessage',
      |                roomName: currentRoom,
      |                content: message
      |            }));
      |
      |            elements.messageInput.value = '';
      |        }
      |
      |        function listRooms() {
      |            if (!socket) return;
      |            socket.send(JSON.stringify({type: 'listRooms'}));
      |        }
      |
      |        function listUsers() {
      |            if (!socket) return;
      |            socket.send(JSON.stringify({type: 'listUsers'}));
      |        }
      |
      |        // Event listeners
      |        elements.connect.addEventListener('click', connect);
      |        elements.disconnect.addEventListener('click', disconnect);
      |        elements.joinRoom.addEventListener('click', joinRoom);
      |        elements.listRooms.addEventListener('click', listRooms);
      |        elements.listUsers.addEventListener('click', listUsers);
      |        elements.sendMessage.addEventListener('click', sendMessage);
      |
      |        elements.messageInput.addEventListener('keypress', function(e) {
      |            if (e.key === 'Enter') {
      |                sendMessage();
      |            }
      |        });
      |
      |        elements.username.addEventListener('keypress', function(e) {
      |            if (e.key === 'Enter') {
      |                connect();
      |            }
      |        });
      |
      |        elements.roomName.addEventListener('keypress', function(e) {
      |            if (e.key === 'Enter') {
      |                joinRoom();
      |            }
      |        });
      |    </script>
      |</body>
      |</html>""".stripMargin
  }
}
