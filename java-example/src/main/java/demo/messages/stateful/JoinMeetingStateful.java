package demo.messages.stateful;

/**
 * Message for joining a meeting
 * Equivalent to Scala's JoinMeetingStateful case class
 */
public record JoinMeetingStateful(String value) implements StatefulMessage {
}
