package demo.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import demo.actors.HealthCheckActor.ActorHealthStatus;
import demo.actors.HealthCheckActor.ActorRegistered;
import demo.actors.HealthCheckActor.ActorUnregistered;
import demo.actors.HealthCheckActor.GetActorHealth;
import demo.actors.HealthCheckActor.GetHealthStatus;
import demo.actors.HealthCheckActor.MarkActorHealthy;
import demo.actors.HealthCheckActor.MarkActorUnhealthy;
import demo.actors.HealthCheckActor.RegisterActor;
import demo.actors.HealthCheckActor.SystemHealthStatus;
import demo.actors.HealthCheckActor.UnregisterActor;
import scala.concurrent.duration.Duration;

public class HealthCheckActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("HealthCheckActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testHealthCheckActorGetSystemHealth() {
        new TestKit(system) {{
            final ActorRef healthActor = system.actorOf(Props.create(HealthCheckActor.class));

            // Test getting system health status
            healthActor.tell(new GetHealthStatus(), getRef());

            SystemHealthStatus response = expectMsgClass(Duration.create(1, "second"), SystemHealthStatus.class);
            assertNotNull(response, "Health status response should not be null");
            assertNotNull(response.status(), "Status map should not be null");
        }};
    }

    @Test
    public void testHealthCheckActorRegisterActor() {
        new TestKit(system) {{
            final ActorRef healthActor = system.actorOf(Props.create(HealthCheckActor.class));
            final ActorRef testActor = system.actorOf(Props.empty(), "testActor");

            // Register an actor for monitoring
            healthActor.tell(new RegisterActor("testActor", testActor), getRef());

            ActorRegistered response = expectMsgClass(Duration.create(1, "second"), ActorRegistered.class);
            assertNotNull(response, "Registration response should not be null");
            assertEquals("testActor", response.name(), "Should confirm registration of correct actor");
        }};
    }

    @Test
    public void testHealthCheckActorUnregisterActor() {
        new TestKit(system) {{
            final ActorRef healthActor = system.actorOf(Props.create(HealthCheckActor.class));
            final ActorRef testActor = system.actorOf(Props.empty(), "testActor2");

            // Register then unregister an actor
            healthActor.tell(new RegisterActor("testActor2", testActor), getRef());
            expectMsgClass(Duration.create(1, "second"), ActorRegistered.class);

            healthActor.tell(new UnregisterActor("testActor2"), getRef());

            ActorUnregistered response = expectMsgClass(Duration.create(1, "second"), ActorUnregistered.class);
            assertNotNull(response, "Unregistration response should not be null");
            assertEquals("testActor2", response.name(), "Should confirm unregistration of correct actor");
        }};
    }

    @Test
    public void testHealthCheckActorGetActorHealth() {
        new TestKit(system) {{
            final ActorRef healthActor = system.actorOf(Props.create(HealthCheckActor.class));
            final ActorRef testActor = system.actorOf(Props.empty(), "testActor3");

            // Register an actor and get its health
            healthActor.tell(new RegisterActor("testActor3", testActor), getRef());
            expectMsgClass(Duration.create(1, "second"), ActorRegistered.class);

            healthActor.tell(new GetActorHealth("testActor3"), getRef());

            ActorHealthStatus response = expectMsgClass(Duration.create(1, "second"), ActorHealthStatus.class);
            assertNotNull(response, "Actor health response should not be null");
            assertEquals("testActor3", response.name(), "Should return health for correct actor");
            assertTrue(response.status().isPresent(), "Health status should be present");
        }};
    }

    @Test
    public void testHealthCheckActorMarkActorUnhealthy() {
        new TestKit(system) {{
            final ActorRef healthActor = system.actorOf(Props.create(HealthCheckActor.class));
            final ActorRef testActor = system.actorOf(Props.empty(), "testActor4");

            // Register an actor, mark it unhealthy, then check
            healthActor.tell(new RegisterActor("testActor4", testActor), getRef());
            expectMsgClass(Duration.create(1, "second"), ActorRegistered.class);

            healthActor.tell(new MarkActorUnhealthy("testActor4", "Test failure"), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            healthActor.tell(new GetActorHealth("testActor4"), getRef());

            ActorHealthStatus response = expectMsgClass(Duration.create(1, "second"), ActorHealthStatus.class);
            assertNotNull(response, "Actor health response should not be null");
            assertTrue(response.status().isPresent(), "Health status should be present");
            assertFalse(response.status().get().isHealthy(), "Actor should be marked as unhealthy");
        }};
    }

    @Test
    public void testHealthCheckActorMarkActorHealthy() {
        new TestKit(system) {{
            final ActorRef healthActor = system.actorOf(Props.create(HealthCheckActor.class));
            final ActorRef testActor = system.actorOf(Props.empty(), "testActor5");

            // Register an actor, mark it unhealthy, then healthy again
            healthActor.tell(new RegisterActor("testActor5", testActor), getRef());
            expectMsgClass(Duration.create(1, "second"), ActorRegistered.class);

            healthActor.tell(new MarkActorUnhealthy("testActor5", "Test failure"), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            healthActor.tell(new MarkActorHealthy("testActor5"), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            healthActor.tell(new GetActorHealth("testActor5"), getRef());

            ActorHealthStatus response = expectMsgClass(Duration.create(1, "second"), ActorHealthStatus.class);
            assertNotNull(response, "Actor health response should not be null");
            assertTrue(response.status().isPresent(), "Health status should be present");
            assertTrue(response.status().get().isHealthy(), "Actor should be marked as healthy again");
        }};
    }

    @Test
    public void testHealthCheckActorGetHealthForNonexistentActor() {
        new TestKit(system) {{
            final ActorRef healthActor = system.actorOf(Props.create(HealthCheckActor.class));

            // Try to get health for an actor that was never registered
            healthActor.tell(new GetActorHealth("nonexistent"), getRef());

            ActorHealthStatus response = expectMsgClass(Duration.create(1, "second"), ActorHealthStatus.class);
            assertNotNull(response, "Actor health response should not be null");
            assertEquals("nonexistent", response.name(), "Should return correct actor name");
            assertFalse(response.status().isPresent(), "Health status should not be present for unregistered actor");
        }};
    }
}
