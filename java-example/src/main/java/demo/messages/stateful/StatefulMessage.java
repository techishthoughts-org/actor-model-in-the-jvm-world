package demo.messages.stateful;

/**
 * Base sealed interface for stateful actor messages
 * Equivalent to Scala's sealed trait StatefulMessage
 * Uses Java 21 sealed classes feature
 */
public sealed interface StatefulMessage
    permits AskForHelpStateful, HelpResponseStateful, SendMeetingLinkStateful,
            JoinMeetingStateful, InMeetingStateful {
}
