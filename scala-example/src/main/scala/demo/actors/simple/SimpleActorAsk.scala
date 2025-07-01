package demo.actors.simple

import akka.actor.Actor
import akka.actor.ActorLogging
import demo.actors.simple.messages.DoubleMessage
import demo.actors.simple.messages.IntMessage
import demo.actors.simple.messages.StringMessage

/**
 * Simple actor that demonstrates request-response messaging pattern.
 * Processes messages and sends responses back to the sender.
 *
 * Supported messages:
 * - IntMessage: Responds with the same IntMessage
 * - StringMessage: Responds with the same StringMessage
 * - DoubleMessage: Responds with the same DoubleMessage
 * - Any other message: Logs warning but doesn't respond
 */
class SimpleActorAsk extends Actor with ActorLogging {

  private val actorName = self.path.name

  override def preStart(): Unit = {
    log.info(s"SimpleActorAsk '$actorName' started")
  }

  override def postStop(): Unit = {
    log.info(s"SimpleActorAsk '$actorName' stopped")
  }

  override def receive: Receive = {
    case value: IntMessage =>
      val startTime = System.currentTimeMillis()
      val originalSender = sender()
      try {
        log.info(s"[$actorName] Processing IntMessage: $value from ${originalSender.path}")
        originalSender ! value
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(ex, s"[$actorName] Error processing IntMessage: $value")
          recordError()
          // Send error response to sender
          originalSender ! akka.actor.Status.Failure(ex)
      }

    case value: StringMessage =>
      val startTime = System.currentTimeMillis()
      val originalSender = sender()
      try {
        log.info(s"[$actorName] Processing StringMessage: $value from ${originalSender.path}")
        originalSender ! value
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(ex, s"[$actorName] Error processing StringMessage: $value")
          recordError()
          originalSender ! akka.actor.Status.Failure(ex)
      }

    case value: DoubleMessage =>
      val startTime = System.currentTimeMillis()
      val originalSender = sender()
      try {
        log.info(s"[$actorName] Processing DoubleMessage: $value from ${originalSender.path}")
        originalSender ! value
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(ex, s"[$actorName] Error processing DoubleMessage: $value")
          recordError()
          originalSender ! akka.actor.Status.Failure(ex)
      }

    case unexpected =>
      log.warning(s"[$actorName] Received unexpected message: $unexpected from ${sender().path}")
      recordError()
      // Don't respond to unexpected messages to avoid protocol violations
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
