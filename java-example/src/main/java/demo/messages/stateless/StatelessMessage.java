package demo.messages.stateless;

/**
 * Base sealed interface for stateless actor messages
 * Equivalent to Scala's sealed trait StatelessMessage
 * Uses Java 21 sealed classes feature
 */
public sealed interface StatelessMessage
    permits AskForHelpStateless, HelpResponseStateless, SendMeetingLinkStateless,
            JoinMeetingStateless, InMeetingStateless, GetStatusStateless {
}
