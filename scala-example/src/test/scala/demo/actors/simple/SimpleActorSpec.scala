package demo.actors.simple

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import demo.actors.simple.messages.DoubleMessage
import demo.actors.simple.messages.IntMessage
import demo.actors.simple.messages.StringMessage
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*

class SimpleActorSenderSpec extends TestKit(ActorSystem("SimpleActorSenderSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SimpleActorSender" should {
    "handle IntMessage correctly" in {
      val actor = system.actorOf(Props[SimpleActorSender]())

      actor ! IntMessage(42)

      // Actor should not reply for fire-and-forget pattern
      // Just verify it doesn't crash (no exception thrown)
      expectNoMessage(100.millis)
    }

    "handle StringMessage correctly" in {
      val actor = system.actorOf(Props[SimpleActorSender]())

      actor ! StringMessage("Hello, World!")

      expectNoMessage(100.millis)
    }

    "handle DoubleMessage correctly" in {
      val actor = system.actorOf(Props[SimpleActorSender]())

      actor ! DoubleMessage(3.14)

      expectNoMessage(100.millis)
    }

    "stop itself when receiving unexpected message" in {
      val probe = TestProbe()
      val actor = system.actorOf(Props[SimpleActorSender]())

      probe.watch(actor)
      actor ! "unexpected message"

      probe.expectTerminated(actor, 1.second)
    }
  }
}

class SimpleActorAskSpec extends TestKit(ActorSystem("SimpleActorAskSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SimpleActorAsk" should {
    "respond to IntMessage with same IntMessage" in {
      val actor = system.actorOf(Props[SimpleActorAsk]())

      actor ! IntMessage(42)

      expectMsg(IntMessage(42))
    }

    "respond to StringMessage with same StringMessage" in {
      val actor = system.actorOf(Props[SimpleActorAsk]())

      actor ! StringMessage("Hello, World!")

      expectMsg(StringMessage("Hello, World!"))
    }

    "respond to DoubleMessage with same DoubleMessage" in {
      val actor = system.actorOf(Props[SimpleActorAsk]())

      actor ! DoubleMessage(3.14)

      expectMsg(DoubleMessage(3.14))
    }

    "handle unexpected messages gracefully" in {
      val actor = system.actorOf(Props[SimpleActorAsk]())

      actor ! "unexpected message"

      // Should not crash, but may not respond
      expectNoMessage(100.millis)
    }
  }
}
