package demo.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Cancellable
import akka.actor.Props

import scala.collection.mutable
import scala.concurrent.duration.*

/**
 * Metrics collection actor that tracks performance metrics
 * for the actor system and provides monitoring data.
 */
class MetricsActor extends Actor with ActorLogging {

  import MetricsActor._
  import context.dispatcher

  private val messageCounters = mutable.Map[String, Long]().withDefaultValue(0L)
  private val processingTimes = mutable.Map[String, List[Long]]().withDefaultValue(List.empty)
  private val errorCounters = mutable.Map[String, Long]().withDefaultValue(0L)
  private var metricsReportSchedule: Option[Cancellable] = None

  override def preStart(): Unit = {
    log.info("MetricsActor started")
    // Schedule periodic metrics reporting every 60 seconds
    metricsReportSchedule = Some(
      context.system.scheduler.scheduleWithFixedDelay(
        initialDelay = 60.seconds,
        delay = 60.seconds,
        receiver = self,
        message = ReportMetrics
      )
    )
  }

  override def postStop(): Unit = {
    metricsReportSchedule.foreach(_.cancel())
    log.info("MetricsActor stopped")
  }

  override def receive: Receive = {
    case IncrementMessageCounter(actorName) =>
      messageCounters += actorName -> (messageCounters(actorName) + 1)

    case RecordProcessingTime(actorName, processingTimeMs) =>
      val currentTimes = processingTimes(actorName)
      val updatedTimes = (processingTimeMs :: currentTimes).take(100) // Keep last 100 measurements
      processingTimes += actorName -> updatedTimes

    case IncrementErrorCounter(actorName) =>
      errorCounters += actorName -> (errorCounters(actorName) + 1)

    case GetMetrics(actorName) =>
      val metrics = ActorMetrics(
        actorName = actorName,
        messageCount = messageCounters(actorName),
        averageProcessingTime = calculateAverageProcessingTime(actorName),
        errorCount = errorCounters(actorName)
      )
      sender() ! metrics

    case GetAllMetrics =>
      val allActors = (messageCounters.keys ++ processingTimes.keys ++ errorCounters.keys).toSet
      val allMetrics = allActors.map { actorName =>
        ActorMetrics(
          actorName = actorName,
          messageCount = messageCounters(actorName),
          averageProcessingTime = calculateAverageProcessingTime(actorName),
          errorCount = errorCounters(actorName)
        )
      }.toList
      sender() ! SystemMetrics(allMetrics)

    case ResetMetrics(actorName) =>
      messageCounters.remove(actorName)
      processingTimes.remove(actorName)
      errorCounters.remove(actorName)
      log.info(s"Reset metrics for actor: $actorName")

    case ResetAllMetrics =>
      messageCounters.clear()
      processingTimes.clear()
      errorCounters.clear()
      log.info("Reset all metrics")

    case ReportMetrics =>
      reportCurrentMetrics()
  }

  private def calculateAverageProcessingTime(actorName: String): Option[Double] = {
    val times = processingTimes(actorName)
    if (times.nonEmpty) {
      Some(times.sum.toDouble / times.length)
    } else {
      None
    }
  }

  private def reportCurrentMetrics(): Unit = {
    val allActors = (messageCounters.keys ++ processingTimes.keys ++ errorCounters.keys).toSet

    if (allActors.nonEmpty) {
      log.info("=== METRICS REPORT ===")
      allActors.foreach { actorName =>
        val msgCount = messageCounters(actorName)
        val avgTime = calculateAverageProcessingTime(actorName)
        val errors = errorCounters(actorName)

        val avgTimeStr = avgTime.map(t => f"$t%.2f ms").getOrElse("N/A")
        log.info(s"Actor: $actorName | Messages: $msgCount | Avg Processing: $avgTimeStr | Errors: $errors")
      }
      log.info("=====================")
    }
  }
}

object MetricsActor {
  def props(): Props = Props(new MetricsActor)

  // Messages
  case class IncrementMessageCounter(actorName: String)
  case class RecordProcessingTime(actorName: String, processingTimeMs: Long)
  case class IncrementErrorCounter(actorName: String)
  case class GetMetrics(actorName: String)
  case object GetAllMetrics
  case class ResetMetrics(actorName: String)
  case object ResetAllMetrics
  case object ReportMetrics

  // Response types
  case class ActorMetrics(
    actorName: String,
    messageCount: Long,
    averageProcessingTime: Option[Double],
    errorCount: Long
  )

  case class SystemMetrics(actorMetrics: List[ActorMetrics])
}
