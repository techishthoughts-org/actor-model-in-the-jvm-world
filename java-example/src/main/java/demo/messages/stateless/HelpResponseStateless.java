package demo.messages.stateless;

/**
 * Message responding to help request
 * Equivalent to Scala's HelpResponseStateless case class
 */
public record HelpResponseStateless(String message) implements StatelessMessage {
}
