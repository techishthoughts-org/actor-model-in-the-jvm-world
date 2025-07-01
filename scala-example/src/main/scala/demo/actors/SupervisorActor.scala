package demo.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.SupervisorStrategy.Restart
import akka.actor.SupervisorStrategy.Resume
import akka.actor.SupervisorStrategy.Stop

import scala.concurrent.duration.*

/**
 * Supervisor actor that demonstrates proper supervision strategies
 * for fault tolerance and resilience in the actor system.
 */
class SupervisorActor extends Actor with ActorLogging {

  /**
   * Supervision strategy that defines how to handle different types of failures:
   * - IllegalArgumentException: Resume the actor (temporary failure)
   * - NullPointerException: Restart the actor (recoverable failure)
   * - IllegalStateException: Stop the actor (unrecoverable failure)
   * - Exception: Escalate to parent supervisor (unknown failure)
   */
  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(
    maxNrOfRetries = 10,
    withinTimeRange = 1.minute,
    loggingEnabled = true
  ) {
    case _: IllegalArgumentException =>
      log.warning("Resuming actor after IllegalArgumentException")
      Resume
    case _: NullPointerException =>
      log.warning("Restarting actor after NullPointerException")
      Restart
    case _: IllegalStateException =>
      log.error("Stopping actor after IllegalStateException")
      Stop
    case _: Exception =>
      log.error("Escalating unknown exception to parent")
      Escalate
  }

  override def preStart(): Unit = {
    log.info("SupervisorActor started")
  }

  override def postStop(): Unit = {
    log.info("SupervisorActor stopped")
  }

  override def receive: Receive = {
    case CreateChild(name, props) =>
      val child = context.actorOf(props, name)
      sender() ! ChildCreated(name, child)
      log.info(s"Created child actor: $name")

    case message =>
      log.info(s"SupervisorActor received: $message")
  }
}

object SupervisorActor {
  def props(): Props = Props(new SupervisorActor)
}

// Messages for supervisor interaction
case class CreateChild(name: String, props: Props)
case class ChildCreated(name: String, ref: akka.actor.ActorRef)
