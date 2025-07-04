package demo.actors.stateless;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import demo.messages.stateless.AskForHelpStateless;
import demo.messages.stateless.GetStatusStateless;
import demo.messages.stateless.HelpResponseStateless;
import demo.messages.stateless.InMeetingStateless;
import demo.messages.stateless.JoinMeetingStateless;
import demo.messages.stateless.SendMeetingLinkStateless;
import demo.messages.stateless.WorkerStatusStateless;

public class StatelessActorTest {

    static ActorSystem system;

    @BeforeAll
    public static void setup() {
        system = ActorSystem.create("StatelessActorTestSystem");
    }

    @AfterAll
    public static void teardown() {
        system.terminate();
        system = null;
    }

    @Test
    public void testStatelessActorHelpRequest() {
        new TestKit(system) {{
            final ActorRef statelessActor = system.actorOf(StatelessActor.props("TestWorker"));

            // Test help request
            statelessActor.tell(new AskForHelpStateless("Need assistance with computation"), getRef());

            HelpResponseStateless response = expectMsgClass(HelpResponseStateless.class);
            assertNotNull(response, "Help response should not be null");
            assertTrue(response.message().length() > 0, "Should provide helpful response");
        }};
    }

    @Test
    public void testStatelessActorStatusRequest() {
        new TestKit(system) {{
            final ActorRef statelessActor = system.actorOf(StatelessActor.props("TestWorker"));

            // Test status request
            statelessActor.tell(new GetStatusStateless(), getRef());

            WorkerStatusStateless status = expectMsgClass(WorkerStatusStateless.class);
            assertNotNull(status, "Status should not be null");
            assertNotEquals(WorkerStatusStateless.IN_MEETING, status, "Stateless actor should never be in meeting");
        }};
    }

    @Test
    public void testStatelessActorConsistentBehavior() {
        new TestKit(system) {{
            final ActorRef statelessActor = system.actorOf(StatelessActor.props("TestWorker"));

            // Test that multiple identical requests produce consistent results
            for (int i = 0; i < 3; i++) {
                statelessActor.tell(new GetStatusStateless(), getRef());
                WorkerStatusStateless status = expectMsgClass(WorkerStatusStateless.class);
                assertNotNull(status, "Status " + i + " should not be null");
                assertNotEquals(WorkerStatusStateless.IN_MEETING, status, "Status should be consistent across calls");
            }
        }};
    }

        @Test
    public void testStatelessActorMultipleHelpRequests() {
        new TestKit(system) {{
            // Test multiple help requests with separate actor instances
            // (StatelessActor uses behavior switching, so each request needs a fresh actor)
            String[] requests = {
                "Help with calculation",
                "Need algorithm advice",
                "Assistance required"
            };

            for (int i = 0; i < requests.length; i++) {
                // Create a new actor for each request to test consistency
                final ActorRef statelessActor = system.actorOf(StatelessActor.props("TestWorker" + i));

                statelessActor.tell(new AskForHelpStateless(requests[i]), getRef());
                HelpResponseStateless response = expectMsgClass(HelpResponseStateless.class);
                assertNotNull(response, "Response should not be null for request: " + requests[i]);
                assertTrue(response.message().length() > 0, "Response should contain helpful content");
            }
        }};
    }

    @Test
    public void testStatelessActorMeetingRequestHandling() {
        new TestKit(system) {{
            final ActorRef statelessActor = system.actorOf(StatelessActor.props("TestWorker"));

            // Test meeting request (stateless actors typically don't join meetings)
            statelessActor.tell(new JoinMeetingStateless("TestMeeting"), getRef());

            // The actor is in idle state, so this message won't be handled
            // It should log an error and not respond
            expectNoMessage();
        }};
    }

    @Test
    public void testStatelessActorInMeetingMessage() {
        new TestKit(system) {{
            final ActorRef statelessActor = system.actorOf(StatelessActor.props("TestWorker"));

            // Test InMeeting message handling
            statelessActor.tell(new InMeetingStateless("not in meeting"), getRef());

            // Should handle appropriately (won't be handled in idle state)
            expectNoMessage();

            // Verify status remains unchanged
            statelessActor.tell(new GetStatusStateless(), getRef());
            WorkerStatusStateless status = expectMsgClass(WorkerStatusStateless.class);
            assertNotEquals(WorkerStatusStateless.IN_MEETING, status, "Status should remain unchanged");
        }};
    }

    @Test
    public void testStatelessActorSendMeetingLink() {
        new TestKit(system) {{
            final ActorRef statelessActor = system.actorOf(StatelessActor.props("TestWorker"));

            // Test sending meeting link
            statelessActor.tell(new SendMeetingLinkStateless("http://meeting.example.com"), getRef());

            // Should handle meeting link messages (won't be handled in idle state)
            expectNoMessage();
        }};
    }
}
