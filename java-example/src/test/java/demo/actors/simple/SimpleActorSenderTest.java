package demo.actors.simple;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import demo.messages.DoubleMessage;
import demo.messages.IntMessage;
import demo.messages.StringMessage;

/**
 * Test class for SimpleActorSender using JUnit 5 and Akka TestKit
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleActorSenderTest {

    private ActorSystem system;

    @BeforeAll
    public void setup() {
        system = ActorSystem.create("TestSystem");
    }

    @AfterAll
    public void teardown() {
        system.terminate();
        system = null;
    }

    @Test
    public void testSimpleActorSenderProcessesIntMessage() {
        new TestKit(system) {{
            // Create the actor under test
            Props props = SimpleActorSender.props();
            ActorRef actorRef = system.actorOf(props, "testActorInt");

            // Send IntMessage - this is fire-and-forget, so we just verify it doesn't crash
            actorRef.tell(new IntMessage(42), getRef());

            // Wait a bit to allow processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // No response expected for fire-and-forget pattern
            expectNoMessage();
        }};
    }

    @Test
    public void testSimpleActorSenderProcessesStringMessage() {
        new TestKit(system) {{
            // Create the actor under test
            Props props = SimpleActorSender.props();
            ActorRef actorRef = system.actorOf(props, "testActorString");

            // Send StringMessage
            actorRef.tell(new StringMessage("Hello Test"), getRef());

            // Wait a bit to allow processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // No response expected for fire-and-forget pattern
            expectNoMessage();
        }};
    }

    @Test
    public void testSimpleActorSenderProcessesDoubleMessage() {
        new TestKit(system) {{
            // Create the actor under test
            Props props = SimpleActorSender.props();
            ActorRef actorRef = system.actorOf(props, "testActorDouble");

            // Send DoubleMessage
            actorRef.tell(new DoubleMessage(3.14159), getRef());

            // Wait a bit to allow processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // No response expected for fire-and-forget pattern
            expectNoMessage();
        }};
    }
}
