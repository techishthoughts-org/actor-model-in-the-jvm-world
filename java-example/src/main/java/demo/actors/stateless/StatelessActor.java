package demo.actors.stateless;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.messages.stateless.AskForHelpStateless;
import demo.messages.stateless.GetStatusStateless;
import demo.messages.stateless.HelpResponseStateless;
import demo.messages.stateless.InMeetingStateless;
import demo.messages.stateless.JoinMeetingStateless;
import demo.messages.stateless.SendMeetingLinkStateless;
import demo.messages.stateless.WorkerStatusStateless;

/**
 * Stateless actor that processes messages independently, using context.become
 * to transition between behaviors. Demonstrates behavior switching patterns
 * without maintaining explicit state variables.
 *
 * Equivalent to Scala's StatelessActor
 */
public class StatelessActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;

    public StatelessActor(String name) {
        this.name = name;
    }

    public static Props props(String name) {
        return Props.create(StatelessActor.class, name);
    }

    @Override
    public void preStart() {
        log.info("StatelessActor {} started", name);
        getContext().become(idle()); // Start in the idle state
    }

    @Override
    public Receive createReceive() {
        // Initial receive function is overridden by context.become in preStart
        return idle();
    }

    /**
     * Defines the behavior when the actor is idle
     */
    private Receive idle() {
        return receiveBuilder()
            .match(AskForHelpStateless.class, this::handleAskForHelpInIdle)
            .match(GetStatusStateless.class, msg ->
                getSender().tell(WorkerStatusStateless.AVAILABLE, getSelf()))
            .matchAny(msg ->
                log.error("Unexpected message in Idle state: {}", msg))
            .build();
    }

    /**
     * Defines the behavior when the actor is waiting for a response
     */
    private Receive waitingResponse() {
        return receiveBuilder()
            .match(HelpResponseStateless.class, this::handleHelpResponseInWaiting)
            .match(GetStatusStateless.class, msg ->
                getSender().tell(WorkerStatusStateless.WAITING_RESPONSE, getSelf()))
            .matchAny(msg ->
                log.error("Unexpected message in WaitingResponse state: {}", msg))
            .build();
    }

    /**
     * Defines the behavior when the actor is available for a meeting
     */
    private Receive available() {
        return receiveBuilder()
            .match(SendMeetingLinkStateless.class, this::handleSendMeetingLinkInAvailable)
            .match(GetStatusStateless.class, msg ->
                getSender().tell(WorkerStatusStateless.AVAILABLE, getSelf()))
            .matchAny(msg ->
                log.error("Unexpected message in Available state: {}", msg))
            .build();
    }

    /**
     * Defines the behavior when the actor is in a meeting
     */
    private Receive inMeeting() {
        return receiveBuilder()
            .match(JoinMeetingStateless.class, this::handleJoinMeetingInMeeting)
            .match(GetStatusStateless.class, msg ->
                getSender().tell(WorkerStatusStateless.IN_MEETING, getSelf()))
            .matchAny(msg ->
                log.error("Unexpected message in InMeeting state: {}", msg))
            .build();
    }

    private void handleAskForHelpInIdle(AskForHelpStateless message) {
        log.info("Received AskForHelpStateless message: '{}' from {}",
            message.message(), getSender().path());
        getSender().tell(new HelpResponseStateless("I will help you!"), getSelf());
        getContext().become(waitingResponse()); // Transition to waiting response state
    }

    private void handleHelpResponseInWaiting(HelpResponseStateless message) {
        log.info("Received HelpResponseStateless message: '{}' from {}",
            message.message(), getSender().path());
        getSender().tell(new SendMeetingLinkStateless("Can you join in this meeting link?"), getSelf());
        getContext().become(available()); // Transition back to available state
    }

    private void handleSendMeetingLinkInAvailable(SendMeetingLinkStateless message) {
        log.info("Received SendMeetingLinkStateless message: '{}' from {}",
            message.message(), getSender().path());
        getSender().tell(new JoinMeetingStateless("Yes, I can join"), getSelf());
        getContext().become(inMeeting()); // Transition to in-meeting state
    }

    private void handleJoinMeetingInMeeting(JoinMeetingStateless message) {
        log.info("Received JoinMeetingStateless message: '{}' from {}",
            message.message(), getSender().path());
        getSender().tell(new InMeetingStateless("Joining the meeting"), getSelf());
    }
}
