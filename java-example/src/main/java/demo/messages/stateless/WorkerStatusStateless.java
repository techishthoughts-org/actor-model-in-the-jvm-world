package demo.messages.stateless;

/**
 * Worker status enumeration for stateless actors
 * Equivalent to Scala's WorkerStatusStateless enum
 */
public enum WorkerStatusStateless {
    IDLE,
    AVAILABLE,
    WAITING_RESPONSE,
    IN_MEETING
}
