package demo.actors.simple;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.messages.DoubleMessage;
import demo.messages.IntMessage;
import demo.messages.StringMessage;

/**
 * Simple actor that demonstrates request-response messaging pattern.
 * Processes messages and sends responses back to the sender.
 *
 * Supported messages:
 * - IntMessage: Responds with the same IntMessage
 * - StringMessage: Responds with the same StringMessage
 * - DoubleMessage: Responds with the same DoubleMessage
 * - Any other message: Logs warning but doesn't respond
 *
 * Equivalent to Scala's SimpleActorAsk
 */
public class SimpleActorAsk extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String actorName = getSelf().path().name();

    @Override
    public void preStart() {
        log.info("SimpleActorAsk '{}' started", actorName);
    }

    @Override
    public void postStop() {
        log.info("SimpleActorAsk '{}' stopped", actorName);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(IntMessage.class, this::handleIntMessage)
            .match(StringMessage.class, this::handleStringMessage)
            .match(DoubleMessage.class, this::handleDoubleMessage)
            .matchAny(this::handleUnexpectedMessage)
            .build();
    }

    private void handleIntMessage(IntMessage message) {
        long startTime = System.currentTimeMillis();
        var originalSender = getSender();

        try {
            log.info("[{}] Processing IntMessage: {} from {}",
                actorName, message, originalSender.path());
            originalSender.tell(message, getSelf());
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error(ex, "[{}] Error processing IntMessage: {}", actorName, message);
            recordError();
            originalSender.tell(new Status.Failure(ex), getSelf());
        }
    }

    private void handleStringMessage(StringMessage message) {
        long startTime = System.currentTimeMillis();
        var originalSender = getSender();

        try {
            log.info("[{}] Processing StringMessage: {} from {}",
                actorName, message, originalSender.path());
            originalSender.tell(message, getSelf());
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error(ex, "[{}] Error processing StringMessage: {}", actorName, message);
            recordError();
            originalSender.tell(new Status.Failure(ex), getSelf());
        }
    }

    private void handleDoubleMessage(DoubleMessage message) {
        long startTime = System.currentTimeMillis();
        var originalSender = getSender();

        try {
            log.info("[{}] Processing DoubleMessage: {} from {}",
                actorName, message, originalSender.path());
            originalSender.tell(message, getSelf());
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error(ex, "[{}] Error processing DoubleMessage: {}", actorName, message);
            recordError();
            originalSender.tell(new Status.Failure(ex), getSelf());
        }
    }

    private void handleUnexpectedMessage(Object message) {
        log.warning("[{}] Received unexpected message: {} from {}",
            actorName, message, getSender().path());
        recordError();
        // Don't respond to unexpected messages to avoid protocol violations
    }

    private void recordMetrics(long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        // In a real system, you would send this to a metrics collection actor
        log.debug("[{}] Message processed in {}ms", actorName, processingTime);
    }

    private void recordError() {
        // In a real system, you would send this to a metrics collection actor
        log.debug("[{}] Error recorded", actorName);
    }
}
