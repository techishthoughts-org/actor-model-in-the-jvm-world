package demo.messages;

/**
 * Message containing a double value
 * Equivalent to Scala's DoubleMessage case class
 * Uses Java 21 record with sealed interface
 */
public record DoubleMessage(double value) implements Message {
    @Override
    public String getContent() {
        return String.valueOf(value);
    }
}
