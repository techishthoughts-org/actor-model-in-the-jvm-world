package demo.actors;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import akka.actor.AbstractActor;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Metrics collection actor that tracks performance metrics
 * for the actor system and provides monitoring data.
 *
 * Equivalent to Scala's MetricsActor
 */
public class MetricsActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, Long> messageCounters = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> processingTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> errorCounters = new ConcurrentHashMap<>();
    private Optional<Cancellable> metricsReportSchedule = Optional.empty();

    public static Props props() {
        return Props.create(MetricsActor.class);
    }

    @Override
    public void preStart() {
        log.info("MetricsActor started");
        // Schedule periodic metrics reporting every 60 seconds
        metricsReportSchedule = Optional.of(
            getContext().getSystem().scheduler().schedule(
                Duration.ofSeconds(60),
                Duration.ofSeconds(60),
                getSelf(),
                new ReportMetrics(),
                getContext().getDispatcher(),
                getSelf()
            )
        );
    }

    @Override
    public void postStop() {
        metricsReportSchedule.ifPresent(Cancellable::cancel);
        log.info("MetricsActor stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(IncrementMessageCounter.class, this::handleIncrementMessageCounter)
            .match(RecordProcessingTime.class, this::handleRecordProcessingTime)
            .match(IncrementErrorCounter.class, this::handleIncrementErrorCounter)
            .match(GetMetrics.class, this::handleGetMetrics)
            .match(GetAllMetrics.class, msg -> handleGetAllMetrics())
            .match(ResetMetrics.class, this::handleResetMetrics)
            .match(ResetAllMetrics.class, msg -> handleResetAllMetrics())
            .match(ReportMetrics.class, msg -> reportCurrentMetrics())
            .build();
    }

    private void handleIncrementMessageCounter(IncrementMessageCounter message) {
        messageCounters.merge(message.actorName(), 1L, Long::sum);
    }

    private void handleRecordProcessingTime(RecordProcessingTime message) {
        processingTimes.compute(message.actorName(), (key, currentTimes) -> {
            List<Long> times = currentTimes != null ? new ArrayList<>(currentTimes) : new ArrayList<>();
            times.add(0, message.processingTimeMs()); // Add to front
            // Keep last 100 measurements
            return times.size() > 100 ? times.subList(0, 100) : times;
        });
    }

    private void handleIncrementErrorCounter(IncrementErrorCounter message) {
        errorCounters.merge(message.actorName(), 1L, Long::sum);
    }

    private void handleGetMetrics(GetMetrics message) {
        ActorMetrics metrics = new ActorMetrics(
            message.actorName(),
            messageCounters.getOrDefault(message.actorName(), 0L),
            calculateAverageProcessingTime(message.actorName()),
            errorCounters.getOrDefault(message.actorName(), 0L)
        );
        getSender().tell(metrics, getSelf());
    }

    private void handleGetAllMetrics() {
        Set<String> allActors = new HashSet<>();
        allActors.addAll(messageCounters.keySet());
        allActors.addAll(processingTimes.keySet());
        allActors.addAll(errorCounters.keySet());

        List<ActorMetrics> allMetrics = allActors.stream()
            .map(actorName -> new ActorMetrics(
                actorName,
                messageCounters.getOrDefault(actorName, 0L),
                calculateAverageProcessingTime(actorName),
                errorCounters.getOrDefault(actorName, 0L)
            ))
            .collect(Collectors.toList());

        getSender().tell(new SystemMetrics(allMetrics), getSelf());
    }

    private void handleResetMetrics(ResetMetrics message) {
        messageCounters.remove(message.actorName());
        processingTimes.remove(message.actorName());
        errorCounters.remove(message.actorName());
        log.info("Reset metrics for actor: {}", message.actorName());
    }

    private void handleResetAllMetrics() {
        messageCounters.clear();
        processingTimes.clear();
        errorCounters.clear();
        log.info("Reset all metrics");
    }

    private Optional<Double> calculateAverageProcessingTime(String actorName) {
        List<Long> times = processingTimes.get(actorName);
        if (times != null && !times.isEmpty()) {
            double average = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
            return Optional.of(average);
        }
        return Optional.empty();
    }

    private void reportCurrentMetrics() {
        Set<String> allActors = new HashSet<>();
        allActors.addAll(messageCounters.keySet());
        allActors.addAll(processingTimes.keySet());
        allActors.addAll(errorCounters.keySet());

        if (!allActors.isEmpty()) {
            log.info("=== METRICS REPORT ===");
            allActors.forEach(actorName -> {
                long msgCount = messageCounters.getOrDefault(actorName, 0L);
                Optional<Double> avgTime = calculateAverageProcessingTime(actorName);
                long errors = errorCounters.getOrDefault(actorName, 0L);

                String avgTimeStr = avgTime.map(t -> String.format("%.2f ms", t)).orElse("N/A");
                log.info("Actor: {} | Messages: {} | Avg Processing: {} | Errors: {}",
                    actorName, msgCount, avgTimeStr, errors);
            });
            log.info("=====================");
        }
    }

    // Messages
    public record IncrementMessageCounter(String actorName) {}
    public record RecordProcessingTime(String actorName, long processingTimeMs) {}
    public record IncrementErrorCounter(String actorName) {}
    public record GetMetrics(String actorName) {}
    public record GetAllMetrics() {}
    public record ResetMetrics(String actorName) {}
    public record ResetAllMetrics() {}
    public record ReportMetrics() {}

    // Response types
    public record ActorMetrics(
        String actorName,
        long messageCount,
        Optional<Double> averageProcessingTime,
        long errorCount
    ) {}

    public record SystemMetrics(List<ActorMetrics> actorMetrics) {}
}
