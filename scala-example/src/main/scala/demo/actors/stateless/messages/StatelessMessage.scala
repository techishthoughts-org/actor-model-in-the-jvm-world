package demo.actors.stateless.messages

enum WorkerStatusStateless {
  case Idle
  case Available
  case WaitingResponse
  case InMeeting
}

// Define message types for the stateless actor. These represent requests and responses handled by the actor.
sealed trait StatelessMessage

// A request for help with a specific message
case class AskForHelpStateless(message: String) extends StatelessMessage

// A response indicating willingness to help
case class HelpResponseStateless(message: String) extends StatelessMessage

// A request to send a meeting link
case class SendMeetingLinkStateless(message: String) extends StatelessMessage

// A response indicating agreement to join a meeting
case class JoinMeetingStateless(message: String) extends StatelessMessage

// A notification for when the actor is already in a meeting
case class InMeetingStateless(message: String) extends StatelessMessage

// A request to retrieve the current status of the actor
case class GetStatusStateless() extends StatelessMessage
