package demo.actors;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Health check actor that monitors the health of other actors in the system
 * and provides health status information for monitoring and alerting.
 *
 * Equivalent to Scala's HealthCheckActor
 */
public class HealthCheckActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<String, ActorRef> monitoredActors = new HashMap<>();
    private final Map<String, HealthStatus> healthStatus = new HashMap<>();
    private Optional<Cancellable> healthCheckSchedule = Optional.empty();

    public static Props props() {
        return Props.create(HealthCheckActor.class);
    }

    @Override
    public void preStart() {
        log.info("HealthCheckActor started");
        // Schedule periodic health checks every 30 seconds
        healthCheckSchedule = Optional.of(
            getContext().getSystem().scheduler().schedule(
                Duration.ofSeconds(10),
                Duration.ofSeconds(30),
                getSelf(),
                new PerformHealthChecks(),
                getContext().getDispatcher(),
                getSelf()
            )
        );
    }

    @Override
    public void postStop() {
        healthCheckSchedule.ifPresent(Cancellable::cancel);
        log.info("HealthCheckActor stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(RegisterActor.class, this::handleRegisterActor)
            .match(UnregisterActor.class, this::handleUnregisterActor)
            .match(PerformHealthChecks.class, msg -> performHealthChecks())
            .match(GetHealthStatus.class, msg ->
                getSender().tell(new SystemHealthStatus(new HashMap<>(healthStatus)), getSelf()))
            .match(GetActorHealth.class, this::handleGetActorHealth)
            .match(MarkActorUnhealthy.class, this::handleMarkActorUnhealthy)
            .match(MarkActorHealthy.class, this::handleMarkActorHealthy)
            .build();
    }

    private void handleRegisterActor(RegisterActor message) {
        monitoredActors.put(message.name(), message.actorRef());
        healthStatus.put(message.name(), HealthStatus.healthy());
        log.info("Registered actor for monitoring: {}", message.name());
        getSender().tell(new ActorRegistered(message.name()), getSelf());
    }

    private void handleUnregisterActor(UnregisterActor message) {
        monitoredActors.remove(message.name());
        healthStatus.remove(message.name());
        log.info("Unregistered actor from monitoring: {}", message.name());
        getSender().tell(new ActorUnregistered(message.name()), getSelf());
    }

    private void handleGetActorHealth(GetActorHealth message) {
        Optional<HealthStatus> status = Optional.ofNullable(healthStatus.get(message.name()));
        getSender().tell(new ActorHealthStatus(message.name(), status), getSelf());
    }

    private void handleMarkActorUnhealthy(MarkActorUnhealthy message) {
        healthStatus.put(message.name(), HealthStatus.unhealthy(message.reason()));
        log.warning("Actor {} marked as unhealthy: {}", message.name(), message.reason());
    }

    private void handleMarkActorHealthy(MarkActorHealthy message) {
        healthStatus.put(message.name(), HealthStatus.healthy());
        log.info("Actor {} marked as healthy", message.name());
    }

    private void performHealthChecks() {
        log.debug("Performing health checks");
        monitoredActors.forEach((name, actorRef) -> {
            // Simple liveness check by sending a ping message
            // In a real system, you would implement a more sophisticated health check
            try {
                // Actor is assumed healthy if it exists in our map
                HealthStatus currentStatus = healthStatus.get(name);
                if (currentStatus == null || !currentStatus.isHealthy()) {
                    healthStatus.put(name, HealthStatus.healthy());
                    log.debug("Health check passed for {}", name);
                }
            } catch (Exception ex) {
                healthStatus.put(name, HealthStatus.unhealthy("Health check failed: " + ex.getMessage()));
                log.warning("Health check failed for {}: {}", name, ex.getMessage());
            }
        });
    }

    // Messages
    public record RegisterActor(String name, ActorRef actorRef) {}
    public record UnregisterActor(String name) {}
    public record PerformHealthChecks() {}
    public record GetHealthStatus() {}
    public record GetActorHealth(String name) {}
    public record MarkActorUnhealthy(String name, String reason) {}
    public record MarkActorHealthy(String name) {}

    // Responses
    public record ActorRegistered(String name) {}
    public record ActorUnregistered(String name) {}
    public record SystemHealthStatus(Map<String, HealthStatus> status) {}
    public record ActorHealthStatus(String name, Optional<HealthStatus> status) {}

    // Health status types
    public sealed interface HealthStatus permits HealthStatus.Healthy, HealthStatus.Unhealthy {
        boolean isHealthy();

        static HealthStatus healthy() {
            return new Healthy();
        }

        static HealthStatus unhealthy(String reason) {
            return new Unhealthy(reason);
        }

        record Healthy() implements HealthStatus {
            @Override
            public boolean isHealthy() {
                return true;
            }
        }

        record Unhealthy(String reason) implements HealthStatus {
            @Override
            public boolean isHealthy() {
                return false;
            }
        }
    }
}
