package demo.actors

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*

class DemoExecutorHelperSpec extends AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem("DemoExecutorHelperSpec")

  override def afterAll(): Unit = {
    system.terminate()
  }

  "DemoExecutorHelper" should {
    "execute stateful worker example without errors" in {
      // This test ensures the demo runs without exceptions
      noException should be thrownBy {
        DemoExecutorHelper.statefulWorkerExampleWithLogging(system)

        // Give actors time to process messages
        Thread.sleep(200)
      }
    }

    "execute stateless worker example without errors" in {
      noException should be thrownBy {
        DemoExecutorHelper.statelessWorkerExampleWithLogging(system)

        // Give actors time to process messages
        Thread.sleep(200)
      }
    }

    "execute simple actor sender example without errors" in {
      noException should be thrownBy {
        DemoExecutorHelper.simpleActorExampleUsingSender(system)

        // Give actors time to process messages
        Thread.sleep(100)
      }
    }

    "execute simple actor ask example without errors" in {
      noException should be thrownBy {
        DemoExecutorHelper.simpleActorExampleUsingAsk(system)
      }
    }
  }
}
