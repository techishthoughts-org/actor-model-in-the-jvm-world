package demo.actors.simple;

import akka.actor.AbstractActor;
import akka.actor.Props;
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

    /**
     * Factory method for creating Props for this actor
     */
    public static Props props() {
        return Props.create(SimpleActorSender.class);
    }

    @Override
    public void preStart() {
        log.info("🚀 Actor '{}' started and ready to process messages", actorName);
    }

    @Override
    public void postStop() {
        log.info("🛑 Actor '{}' stopped gracefully", actorName);
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
            log.info("🔢 Received integer value: {} - Processing completed", message.value());
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error("❌ Failed to process integer message: {} - Error: {}", message, ex.getMessage());
            recordError();
        }
    }

    /**
     * Handle StringMessage with metrics recording and error handling
     */
    private void handleStringMessage(StringMessage message) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("📝 Received text message: '{}' - Processing completed", message.value());
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error("❌ Failed to process text message: {} - Error: {}", message, ex.getMessage());
            recordError();
        }
    }

    /**
     * Handle DoubleMessage with metrics recording and error handling
     */
    private void handleDoubleMessage(DoubleMessage message) {
        long startTime = System.currentTimeMillis();
        try {
            log.info("🔢 Received decimal value: {} - Processing completed", message.value());
            recordMetrics(startTime);
        } catch (Exception ex) {
            log.error("❌ Failed to process decimal message: {} - Error: {}", message, ex.getMessage());
            recordError();
        }
    }

    /**
     * Handle unexpected messages by logging warning and stopping actor
     */
    private void handleUnexpectedMessage(Object message) {
        log.warning("⚠️ Unexpected message received: {} from sender: {} - Stopping actor for safety",
                message.getClass().getSimpleName(), getSender().path().name());
        recordError();
        getContext().stop(getSelf());
    }

    /**
     * Record processing metrics (would send to metrics collection actor in real system)
     */
    private void recordMetrics(long startTime) {
        long processingTime = System.currentTimeMillis() - startTime;
        log.debug("⚡ Message processed in {}ms", processingTime);
    }

    /**
     * Record error metrics (would send to metrics collection actor in real system)
     */
    private void recordError() {
        log.debug("📊 Error metric recorded for analysis");
    }
}
