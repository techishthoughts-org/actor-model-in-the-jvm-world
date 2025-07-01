package demo.messages.stateful;

/**
 * Message responding to help request
 * Equivalent to Scala's HelpResponseStateful case class
 */
public record HelpResponseStateful(String value) implements StatefulMessage {
}
