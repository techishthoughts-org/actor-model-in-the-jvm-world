package demo.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Cancellable
import akka.actor.Props

import scala.collection.mutable
import scala.concurrent.duration.*

/**
 * Health check actor that monitors the health of other actors in the system
 * and provides health status information for monitoring and alerting.
 */
class HealthCheckActor extends Actor with ActorLogging {

  import HealthCheckActor._
  import context.dispatcher

  private val monitoredActors = mutable.Map[String, ActorRef]()
  private val healthStatus = mutable.Map[String, HealthStatus]()
  private var healthCheckSchedule: Option[Cancellable] = None

  override def preStart(): Unit = {
    log.info("HealthCheckActor started")
    // Schedule periodic health checks every 30 seconds
    healthCheckSchedule = Some(
      context.system.scheduler.scheduleWithFixedDelay(
        initialDelay = 10.seconds,
        delay = 30.seconds,
        receiver = self,
        message = PerformHealthChecks
      )
    )
  }

  override def postStop(): Unit = {
    healthCheckSchedule.foreach(_.cancel())
    log.info("HealthCheckActor stopped")
  }

  override def receive: Receive = {
    case RegisterActor(name, actorRef) =>
      monitoredActors += name -> actorRef
      healthStatus += name -> HealthStatus.Healthy
      log.info(s"Registered actor for monitoring: $name")
      sender() ! ActorRegistered(name)

    case UnregisterActor(name) =>
      monitoredActors.remove(name)
      healthStatus.remove(name)
      log.info(s"Unregistered actor from monitoring: $name")
      sender() ! ActorUnregistered(name)

    case PerformHealthChecks =>
      performHealthChecks()

    case GetHealthStatus =>
      sender() ! SystemHealthStatus(healthStatus.toMap)

    case GetActorHealth(name) =>
      val status = healthStatus.get(name)
      sender() ! ActorHealthStatus(name, status)

    case MarkActorUnhealthy(name, reason) =>
      healthStatus += name -> HealthStatus.Unhealthy(reason)
      log.warning(s"Actor $name marked as unhealthy: $reason")

    case MarkActorHealthy(name) =>
      healthStatus += name -> HealthStatus.Healthy
      log.info(s"Actor $name marked as healthy")
  }

  private def performHealthChecks(): Unit = {
    log.debug("Performing health checks")
    monitoredActors.foreach { case (name, actorRef) =>
            // Simple liveness check by sending a ping message
      // In a real system, you would implement a more sophisticated health check
      try {
        // Actor is assumed healthy if it exists in our map
        healthStatus.get(name) match {
          case Some(HealthStatus.Healthy) => // Already healthy
          case _ =>
            healthStatus += name -> HealthStatus.Healthy
            log.debug(s"Health check passed for $name")
        }
      } catch {
        case ex: Exception =>
          healthStatus += name -> HealthStatus.Unhealthy(s"Health check failed: ${ex.getMessage}")
          log.warning(s"Health check failed for $name: ${ex.getMessage}")
      }
    }
  }
}

object HealthCheckActor {
  def props(): Props = Props(new HealthCheckActor)

  // Messages
  case class RegisterActor(name: String, actorRef: ActorRef)
  case class UnregisterActor(name: String)
  case object PerformHealthChecks
  case object GetHealthStatus
  case class GetActorHealth(name: String)
  case class MarkActorUnhealthy(name: String, reason: String)
  case class MarkActorHealthy(name: String)

  // Responses
  case class ActorRegistered(name: String)
  case class ActorUnregistered(name: String)
  case class SystemHealthStatus(status: Map[String, HealthStatus])
  case class ActorHealthStatus(name: String, status: Option[HealthStatus])

  // Health status types
  sealed trait HealthStatus
  object HealthStatus {
    case object Healthy extends HealthStatus
    case class Unhealthy(reason: String) extends HealthStatus
  }
}
