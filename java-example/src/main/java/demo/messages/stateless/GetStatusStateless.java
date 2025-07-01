package demo.messages.stateless;

/**
 * Message to request the current status of a stateless actor
 * Equivalent to Scala's GetStatusStateless case class
 */
public record GetStatusStateless() implements StatelessMessage {
}
