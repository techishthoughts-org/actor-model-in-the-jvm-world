package demo.messages.stateless;

/**
 * Message requesting help with a specific message
 * Equivalent to Scala's AskForHelpStateless case class
 */
public record AskForHelpStateless(String message) implements StatelessMessage {
}
