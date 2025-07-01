package demo.messages;

/**
 * Message containing an integer value
 * Equivalent to Scala's IntMessage case class
 * Uses Java 21 record with sealed interface
 */
public record IntMessage(int value) implements Message {
    @Override
    public String getContent() {
        return String.valueOf(value);
    }
}
