package demo.messages;

/**
 * Message containing a string value
 * Equivalent to Scala's StringMessage case class
 * Uses Java 21 record with sealed interface
 */
public record StringMessage(String value) implements Message {
    @Override
    public String getContent() {
        return value;
    }
}
