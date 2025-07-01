package demo.actors.stateful

import akka.actor.Actor
import akka.actor.ActorLogging
import demo.actors.stateful.messages._
import akka.actor.Props

object StatefulActor {
  def props(name: String, status: WorkerStatusStateful): Props = Props(
    new StatefulActor(name, status)
  )
}

class StatefulActor(name: String, var status: WorkerStatusStateful)
    extends Actor
    with ActorLogging {

  override def preStart(): Unit = {
    log.info(s"WorkerActor $name started with status $status")
  }

  override def receive: Receive = {
    case AskForHelpStateful(message: String) =>
      log.info(s"Received AskForHelp message from ${sender().path}")
      status match {
        case WorkerStatusStateful.Idle | WorkerStatusStateful.Available =>
          log.info(
            s"${self.path}, changing status from $status to WaitingResponse"
          )
          status = WorkerStatusStateful.WaitingResponse
          log.info(s"Replying to sender ${sender().path} with HelpResponse")
          sender() ! HelpResponseStateful("I will help you!")
        case _ =>
          log.error(s"Unexpected AskForHelp message in state $status")
      }

    case HelpResponseStateful(message: String) =>
      log.info(s"Received HelpResponse message from ${sender().path}")
      status match {
        case WorkerStatusStateful.Idle | WorkerStatusStateful.Available =>
          log.info(s"${self.path}, changing status from $status to Available")
          status = WorkerStatusStateful.InMeeting
          sender() ! SendMeetingLinkStateful(
            "Can you join in this meeting link ?"
          )
        case _ =>
          log.error(s"Unexpected HelpResponse message in state $status")
      }

    case SendMeetingLinkStateful(message: String) =>
      log.info(s"Received SendMeetingLink message from ${sender().path}")
      status match {
        case WorkerStatusStateful.Available |
            WorkerStatusStateful.WaitingResponse =>
          log.info(s"${self.path}, changing status from $status to InMeeting")
          status = WorkerStatusStateful.InMeeting
          sender() ! JoinMeetingStateful("Yes, I can join")
        case _ =>
          log.error(s"Unexpected SendMeetingLink message in state $status")
      }

    case JoinMeetingStateful(message: String) =>
      log.info(s"Received JoinMeeting message from ${sender().path}")
      status match {
        case WorkerStatusStateful.Available =>
          log.info(s"${self.path}, changing status from $status to InMeeting")
          status = WorkerStatusStateful.InMeeting
          sender() ! JoinMeetingStateful("Joining the meeting")
        case WorkerStatusStateful.InMeeting =>
          log.info(s"${self.path}, is already in meeting")
          sender() ! InMeetingStateful("Joining the meeting")
        case _ =>
          log.error(s"Unexpected JoinMeeting message in state $status")
      }
    case WorkerStatusStateful.InMeeting =>
      log.info(s"${self.path}, is already in meeting")
      sender() ! InMeetingStateful("Joining the meeting")

    case GetStatusStateful =>
      sender() ! status
    case _ =>
      log.error(s"Received unexpected message in state $status")
  }
}
