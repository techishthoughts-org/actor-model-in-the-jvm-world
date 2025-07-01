package demo.actors.stateful

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import demo.actors.stateful.messages.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*

class StatefulActorSpec extends TestKit(ActorSystem("StatefulActorSpec"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "StatefulActor" should {
    "start with correct initial status" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.Idle))

      actor ! GetStatusStateful

      expectMsg(WorkerStatusStateful.Idle)
    }

    "transition from Idle to WaitingResponse on AskForHelp" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.Idle))

      actor ! AskForHelpStateful("Can you help me?")

      expectMsg(HelpResponseStateful("I will help you!"))

      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.WaitingResponse)
    }

    "transition from Available to InMeeting on AskForHelp" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.Available))

      actor ! AskForHelpStateful("Can you help me?")

      expectMsg(HelpResponseStateful("I will help you!"))

      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.WaitingResponse)
    }

    "handle HelpResponse when Available" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.Available))

      actor ! HelpResponseStateful("I can help!")

      expectMsg(SendMeetingLinkStateful("Can you join in this meeting link ?"))

      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.InMeeting)
    }

    "handle SendMeetingLink when Available" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.Available))

      actor ! SendMeetingLinkStateful("Meeting link here")

      expectMsg(JoinMeetingStateful("Yes, I can join"))

      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.InMeeting)
    }

    "handle SendMeetingLink when WaitingResponse" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.WaitingResponse))

      actor ! SendMeetingLinkStateful("Meeting link here")

      expectMsg(JoinMeetingStateful("Yes, I can join"))

      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.InMeeting)
    }

    "handle JoinMeeting when Available" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.Available))

      actor ! JoinMeetingStateful("Joining now")

      expectMsg(JoinMeetingStateful("Joining the meeting"))

      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.InMeeting)
    }

    "handle JoinMeeting when already InMeeting" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.InMeeting))

      actor ! JoinMeetingStateful("Joining now")

      expectMsg(InMeetingStateful("Joining the meeting"))

      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.InMeeting)
    }

    "reject AskForHelp when InMeeting" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.InMeeting))

      actor ! AskForHelpStateful("Can you help me?")

      // Should not respond as it's in an invalid state
      expectNoMessage(100.millis)
    }

    "complete workflow from Idle to InMeeting" in {
      val actor = system.actorOf(StatefulActor.props("TestWorker", WorkerStatusStateful.Idle))

      // Step 1: Ask for help
      actor ! AskForHelpStateful("Need help")
      expectMsg(HelpResponseStateful("I will help you!"))

      // Step 2: Send meeting link
      actor ! SendMeetingLinkStateful("Meeting link")
      expectMsg(JoinMeetingStateful("Yes, I can join"))

      // Verify final state
      actor ! GetStatusStateful
      expectMsg(WorkerStatusStateful.InMeeting)
    }
  }
}
