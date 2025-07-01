package demo

import akka.actor.ActorSystem
import demo.actors.DemoExecutorHelper
import demo.chat.ChatDemo

/**
 * Main application entry point
 * Comprehensive demonstration of Actor Model patterns using Scala 3 and Akka
 */
object Main {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("ActorDemoSystem")

    println("🎯 Akka Actor Model Demo (Scala 3)")
    println("=" * 40)

    try {
      // Demonstrate all actor patterns
      println("\n📋 Running Actor Pattern Demonstrations...")

      // Simple actor examples
      println("\n1️⃣ Simple Actor Patterns:")
      DemoExecutorHelper.statefulWorkerExampleWithLogging(system)
      Thread.sleep(2000)

      // Chat system demonstration
      println("\n2️⃣ Chat System Demonstration:")
      val chatDemo = new ChatDemo()
      chatDemo.demonstrateChatSystem(system)
      Thread.sleep(3000)

      println("\n✅ All demonstrations completed successfully!")
      println("🎉 Scala 3 sealed traits + case classes with comprehensive actor patterns")
      println("💎 Complete Actor Model implementation with modern Scala features")

    } catch {
      case exception: Exception =>
        println(s"❌ Application failed: ${exception.getMessage}")
        exception.printStackTrace()
    } finally {
      system.terminate()
    }
  }
}
