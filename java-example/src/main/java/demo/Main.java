package demo;

import akka.actor.ActorSystem;
import demo.actors.DemoExecutorHelper;

/**
 * Main application entry point
 * Comprehensive demonstration of Actor Model patterns using Java 21 and Akka with sealed classes
 */
public class Main {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ActorDemoSystem");

        System.out.println("🎯 Akka Actor Model Demo (Java 21 with Sealed Classes)");
        System.out.println("=".repeat(60));

        try {
            // Run comprehensive actor pattern demonstrations
            System.out.println("\n📋 Running Complete Actor Pattern Demonstrations...");
            DemoExecutorHelper.runAllDemonstrations(system);

            System.out.println("\n✅ All demonstrations completed successfully!");
            System.out.println("🎉 Java 21 Sealed Classes + Records equivalent to Scala sealed traits + case classes");
            System.out.println("💎 Complete Actor Model implementation with modern Java features");

        } catch (Exception exception) {
            System.err.printf("❌ Application failed: %s%n", exception.getMessage());
            exception.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}
