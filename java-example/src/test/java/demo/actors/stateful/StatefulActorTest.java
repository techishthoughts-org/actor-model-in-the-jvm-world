package demo.actors.stateful;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import demo.messages.stateful.AskForHelpStateful;
import demo.messages.stateful.GetStatusStateful;
import demo.messages.stateful.HelpResponseStateful;
import demo.messages.stateful.InMeetingStateful;
import demo.messages.stateful.JoinMeetingStateful;
import demo.messages.stateful.SendMeetingLinkStateful;
import demo.messages.stateful.WorkerStatusStateful;
import scala.concurrent.duration.Duration;

public class StatefulActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("StatefulActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testStatefulActorInitialStatus() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.IDLE));

            // Test initial status
            statefulActor.tell(new GetStatusStateful(), getRef());

            WorkerStatusStateful response = expectMsgClass(Duration.create(1, "second"), WorkerStatusStateful.class);
            assertNotNull(response, "Status response should not be null");
            assertEquals(WorkerStatusStateful.IDLE, response, "Actor should start with IDLE status");
        }};
    }

    @Test
    public void testStatefulActorHelpRequest() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.IDLE));

            // Request help when idle
            statefulActor.tell(new AskForHelpStateful("Need assistance"), getRef());

            HelpResponseStateful response = expectMsgClass(Duration.create(1, "second"), HelpResponseStateful.class);
            assertNotNull(response, "Help response should not be null");
            assertTrue(response.value().contains("help"), "Should provide help when available");
        }};
    }

    @Test
    public void testStatefulActorHelpRequestFromAvailable() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.AVAILABLE));

            // Request help when available
            statefulActor.tell(new AskForHelpStateful("Need assistance"), getRef());

            HelpResponseStateful response = expectMsgClass(Duration.create(1, "second"), HelpResponseStateful.class);
            assertNotNull(response, "Help response should not be null");
            assertTrue(response.value().contains("help"), "Should provide help when available");
        }};
    }

    @Test
    public void testStatefulActorHelpResponseFlow() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.IDLE));

            // Send help response to trigger meeting flow
            statefulActor.tell(new HelpResponseStateful("I can help!"), getRef());

            SendMeetingLinkStateful response = expectMsgClass(Duration.create(1, "second"), SendMeetingLinkStateful.class);
            assertNotNull(response, "Meeting link response should not be null");
            assertTrue(response.value().contains("meeting"), "Should send meeting link");
        }};
    }

    @Test
    public void testStatefulActorMeetingLinkFlow() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.AVAILABLE));

            // Send meeting link
            statefulActor.tell(new SendMeetingLinkStateful("http://meeting.link"), getRef());

            JoinMeetingStateful response = expectMsgClass(Duration.create(1, "second"), JoinMeetingStateful.class);
            assertNotNull(response, "Join meeting response should not be null");
            assertTrue(response.value().contains("join"), "Should agree to join meeting");
        }};
    }

    @Test
    public void testStatefulActorJoinMeetingFromAvailable() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.AVAILABLE));

            // Join meeting when available
            statefulActor.tell(new JoinMeetingStateful("Let's meet"), getRef());

            JoinMeetingStateful response = expectMsgClass(Duration.create(1, "second"), JoinMeetingStateful.class);
            assertNotNull(response, "Join meeting response should not be null");
        }};
    }

    @Test
    public void testStatefulActorJoinMeetingWhenInMeeting() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.IN_MEETING));

            // Try to join meeting when already in meeting
            statefulActor.tell(new JoinMeetingStateful("Another meeting"), getRef());

            InMeetingStateful response = expectMsgClass(Duration.create(1, "second"), InMeetingStateful.class);
            assertNotNull(response, "In meeting response should not be null");
            assertTrue(response.value().contains("meeting"), "Should indicate already in meeting");
        }};
    }

    @Test
    public void testStatefulActorStatusCheck() {
        new TestKit(system) {{
            final ActorRef statefulActor = system.actorOf(
                StatefulActor.props("TestWorker", WorkerStatusStateful.WAITING_RESPONSE));

            // Check status
            statefulActor.tell(new GetStatusStateful(), getRef());

            WorkerStatusStateful status = expectMsgClass(Duration.create(1, "second"), WorkerStatusStateful.class);
            assertEquals(WorkerStatusStateful.WAITING_RESPONSE, status, "Should return current status");
        }};
    }
}
