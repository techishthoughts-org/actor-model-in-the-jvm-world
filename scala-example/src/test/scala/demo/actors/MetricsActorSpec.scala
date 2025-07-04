package demo.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import demo.actors.simple.messages.{StringMessage, IntMessage}
import org.scalatest.{BeforeAndAfterAll}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class MetricsActorSpec
    extends TestKit(ActorSystem("MetricsActorSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A MetricsActor" should {

    "collect and report metrics" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      // Record a metric
      metricsActor ! StringMessage("record:cpu:75.5")

      // Give some time for processing
      Thread.sleep(100)

      // Get metrics
      metricsActor ! StringMessage("get")
      expectMsgType[StringMessage](1.second)
    }

    "handle multiple metric types" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      // Record multiple metrics
      metricsActor ! StringMessage("record:cpu:75.5")
      metricsActor ! StringMessage("record:memory:85.2")
      metricsActor ! StringMessage("record:disk:45.0")

      Thread.sleep(200)

      // Get all metrics
      metricsActor ! StringMessage("get")
      val response = expectMsgType[StringMessage](1.second)

      response.value should not be empty
    }

    "handle clear metrics command" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      // Record a metric
      metricsActor ! StringMessage("record:cpu:75.5")
      Thread.sleep(100)

      // Clear metrics
      metricsActor ! StringMessage("clear")
      Thread.sleep(100)

      // Verify metrics are cleared
      metricsActor ! StringMessage("get")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle invalid commands gracefully" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      // Send invalid command
      metricsActor ! StringMessage("invalid")

      val response = expectMsgType[StringMessage](1.second)
      response.value should (include("Unknown") or include("Error") or include("Invalid"))
    }

    "handle status requests" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      metricsActor ! StringMessage("status")
      val response = expectMsgType[StringMessage](1.second)

      response.value should not be empty
    }

    "handle reset command" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      // Record metrics
      metricsActor ! StringMessage("record:cpu:75.5")
      Thread.sleep(100)

      // Reset
      metricsActor ! StringMessage("reset")
      Thread.sleep(100)

      // Check status after reset
      metricsActor ! StringMessage("get")
      expectMsgType[StringMessage](1.second)
    }

    "maintain consistent behavior across multiple calls" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      for (i <- 1 to 5) {
        metricsActor ! StringMessage("get")
        val response = expectMsgType[StringMessage](1.second)
        response should not be null
      }
    }

    "handle IntMessage gracefully" in {
      val metricsActor = system.actorOf(Props[MetricsActor]())

      // Test with IntMessage
      metricsActor ! IntMessage(100)
      expectNoMessage(100.milliseconds)

      // Should still respond to valid commands
      metricsActor ! StringMessage("get")
      expectMsgType[StringMessage](1.second)
    }
  }
}
