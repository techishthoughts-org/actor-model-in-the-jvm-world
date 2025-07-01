package demo.messages;

/**
 * Base sealed interface for all actor messages
 * Equivalent to Scala's sealed trait Message
 * Uses Java 21 sealed classes feature
 */
public sealed interface Message
    permits IntMessage, StringMessage, DoubleMessage {

    /**
     * Get a string representation of the message content
     */
    String getContent();
}
