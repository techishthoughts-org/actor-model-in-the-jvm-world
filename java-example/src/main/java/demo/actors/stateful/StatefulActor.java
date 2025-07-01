package demo.actors.stateful;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.messages.stateful.AskForHelpStateful;
import demo.messages.stateful.GetStatusStateful;
import demo.messages.stateful.HelpResponseStateful;
import demo.messages.stateful.InMeetingStateful;
import demo.messages.stateful.JoinMeetingStateful;
import demo.messages.stateful.SendMeetingLinkStateful;
import demo.messages.stateful.WorkerStatusStateful;

/**
 * Stateful actor that maintains internal state and processes messages
 * based on its current status. Demonstrates conversation protocols
 * and state management within actors.
 *
 * Equivalent to Scala's StatefulActor
 */
public class StatefulActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String name;
    private WorkerStatusStateful status;

    public StatefulActor(String name, WorkerStatusStateful status) {
        this.name = name;
        this.status = status;
    }

    public static Props props(String name, WorkerStatusStateful status) {
        return Props.create(StatefulActor.class, name, status);
    }

    @Override
    public void preStart() {
        log.info("WorkerActor {} started with status {}", name, status);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(AskForHelpStateful.class, this::handleAskForHelp)
            .match(HelpResponseStateful.class, this::handleHelpResponse)
            .match(SendMeetingLinkStateful.class, this::handleSendMeetingLink)
            .match(JoinMeetingStateful.class, this::handleJoinMeeting)
            .match(GetStatusStateful.class, msg -> getSender().tell(status, getSelf()))
            .matchAny(this::handleUnexpectedMessage)
            .build();
    }

    private void handleAskForHelp(AskForHelpStateful message) {
        log.info("Received AskForHelp message from {}", getSender().path());

        switch (status) {
            case IDLE, AVAILABLE -> {
                log.info("{}, changing status from {} to WAITING_RESPONSE",
                    getSelf().path(), status);
                status = WorkerStatusStateful.WAITING_RESPONSE;
                log.info("Replying to sender {} with HelpResponse", getSender().path());
                getSender().tell(new HelpResponseStateful("I will help you!"), getSelf());
            }
            default -> {
                log.error("Unexpected AskForHelp message in state {}", status);
            }
        }
    }

    private void handleHelpResponse(HelpResponseStateful message) {
        log.info("Received HelpResponse message from {}", getSender().path());

        switch (status) {
            case IDLE, AVAILABLE -> {
                log.info("{}, changing status from {} to IN_MEETING",
                    getSelf().path(), status);
                status = WorkerStatusStateful.IN_MEETING;
                getSender().tell(new SendMeetingLinkStateful(
                    "Can you join in this meeting link ?"), getSelf());
            }
            default -> {
                log.error("Unexpected HelpResponse message in state {}", status);
            }
        }
    }

    private void handleSendMeetingLink(SendMeetingLinkStateful message) {
        log.info("Received SendMeetingLink message from {}", getSender().path());

        switch (status) {
            case AVAILABLE, WAITING_RESPONSE -> {
                log.info("{}, changing status from {} to IN_MEETING",
                    getSelf().path(), status);
                status = WorkerStatusStateful.IN_MEETING;
                getSender().tell(new JoinMeetingStateful("Yes, I can join"), getSelf());
            }
            default -> {
                log.error("Unexpected SendMeetingLink message in state {}", status);
            }
        }
    }

    private void handleJoinMeeting(JoinMeetingStateful message) {
        log.info("Received JoinMeeting message from {}", getSender().path());

        switch (status) {
            case AVAILABLE -> {
                log.info("{}, changing status from {} to IN_MEETING",
                    getSelf().path(), status);
                status = WorkerStatusStateful.IN_MEETING;
                getSender().tell(new JoinMeetingStateful("Joining the meeting"), getSelf());
            }
            case IN_MEETING -> {
                log.info("{}, is already in meeting", getSelf().path());
                getSender().tell(new InMeetingStateful("Already in the meeting"), getSelf());
            }
            default -> {
                log.error("Unexpected JoinMeeting message in state {}", status);
            }
        }
    }

    private void handleUnexpectedMessage(Object message) {
        log.error("Received unexpected message in state {}: {}", status, message);
    }
}
