package demo.messages.stateless;

/**
 * Message for joining a meeting
 * Equivalent to Scala's JoinMeetingStateless case class
 */
public record JoinMeetingStateless(String message) implements StatelessMessage {
}
