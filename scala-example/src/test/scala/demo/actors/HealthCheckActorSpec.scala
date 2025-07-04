package demo.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import demo.actors.simple.messages.{StringMessage, IntMessage}
import org.scalatest.{BeforeAndAfterAll}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class HealthCheckActorSpec
    extends TestKit(ActorSystem("HealthCheckActorSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A HealthCheckActor" should {

    "report healthy status" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("health")
      val response = expectMsgType[StringMessage](1.second)

      response.value.toLowerCase should (include("healthy") or include("ok") or include("good"))
    }

    "handle status requests" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("status")
      val response = expectMsgType[StringMessage](1.second)

      response.value should not be empty
    }

    "provide detailed health information" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("detailed")
      val response = expectMsgType[StringMessage](1.second)

      response.value.length should be > 10
    }

    "handle check command" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("check")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle ping requests" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("ping")
      val response = expectMsgType[StringMessage](1.second)

      response.value should (include("pong") or include("alive") or include("ok"))
    }

    "handle unknown commands gracefully" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("unknown")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "respond consistently to multiple health checks" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      for (i <- 1 to 3) {
        healthActor ! StringMessage("health")
        val response = expectMsgType[StringMessage](1.second)
        response should not be null
      }
    }

    "handle system information requests" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("system")
      val response = expectMsgType[StringMessage](1.second)

      response.value should not be empty
    }

    "handle memory information requests" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("memory")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle uptime requests" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      healthActor ! StringMessage("uptime")
      val response = expectMsgType[StringMessage](1.second)

      response.value should not be empty
    }

    "handle IntMessage gracefully" in {
      val healthActor = system.actorOf(Props[HealthCheckActor]())

      // Test with IntMessage
      healthActor ! IntMessage(123)
      expectNoMessage(100.milliseconds)

      // Should still respond to valid commands
      healthActor ! StringMessage("health")
      expectMsgType[StringMessage](1.second)
    }
  }
}
