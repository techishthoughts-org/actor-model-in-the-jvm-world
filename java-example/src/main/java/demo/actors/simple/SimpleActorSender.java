package demo.actors.simple;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import demo.messages.DoubleMessage;
import demo.messages.IntMessage;
import demo.messages.StringMessage;

/**
 * Simple actor that demonstrates fire-and-forget messaging pattern.
 * Processes messages without sending responses back to the sender.
 *
 * Supported messages:
 * - IntMessage: Logs the integer value received
 * - StringMessage: Logs the string value received
 * - DoubleMessage: Logs the double value received
 * - Any other message: Logs warning and stops the actor
 */
public class SimpleActorSender extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final String actorName = getSelf().path().name();

    @Override
    public void preStart() {
        log.info("SimpleActorSender '{}' started", actorName);
    }

    @Override
    public void postStop() {
        log.info("SimpleActorSender '{}' stopped", actorName);
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

    /**
     * Handle IntMessage with metrics recording and error handling
     */
    private void handleIntMessage(IntMessage message) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("[{}] Processing IntMessage: {}", actorName, message);
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error(ex, "[{}] Error processing IntMessage: {}", actorName, message);
            recordError();
        }
    }

    /**
     * Handle StringMessage with metrics recording and error handling
     */
    private void handleStringMessage(StringMessage message) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("[{}] Processing StringMessage: {}", actorName, message);
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error(ex, "[{}] Error processing StringMessage: {}", actorName, message);
            recordError();
        }
    }

    /**
     * Handle DoubleMessage with metrics recording and error handling
     */
    private void handleDoubleMessage(DoubleMessage message) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("[{}] Processing DoubleMessage: {}", actorName, message);
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error(ex, "[{}] Error processing DoubleMessage: {}", actorName, message);
            recordError();
        }
    }

    /**
     * Handle unexpected messages by logging warning and stopping actor
     */
    private void handleUnexpectedMessage(Object message) {
        log.warning("[{}] Received unexpected message: {} from {}",
                   actorName, message, getSender().path());
        recordError();
        getContext().stop(getSelf());
    }

    /**
     * Record processing metrics (would send to metrics collection actor in real system)
     */
    private void recordMetrics(long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        log.debug("[{}] Message processed in {}ms", actorName, processingTime);
    }

    /**
     * Record error metrics (would send to metrics collection actor in real system)
     */
    private void recordError() {
        log.debug("[{}] Error recorded", actorName);
    }
}
