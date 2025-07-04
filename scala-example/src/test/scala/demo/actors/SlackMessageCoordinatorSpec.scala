package demo.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import demo.actors.simple.messages.{StringMessage, IntMessage}
import org.scalatest.{BeforeAndAfterAll}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class SlackMessageCoordinatorSpec
    extends TestKit(ActorSystem("SlackMessageCoordinatorSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A SlackMessageCoordinator" should {

    "handle message routing" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("route:general:Hello team!")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle channel creation" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("create:channel:new-channel")
      val response = expectMsgType[StringMessage](1.second)

      response.value should (include("created") or include("channel") or include("success"))
    }

    "handle user management" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("add:user:john.doe")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle message broadcasting" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("broadcast:Important announcement")
      val response = expectMsgType[StringMessage](1.second)

      response.value should (include("broadcast") or include("sent") or include("delivered"))
    }

    "handle status requests" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("status")
      val response = expectMsgType[StringMessage](1.second)

      response.value should not be empty
    }

    "handle list channels request" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("list:channels")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle list users request" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("list:users")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle direct messages" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("dm:user123:Hello there!")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle invalid commands gracefully" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("invalid:command")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle message history requests" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("history:general")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "coordinate multiple message operations" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      // Send multiple operations
      coordinator ! StringMessage("create:channel:test-channel")
      expectMsgType[StringMessage](1.second)

      coordinator ! StringMessage("route:test-channel:Test message")
      expectMsgType[StringMessage](1.second)

      coordinator ! StringMessage("list:channels")
      val response = expectMsgType[StringMessage](1.second)
      response should not be null
    }

    "handle configuration requests" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      coordinator ! StringMessage("config")
      val response = expectMsgType[StringMessage](1.second)

      response should not be null
    }

    "handle IntMessage gracefully" in {
      val coordinator = system.actorOf(Props[SlackMessageCoordinator]())

      // Test with IntMessage
      coordinator ! IntMessage(456)
      expectNoMessage(100.milliseconds)

      // Should still respond to valid commands
      coordinator ! StringMessage("status")
      expectMsgType[StringMessage](1.second)
    }
  }
}
