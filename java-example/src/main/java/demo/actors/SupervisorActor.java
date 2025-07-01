package demo.actors;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;

/**
 * Supervisor actor that demonstrates proper supervision strategies
 * for fault tolerance and resilience in the actor system.
 *
 * Equivalent to Scala's SupervisorActor
 */
public class SupervisorActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * Supervision strategy that defines how to handle different types of failures:
     * - IllegalArgumentException: Resume the actor (temporary failure)
     * - NullPointerException: Restart the actor (recoverable failure)
     * - IllegalStateException: Stop the actor (unrecoverable failure)
     * - Exception: Escalate to parent supervisor (unknown failure)
     */
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(
            10, // maxNrOfRetries
            Duration.create(1, TimeUnit.MINUTES), // withinTimeRange
            akka.japi.pf.DeciderBuilder
                .match(IllegalArgumentException.class, ex -> {
                    log.warning("Resuming actor after IllegalArgumentException");
                    return SupervisorStrategy.resume();
                })
                .match(NullPointerException.class, ex -> {
                    log.warning("Restarting actor after NullPointerException");
                    return SupervisorStrategy.restart();
                })
                .match(IllegalStateException.class, ex -> {
                    log.error("Stopping actor after IllegalStateException");
                    return SupervisorStrategy.stop();
                })
                .match(Exception.class, ex -> {
                    log.error("Escalating unknown exception to parent");
                    return SupervisorStrategy.escalate();
                })
                .build()
        );
    }

    public static Props props() {
        return Props.create(SupervisorActor.class);
    }

    @Override
    public void preStart() {
        log.info("SupervisorActor started");
    }

    @Override
    public void postStop() {
        log.info("SupervisorActor stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(CreateChild.class, this::handleCreateChild)
            .matchAny(message -> log.info("SupervisorActor received: {}", message))
            .build();
    }

    private void handleCreateChild(CreateChild message) {
        ActorRef child = getContext().actorOf(message.props(), message.name());
        getSender().tell(new ChildCreated(message.name(), child), getSelf());
        log.info("Created child actor: {}", message.name());
    }

    // Messages for supervisor interaction
    public record CreateChild(String name, Props props) {}
    public record ChildCreated(String name, ActorRef ref) {}
}
