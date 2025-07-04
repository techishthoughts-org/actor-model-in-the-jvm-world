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
import demo.actors.MetricsActor.ActorMetrics;
import demo.actors.MetricsActor.GetAllMetrics;
import demo.actors.MetricsActor.GetMetrics;
import demo.actors.MetricsActor.IncrementErrorCounter;
import demo.actors.MetricsActor.IncrementMessageCounter;
import demo.actors.MetricsActor.RecordProcessingTime;
import demo.actors.MetricsActor.ResetAllMetrics;
import demo.actors.MetricsActor.ResetMetrics;
import demo.actors.MetricsActor.SystemMetrics;
import scala.concurrent.duration.Duration;

public class MetricsActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("MetricsActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testMetricsActorIncrementMessageCounter() {
        new TestKit(system) {{
            final ActorRef metricsActor = system.actorOf(Props.create(MetricsActor.class));

            // Test incrementing message counter
            metricsActor.tell(new IncrementMessageCounter("testActor"), getRef());

            // No response expected from this message type
            expectNoMessage(Duration.create(100, "milliseconds"));

            // Get metrics to verify counter was incremented
            metricsActor.tell(new GetMetrics("testActor"), getRef());
            ActorMetrics response = expectMsgClass(Duration.create(1, "second"), ActorMetrics.class);

            assertNotNull(response, "Metrics response should not be null");
            assertEquals("testActor", response.actorName(), "Should return metrics for correct actor");
            assertEquals(1L, response.messageCount(), "Message counter should be incremented");
        }};
    }

    @Test
    public void testMetricsActorRecordProcessingTime() {
        new TestKit(system) {{
            final ActorRef metricsActor = system.actorOf(Props.create(MetricsActor.class));

            // Test recording processing time
            metricsActor.tell(new RecordProcessingTime("testActor", 100L), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            // Get metrics to verify processing time was recorded
            metricsActor.tell(new GetMetrics("testActor"), getRef());
            ActorMetrics response = expectMsgClass(Duration.create(1, "second"), ActorMetrics.class);

            assertNotNull(response, "Metrics response should not be null");
            assertEquals("testActor", response.actorName(), "Should return metrics for correct actor");
            assertTrue(response.averageProcessingTime().isPresent(), "Average processing time should be present");
        }};
    }

    @Test
    public void testMetricsActorIncrementErrorCounter() {
        new TestKit(system) {{
            final ActorRef metricsActor = system.actorOf(Props.create(MetricsActor.class));

            // Test incrementing error counter
            metricsActor.tell(new IncrementErrorCounter("testActor"), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            // Get metrics to verify error counter was incremented
            metricsActor.tell(new GetMetrics("testActor"), getRef());
            ActorMetrics response = expectMsgClass(Duration.create(1, "second"), ActorMetrics.class);

            assertNotNull(response, "Metrics response should not be null");
            assertEquals("testActor", response.actorName(), "Should return metrics for correct actor");
            assertEquals(1L, response.errorCount(), "Error counter should be incremented");
        }};
    }

    @Test
    public void testMetricsActorGetAllMetrics() {
        new TestKit(system) {{
            final ActorRef metricsActor = system.actorOf(Props.create(MetricsActor.class));

            // Add some metrics for multiple actors
            metricsActor.tell(new IncrementMessageCounter("actor1"), getRef());
            metricsActor.tell(new IncrementMessageCounter("actor2"), getRef());
            metricsActor.tell(new IncrementErrorCounter("actor1"), getRef());
            expectNoMessage(Duration.create(200, "milliseconds"));

            // Get all metrics
            metricsActor.tell(new GetAllMetrics(), getRef());
            SystemMetrics response = expectMsgClass(Duration.create(1, "second"), SystemMetrics.class);

            assertNotNull(response, "System metrics response should not be null");
            assertNotNull(response.actorMetrics(), "Actor metrics list should not be null");
            assertEquals(2, response.actorMetrics().size(), "Should have metrics for 2 actors");
        }};
    }

    @Test
    public void testMetricsActorResetMetrics() {
        new TestKit(system) {{
            final ActorRef metricsActor = system.actorOf(Props.create(MetricsActor.class));

            // Add some metrics
            metricsActor.tell(new IncrementMessageCounter("testActor"), getRef());
            metricsActor.tell(new IncrementErrorCounter("testActor"), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            // Reset metrics for the actor
            metricsActor.tell(new ResetMetrics("testActor"), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            // Verify metrics were reset
            metricsActor.tell(new GetMetrics("testActor"), getRef());
            ActorMetrics response = expectMsgClass(Duration.create(1, "second"), ActorMetrics.class);

            assertNotNull(response, "Metrics response should not be null");
            assertEquals(0L, response.messageCount(), "Message counter should be reset");
            assertEquals(0L, response.errorCount(), "Error counter should be reset");
        }};
    }

    @Test
    public void testMetricsActorResetAllMetrics() {
        new TestKit(system) {{
            final ActorRef metricsActor = system.actorOf(Props.create(MetricsActor.class));

            // Add metrics for multiple actors
            metricsActor.tell(new IncrementMessageCounter("actor1"), getRef());
            metricsActor.tell(new IncrementMessageCounter("actor2"), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            // Reset all metrics
            metricsActor.tell(new ResetAllMetrics(), getRef());
            expectNoMessage(Duration.create(100, "milliseconds"));

            // Verify all metrics were reset
            metricsActor.tell(new GetAllMetrics(), getRef());
            SystemMetrics response = expectMsgClass(Duration.create(1, "second"), SystemMetrics.class);

            assertNotNull(response, "System metrics response should not be null");
            assertTrue(response.actorMetrics().isEmpty(), "Should have no metrics after reset");
        }};
    }

    @Test
    public void testMetricsActorGetMetricsForNonexistentActor() {
        new TestKit(system) {{
            final ActorRef metricsActor = system.actorOf(Props.create(MetricsActor.class));

            // Get metrics for an actor that has no recorded metrics
            metricsActor.tell(new GetMetrics("nonexistent"), getRef());
            ActorMetrics response = expectMsgClass(Duration.create(1, "second"), ActorMetrics.class);

            assertNotNull(response, "Metrics response should not be null");
            assertEquals("nonexistent", response.actorName(), "Should return correct actor name");
            assertEquals(0L, response.messageCount(), "Message count should be 0");
            assertEquals(0L, response.errorCount(), "Error count should be 0");
            assertFalse(response.averageProcessingTime().isPresent(), "Average processing time should not be present");
        }};
    }
}
