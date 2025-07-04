package demo.actors.stateless

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import demo.actors.stateless.StatelessActor
import demo.actors.stateless.messages._
import org.scalatest.{BeforeAndAfterAll}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class StatelessActorSpec
    extends TestKit(ActorSystem("StatelessActorSpec"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A StatelessActor" should {

    "handle help requests" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      statelessActor ! AskForHelpStateless("Need assistance with computation")
      val response = expectMsgType[HelpResponseStateless](1.second)

      response.message should not be empty
    }

    "always report not in meeting" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      statelessActor ! GetStatusStateless()
      val status = expectMsgType[WorkerStatusStateless](1.second)

      status should not be WorkerStatusStateless.InMeeting
    }

    "provide consistent responses" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      // Multiple status requests should be consistent
      for (i <- 1 to 3) {
        statelessActor ! GetStatusStateless()
        val status = expectMsgType[WorkerStatusStateless](1.second)
        status should not be WorkerStatusStateless.InMeeting
      }
    }

    "handle multiple help requests" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      val requests = List(
        "Help with calculation",
        "Need algorithm advice",
        "Assistance required"
      )

      requests.foreach { request =>
        statelessActor ! AskForHelpStateless(request)
        val response = expectMsgType[HelpResponseStateless](1.second)
        response.message should not be empty
      }
    }

    "handle meeting requests appropriately" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      statelessActor ! JoinMeetingStateless("TestMeeting")

      // Should handle meeting requests (may send response or ignore)
      expectMsgPF(1.second) {
        case _: StatelessMessage => true
        case _ => false
      }
    }

    "handle in-meeting messages" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      statelessActor ! InMeetingStateless("not in meeting")

      // Should handle gracefully (stateless actors don't maintain meeting state)
      expectNoMessage(100.milliseconds)

      // Verify status remains unchanged
      statelessActor ! GetStatusStateless()
      val status = expectMsgType[WorkerStatusStateless](1.second)
      status should not be WorkerStatusStateless.InMeeting
    }

    "handle meeting link messages" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      statelessActor ! SendMeetingLinkStateless("http://meeting.example.com")

      // Should handle meeting link messages appropriately
      expectNoMessage(100.milliseconds)
    }

    "maintain stateless behavior" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      // Send various messages and verify consistent stateless behavior
      statelessActor ! AskForHelpStateless("Help 1")
      expectMsgType[HelpResponseStateless](1.second)

      statelessActor ! GetStatusStateless()
      val status1 = expectMsgType[WorkerStatusStateless](1.second)

      statelessActor ! JoinMeetingStateless("Meeting")
      expectMsgPF(1.second) {
        case _: StatelessMessage => true
        case _ => false
      }

      statelessActor ! GetStatusStateless()
      val status2 = expectMsgType[WorkerStatusStateless](1.second)

      // Status should be consistent (not affected by meeting request)
      status1 should equal(status2)
    }

    "handle concurrent requests" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      // Send multiple concurrent requests
      for (i <- 1 to 5) {
        statelessActor ! AskForHelpStateless(s"Concurrent help request $i")
      }

      // Should handle all requests
      for (i <- 1 to 5) {
        expectMsgType[HelpResponseStateless](1.second)
      }
    }

    "handle unknown message types gracefully" in {
      val statelessActor = system.actorOf(Props[StatelessActor]())

      // Send unknown message type
      statelessActor ! "unknown message"

      // Should handle gracefully without crashing
      expectNoMessage(100.milliseconds)

      // Verify actor is still responsive
      statelessActor ! GetStatusStateless()
      expectMsgType[WorkerStatusStateless](1.second)
    }
  }
}
