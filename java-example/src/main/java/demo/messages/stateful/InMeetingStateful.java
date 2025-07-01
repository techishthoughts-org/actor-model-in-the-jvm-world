package demo.messages.stateful;

/**
 * Message indicating actor is already in meeting
 * Equivalent to Scala's InMeetingStateful case class
 */
public record InMeetingStateful(String value) implements StatefulMessage {
}
