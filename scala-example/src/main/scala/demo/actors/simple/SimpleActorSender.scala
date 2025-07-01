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
    log.info(s"🚀 Actor '$actorName' started and ready to process messages")
  }

  override def postStop(): Unit = {
    log.info(s"🛑 Actor '$actorName' stopped gracefully")
  }

  override def receive: Receive = {
    case value: IntMessage =>
      val startTime = System.currentTimeMillis()
      try {
        log.info(s"🔢 Received integer value: ${value.value} - Processing completed")
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(s"❌ Failed to process integer message: $value - Error: ${ex.getMessage}")
          recordError()
      }

    case value: StringMessage =>
      val startTime = System.currentTimeMillis()
      try {
        log.info(s"📝 Received text message: '${value.value}' - Processing completed")
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(s"❌ Failed to process text message: $value - Error: ${ex.getMessage}")
          recordError()
      }

    case value: DoubleMessage =>
      val startTime = System.currentTimeMillis()
      try {
        log.info(s"🔢 Received decimal value: ${value.value} - Processing completed")
        recordMetrics(startTime)
      } catch {
        case ex: Exception =>
          log.error(s"❌ Failed to process decimal message: $value - Error: ${ex.getMessage}")
          recordError()
      }

    case unexpected =>
      log.warning(s"⚠️ Unexpected message received: ${unexpected.getClass.getSimpleName} from sender: ${sender().path.name} - Stopping actor for safety")
      recordError()
      context.stop(self)
  }

  private def recordMetrics(startTime: Long): Unit = {
    val processingTime = System.currentTimeMillis() - startTime
    // In a real system, you would send this to a metrics collection actor
    log.debug(s"⚡ Message processed in ${processingTime}ms")
  }

  private def recordError(): Unit = {
    // In a real system, you would send this to a metrics collection actor
    log.debug(s"📊 Error metric recorded for analysis")
  }
}
