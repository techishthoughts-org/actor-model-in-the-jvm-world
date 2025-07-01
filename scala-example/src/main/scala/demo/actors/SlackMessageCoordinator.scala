package demo.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import demo.actors.stateful.messages._
import akka.pattern.ask
import scala.concurrent.{Future, ExecutionContext}
import akka.util.Timeout
import scala.concurrent.duration.DurationInt
import demo.actors.stateful._
import demo.actors.stateless.messages._

case class SlackMessage(
    origin: ActorRef,
    message: StatefulMessage | StatelessMessage,
    target: ActorRef
)

case class SlackChat(origin: ActorRef, target: ActorRef)

object SlackMessageCoordinator {
  def props()(implicit ec: ExecutionContext): Props = Props(
    new SlackMessageCoordinator
  )
}

import akka.util.Timeout

class SlackMessageCoordinator(implicit ec: ExecutionContext)
    extends Actor
    with ActorLogging {

  implicit val timeout: Timeout = 2.seconds

  var chat: Option[SlackChat] = None

  def areTheyInMeeting(): Future[Boolean] = {
    val originStatusFuture =
      (chat.get.origin ? GetStatusStateful).mapTo[WorkerStatusStateful]
    val targetStatusFuture =
      (chat.get.target ? GetStatusStateful).mapTo[WorkerStatusStateful]

    for {
      originStatus <- originStatusFuture
      targetStatus <- targetStatusFuture
    } yield {
      originStatus == WorkerStatusStateful.InMeeting && targetStatus == WorkerStatusStateful.InMeeting
    }

  }
  def areTheyInMeetingSync(): Boolean = {
    import scala.concurrent.Await
    Await.result(areTheyInMeeting(), timeout.duration)
  }

  def forwardMessage(message: Any): Unit = {
    if (sender() == chat.get.target) {
      log.info(
        s"Forwarding the message: $message from ${sender().path} to ${chat.get.target.path}"
      )
      chat.get.origin ! message
    } else {
      log.info(
        s"Forwarding the message: $message from ${sender().path} to ${chat.get.origin.path}"
      )
      chat.get.target ! message
    }
  }

  override def receive: Receive = {
    case SlackMessage(origin, message, target) =>
      chat = Some(SlackChat(origin, target))
      log.info(
        s"Received a SlackMessage from ${origin.path} to ${target.path}"
      )
      target ! message

    case message: StatefulMessage =>
      if (areTheyInMeetingSync()) {
        log.info(
          s"Both workers are in meeting, forwarding the message: $message from ${sender().path} to ${chat.get.origin.path}"
        )
        self ! "Both workers are in meeting"
      } else {
        forwardMessage(message)
      }

    case message: StatelessMessage =>
      log.info(
        s"Received a StatelessMessage: $message from ${sender().path}"
      )
      forwardMessage(message)

    case message: String =>
      log.info(s"Received message $message")
      context.stop(self)
  }
}
