package demo.messages.stateful;

/**
 * Message sending a meeting link
 * Equivalent to Scala's SendMeetingLinkStateful case class
 */
public record SendMeetingLinkStateful(String value) implements StatefulMessage {
}
