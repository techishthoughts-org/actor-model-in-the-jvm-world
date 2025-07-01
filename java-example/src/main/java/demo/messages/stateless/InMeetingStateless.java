package demo.messages.stateless;

/**
 * Message indicating actor is already in meeting
 * Equivalent to Scala's InMeetingStateless case class
 */
public record InMeetingStateless(String message) implements StatelessMessage {
}
