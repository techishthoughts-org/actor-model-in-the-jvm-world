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
    log.info(s"🏗️ Stateful worker '$name' initialized with status: $status")
  }

  override def receive: Receive = {
    case AskForHelpStateful(message: String) =>
      log.info(s"💬 Help request received: '$message'")
      status match {
        case WorkerStatusStateful.Idle | WorkerStatusStateful.Available =>
          log.info(s"🔄 Status transition: $status → WaitingResponse")
          status = WorkerStatusStateful.WaitingResponse
          log.info(s"✅ Responding with offer to help")
          sender() ! HelpResponseStateful("I will help you!")
        case _ =>
          log.error(s"❌ Cannot process help request in current status: $status")
      }

    case HelpResponseStateful(message: String) =>
      log.info(s"🤝 Help response received: '$message'")
      status match {
        case WorkerStatusStateful.Idle | WorkerStatusStateful.Available =>
          log.info(s"🔄 Status transition: $status → InMeeting")
          status = WorkerStatusStateful.InMeeting
          sender() ! SendMeetingLinkStateful(
            "Can you join in this meeting link ?"
          )
        case _ =>
          log.error(s"❌ Cannot process help response in current status: $status")
      }

    case SendMeetingLinkStateful(message: String) =>
      log.info(s"🔗 Meeting link received: '$message'")
      status match {
        case WorkerStatusStateful.Available |
            WorkerStatusStateful.WaitingResponse =>
          log.info(s"🔄 Status transition: $status → InMeeting")
          status = WorkerStatusStateful.InMeeting
          sender() ! JoinMeetingStateful("Yes, I can join")
        case _ =>
          log.error(s"❌ Cannot process meeting link in current status: $status")
      }

    case JoinMeetingStateful(message: String) =>
      log.info(s"🚪 Join meeting request: '$message'")
      status match {
        case WorkerStatusStateful.Available =>
          log.info(s"🔄 Status transition: $status → InMeeting")
          status = WorkerStatusStateful.InMeeting
          sender() ! JoinMeetingStateful("Joining the meeting")
        case WorkerStatusStateful.InMeeting =>
          log.info(s"ℹ️ Already in meeting - notifying sender")
          sender() ! InMeetingStateful("Joining the meeting")
        case _ =>
          log.error(s"❌ Cannot process join meeting request in current status: $status")
      }

    case GetStatusStateful =>
      sender() ! status

    case _ =>
      log.error(s"⚠️ Unexpected message type received in status: $status")
  }
}
