package demo.messages.stateful;

/**
 * Worker status enumeration for stateful actors
 * Equivalent to Scala's WorkerStatusStateful enum
 */
public enum WorkerStatusStateful {
    IDLE,
    AVAILABLE,
    WAITING_RESPONSE,
    IN_MEETING
}
