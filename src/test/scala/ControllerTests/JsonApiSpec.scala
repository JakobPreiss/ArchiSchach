package ControllerTests

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import cController.ControllerComponent.StateComponent.jsonSolution.JSONApi
import cController.ControllerComponent.Extra.{ChessContext, State}
import cController.ControllerComponent.Extra.State.whitePlayingState

class JsonApiSpec extends AnyWordSpec with Matchers {
    val instance = new JSONApi
    "JsonApi" should {
        "convert to Json correctly and read from a Json correctly" in {
            val context: ChessContext = new ChessContext
            context.state = whitePlayingState
            val data = instance.to(context, "rnbqkbnr/Ppppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            data.getJson().toString should be ("{\"Box\":{\"fen\":\"rnbqkbnr/Ppppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5\",\"state\":0}}")

            val output : (String, State) = instance.from(data)
            output._1 should be ("rnbqkbnr/Ppppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            output._2 should be (whitePlayingState)
        }
    }
}
