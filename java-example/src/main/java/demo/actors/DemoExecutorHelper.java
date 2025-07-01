package demo.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import demo.actors.simple.SimpleActorAsk;
import demo.actors.simple.SimpleActorSender;
import demo.actors.stateful.StatefulActor;
import demo.actors.stateless.StatelessActor;
import demo.messages.DoubleMessage;
import demo.messages.IntMessage;
import demo.messages.StringMessage;
import demo.messages.stateful.AskForHelpStateful;
import demo.messages.stateful.GetStatusStateful;
import demo.messages.stateful.WorkerStatusStateful;
import demo.messages.stateless.AskForHelpStateless;
import demo.messages.stateless.HelpResponseStateless;
import demo.messages.stateless.SendMeetingLinkStateless;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * Helper class for demonstrating various actor patterns
 * Equivalent to Scala's DemoExecutorHelper
 */
public class DemoExecutorHelper {

    private static final Timeout timeout = Timeout.create(java.time.Duration.ofSeconds(5));

    public static void statefulWorkerExampleWithLogging(ActorSystem system) {
        System.out.println("🔄 Stateful Worker Demonstration");

        // Create stateful actor
        ActorRef statefulActor = system.actorOf(
            StatefulActor.props("StatefulWorker1", WorkerStatusStateful.AVAILABLE),
            "statefulWorker1"
        );

        // Send messages to demonstrate state management
        statefulActor.tell(new AskForHelpStateful("Need help with Java Akka!"), ActorRef.noSender());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        statefulActor.tell(new GetStatusStateful(), ActorRef.noSender());
    }

    public static void simpleActorExampleUsingSender(ActorSystem system) {
        System.out.println("📨 Fire-and-Forget Pattern Demonstration");

        ActorRef simpleActor = system.actorOf(Props.create(SimpleActorSender.class), "simpleSender");

        // Send various message types
        simpleActor.tell(new IntMessage(42), ActorRef.noSender());
        simpleActor.tell(new StringMessage("Hello Akka!"), ActorRef.noSender());
        simpleActor.tell(new DoubleMessage(3.14), ActorRef.noSender());

        System.out.println("📨 Sent messages to SimpleActorSender (fire-and-forget pattern)");
    }

    public static void simpleActorAskPattern(ActorSystem system) {
        System.out.println("🔄 Request-Response Pattern (Ask) Demonstration");

        ActorRef askActor = system.actorOf(Props.create(SimpleActorAsk.class), "simpleAsk");

        try {
            // Use ask pattern for request-response
            Future<Object> future1 = Patterns.ask(askActor, new IntMessage(123), timeout);
            Object result1 = Await.result(future1, timeout.duration());
            System.out.println("📩 Ask response: " + result1);

            Future<Object> future2 = Patterns.ask(askActor, new StringMessage("Ask pattern works!"), timeout);
            Object result2 = Await.result(future2, timeout.duration());
            System.out.println("📩 Ask response: " + result2);

        } catch (Exception e) {
            System.err.println("❌ Ask pattern failed: " + e.getMessage());
        }
    }

    public static void statelessActorBehaviorSwitching(ActorSystem system) {
        System.out.println("🔄 Stateless Actor Behavior Switching Demonstration");

        ActorRef statelessActor = system.actorOf(
            StatelessActor.props("StatelessWorker1"),
            "statelessWorker1"
        );

        // Demonstrate behavior switching through message protocol
        statelessActor.tell(new AskForHelpStateless("Can you help me?"), ActorRef.noSender());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        statelessActor.tell(new HelpResponseStateless("Sure, I can help!"), ActorRef.noSender());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        statelessActor.tell(new SendMeetingLinkStateless("https://meet.example.com/123"), ActorRef.noSender());
    }

    public static void healthCheckDemo(ActorSystem system) {
        System.out.println("🏥 Health Check System Demonstration");

        // Create health check actor
        ActorRef healthChecker = system.actorOf(HealthCheckActor.props(), "healthChecker");

        // Create some actors to monitor
        ActorRef actor1 = system.actorOf(Props.create(SimpleActorSender.class), "monitoredActor1");
        ActorRef actor2 = system.actorOf(Props.create(SimpleActorSender.class), "monitoredActor2");

        // Register actors for monitoring
        healthChecker.tell(new HealthCheckActor.RegisterActor("actor1", actor1), ActorRef.noSender());
        healthChecker.tell(new HealthCheckActor.RegisterActor("actor2", actor2), ActorRef.noSender());

        System.out.println("🏥 Registered actors for health monitoring");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Request health status
        healthChecker.tell(new HealthCheckActor.GetHealthStatus(), ActorRef.noSender());
    }

    public static void metricsDemo(ActorSystem system) {
        System.out.println("📊 Metrics Collection Demonstration");

        // Create metrics actor
        ActorRef metricsActor = system.actorOf(MetricsActor.props(), "metricsActor");

        // Simulate some metrics
        metricsActor.tell(new MetricsActor.IncrementMessageCounter("testActor"), ActorRef.noSender());
        metricsActor.tell(new MetricsActor.RecordProcessingTime("testActor", 150L), ActorRef.noSender());
        metricsActor.tell(new MetricsActor.IncrementMessageCounter("testActor"), ActorRef.noSender());
        metricsActor.tell(new MetricsActor.RecordProcessingTime("testActor", 200L), ActorRef.noSender());

        System.out.println("📊 Recorded sample metrics");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Request metrics
        metricsActor.tell(new MetricsActor.GetAllMetrics(), ActorRef.noSender());
    }

    public static void supervisionDemo(ActorSystem system) {
        System.out.println("👨‍💼 Supervision Strategy Demonstration");

        // Create supervisor actor
        ActorRef supervisor = system.actorOf(SupervisorActor.props(), "supervisor");

        // Create child actor through supervisor
        supervisor.tell(new SupervisorActor.CreateChild("childActor", Props.create(SimpleActorSender.class)),
                       ActorRef.noSender());

        System.out.println("👨‍💼 Created child actor under supervision");
    }

    public static void runAllDemonstrations(ActorSystem system) {
        System.out.println("\n🎯 Running Complete Actor Pattern Demonstrations");
        System.out.println("=".repeat(60));

        try {
            System.out.println("\n1️⃣ Simple Fire-and-Forget Pattern:");
            simpleActorExampleUsingSender(system);
            Thread.sleep(2000);

            System.out.println("\n2️⃣ Request-Response Ask Pattern:");
            simpleActorAskPattern(system);
            Thread.sleep(2000);

            System.out.println("\n3️⃣ Stateful Actor Pattern:");
            statefulWorkerExampleWithLogging(system);
            Thread.sleep(2000);

            System.out.println("\n4️⃣ Stateless Actor with Behavior Switching:");
            statelessActorBehaviorSwitching(system);
            Thread.sleep(2000);

            System.out.println("\n5️⃣ Health Check System:");
            healthCheckDemo(system);
            Thread.sleep(2000);

            System.out.println("\n6️⃣ Metrics Collection:");
            metricsDemo(system);
            Thread.sleep(2000);

            System.out.println("\n7️⃣ Supervision Strategy:");
            supervisionDemo(system);
            Thread.sleep(2000);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Demonstration interrupted: " + e.getMessage());
        }
    }
}
