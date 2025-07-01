package demo.messages.stateful;

/**
 * Message requesting help with a specific value
 * Equivalent to Scala's AskForHelpStateful case class
 */
public record AskForHelpStateful(String value) implements StatefulMessage {
}
