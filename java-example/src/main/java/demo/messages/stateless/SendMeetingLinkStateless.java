package demo.messages.stateless;

/**
 * Message sending a meeting link
 * Equivalent to Scala's SendMeetingLinkStateless case class
 */
public record SendMeetingLinkStateless(String message) implements StatelessMessage {
}
