package demo.actors.stateless

import akka.actor.{Actor, ActorLogging, Props}
import demo.actors.stateless.messages._

// Companion object for StatelessActor, providing a factory method to create actor instances
object StatelessActor {
  def props(name: String): Props = Props(
    new StatelessActor(name)
  ) // Create Props for actor instantiation
}

// StatelessActor processes messages independently, using context.become to transition between behaviors
class StatelessActor(name: String) extends Actor with ActorLogging {

  // Logs a message when the actor starts
  override def preStart(): Unit = {
    log.info(s"StatelessActor $name started")
    context.become(idle) // Start in the idle state
  }

  // Defines the behavior when the actor is idle
  def idle: Receive = {
    case AskForHelpStateless(message) =>
      log.info(
        s"Received AskForHelpStateless message: '$message' from ${sender().path}"
      )
      sender() ! HelpResponseStateless("I will help you!")
      context.become(waitingResponse) // Transition to waiting response state

    case GetStatusStateless() =>
      sender() ! WorkerStatusStateless.Available

    case unexpected =>
      log.error(s"Unexpected message in Idle state: $unexpected")
  }

  // Defines the behavior when the actor is waiting for a response
  def waitingResponse: Receive = {
    case HelpResponseStateless(message) =>
      log.info(
        s"Received HelpResponseStateless message: '$message' from ${sender().path}"
      )
      sender() ! SendMeetingLinkStateless("Can you join in this meeting link?")
      context.become(available) // Transition back to available state

    case GetStatusStateless() =>
      sender() ! WorkerStatusStateless.WaitingResponse

    case unexpected =>
      log.error(s"Unexpected message in WaitingResponse state: $unexpected")
  }

  // Defines the behavior when the actor is available for a meeting
  def available: Receive = {
    case SendMeetingLinkStateless(message) =>
      log.info(
        s"Received SendMeetingLinkStateless message: '$message' from ${sender().path}"
      )
      sender() ! JoinMeetingStateless("Yes, I can join")
      context.become(inMeeting) // Transition to in-meeting state

    case GetStatusStateless() =>
      sender() ! WorkerStatusStateless.Available

    case unexpected =>
      log.error(s"Unexpected message in Available state: $unexpected")
  }

  // Defines the behavior when the actor is in a meeting
  def inMeeting: Receive = {
    case JoinMeetingStateless(message) =>
      log.info(
        s"Received JoinMeetingStateless message: '$message' from ${sender().path}"
      )
      sender() ! InMeetingStateless("Joining the meeting")

    case GetStatusStateless() =>
      sender() ! WorkerStatusStateless.InMeeting

    case unexpected =>
      log.error(s"Unexpected message in InMeeting state: $unexpected")
  }

  // Initial receive function is overridden by context.become in preStart
  override def receive: Receive = Actor.emptyBehavior
}
