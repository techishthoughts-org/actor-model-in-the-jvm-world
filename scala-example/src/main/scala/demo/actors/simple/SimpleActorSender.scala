package demo.actors.simple

import akka.actor.Actor
import akka.actor.ActorLogging
import demo.actors.simple.messages.DoubleMessage
import demo.actors.simple.messages.IntMessage
import demo.actors.simple.messages.StringMessage

/**
 * Simple actor that demonstrates fire-and-forget messaging pattern.
 * Processes messages without sending responses back to the sender.
 *
 * Supported messages:
 * - IntMessage: Logs the integer value received
 * - StringMessage: Logs the string value received
 * - DoubleMessage: Logs the double value received
 * - Any other message: Logs warning and stops the actor
 */
class SimpleActorSender extends Actor with ActorLogging {

  private val actorName = self.path.name

  override def preStart(): Unit = {
    log.info(s"SimpleActorSender '$actorName' started")
  }

  override def postStop(): Unit = {
    log.info(s"SimpleActorSender '$actorName' stopped")
  }

  override def receive: Receive = {
    case value: IntMessage =>
      val startTime = System.currentTimeMillis()
      try {
        log.info(s"[$actorName] Processing IntMessage: $value")
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(ex, s"[$actorName] Error processing IntMessage: $value")
          recordError()
      }

    case value: StringMessage =>
      val startTime = System.currentTimeMillis()
      try {
        log.info(s"[$actorName] Processing StringMessage: $value")
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(ex, s"[$actorName] Error processing StringMessage: $value")
          recordError()
      }

    case value: DoubleMessage =>
      val startTime = System.currentTimeMillis()
      try {
        log.info(s"[$actorName] Processing DoubleMessage: $value")
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(ex, s"[$actorName] Error processing DoubleMessage: $value")
          recordError()
      }

    case unexpected =>
      log.warning(s"[$actorName] Received unexpected message: $unexpected from ${sender().path}")
      recordError()
      context.stop(self)
  }

  private def recordMetrics(startTime: Long): Unit = {
    val processingTime = System.currentTimeMillis() - startTime
    // In a real system, you would send this to a metrics collection actor
    log.debug(s"[$actorName] Message processed in ${processingTime}ms")
  }

  private def recordError(): Unit = {
    // In a real system, you would send this to a metrics collection actor
    log.debug(s"[$actorName] Error recorded")
  }
}
