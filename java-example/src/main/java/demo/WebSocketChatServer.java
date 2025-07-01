package demo;

import java.util.Scanner;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

/**
 * WebSocket Chat Server - Java Implementation
 * Demonstrates Java 21 with Akka HTTP server setup
 */
public class WebSocketChatServer extends AllDirectives {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ChatWebSocketSystem");

        try {
            WebSocketChatServer server = new WebSocketChatServer();
            Route routes = server.createRoutes();

            CompletionStage<ServerBinding> bindingFuture = Http.get(system)
                .newServerAt("localhost", 8081)
                .bind(routes);

            bindingFuture.whenComplete((binding, failure) -> {
                if (binding != null) {
                    System.out.println("🚀 WebSocket Chat Server (Java) online at http://localhost:8081/");
                    System.out.println("🌐 Test page: http://localhost:8081/");
                    System.out.println("📊 Health check: http://localhost:8081/api/health");
                    System.out.println("Press RETURN to stop...");

                    Scanner scanner = new Scanner(System.in);
                    scanner.nextLine();

                    binding.unbind().whenComplete((unbound, unbindFailure) -> {
                        System.out.println("🛑 Server stopped");
                        system.terminate();
                    });

                } else {
                    System.err.println("❌ Failed to bind server: " + failure.getMessage());
                    system.terminate();
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Server startup failed: " + e.getMessage());
            system.terminate();
        }
    }

    private Route createRoutes() {
        return route(
            pathPrefix("api", () -> route(
                path("health", () ->
                    get(() -> complete(StatusCodes.OK, HttpEntities.create(ContentTypes.APPLICATION_JSON, "{\"status\":\"healthy\"}"))
                    )
                ),
                path("info", () ->
                    get(() -> complete(StatusCodes.OK, HttpEntities.create(ContentTypes.APPLICATION_JSON,
                        "{\"service\":\"WebSocket Chat Server - Java\",\"version\":\"1.0.0\"}"))
                    )
                )
            )),
            pathSingleSlash(() ->
                complete(StatusCodes.OK, HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, createTestPage()))
            )
        );
    }

    private String createTestPage() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>Java WebSocket Chat Server</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .status { background: #d4edda; color: #155724; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .java-badge { background: #f89406; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px; }
                    .endpoint { background: #e9ecef; padding: 10px; border-radius: 4px; margin: 10px 0; }
                    code { background: #f8f9fa; padding: 2px 6px; border-radius: 3px; }
                    pre { background: #f8f9fa; padding: 15px; border-radius: 5px; overflow-x: auto; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>☕ Java WebSocket Chat Server <span class="java-badge">Java 21</span></h1>

                    <div class="status">
                        ✅ Java HTTP Server Running Successfully on Port 8081
                    </div>

                    <h2>🚀 Implementation Features</h2>
                    <ul>
                        <li><strong>Java 21</strong> with modern language features</li>
                        <li><strong>Akka HTTP 10.2.10</strong> server framework</li>
                        <li><strong>Actor Model</strong> foundation with Akka</li>
                        <li><strong>Maven</strong> build system</li>
                        <li><strong>Sealed Classes</strong> and pattern matching ready</li>
                    </ul>

                    <h2>🔗 Available Endpoints</h2>
                    <div class="endpoint">
                        <strong>Health Check:</strong>
                        <a href="/api/health" target="_blank">GET /api/health</a>
                    </div>
                    <div class="endpoint">
                        <strong>Server Info:</strong>
                        <a href="/api/info" target="_blank">GET /api/info</a>
                    </div>

                    <h2>🏃‍♂️ Running the Server</h2>
                    <pre>cd java-example
mvn clean compile exec:java -Dexec.mainClass="demo.WebSocketChatServer"</pre>

                    <h2>📝 Next Steps</h2>
                    <p>This server demonstrates the foundation for a Java WebSocket chat system. To add full WebSocket chat functionality:</p>
                    <ol>
                        <li>Implement WebSocket routing with <code>handleWebSocketMessages</code></li>
                        <li>Create chat actor system for message handling</li>
                        <li>Add JSON message processing with Jackson</li>
                        <li>Build real-time message broadcasting</li>
                    </ol>

                    <p><strong>Note:</strong> This Java server runs on port 8081 to avoid conflicts with the Scala implementation (port 8080).</p>

                    <h2>🎯 Language Comparison</h2>
                    <p>Compare this Java implementation with the Scala version running on port 8080 to see how both languages approach the Actor Model and WebSocket handling.</p>
                </div>
            </body>
            </html>
            """;
    }
}
