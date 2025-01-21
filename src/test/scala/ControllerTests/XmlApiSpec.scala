package ControllerTests

import cController.ControllerComponent.Extra.State.whitePlayingState
import cController.ControllerComponent.Extra.{ChessContext, State}
import cController.ControllerComponent.StateComponent.xmlSolution.XMLApi
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class XmlApiSpec extends AnyWordSpec with Matchers {
    val instance = new XMLApi
    "JsonApi" should {
        "convert to Json correctly and read from a Json correctly" in {
            val context: ChessContext = new ChessContext
            context.state = whitePlayingState
            val data = instance.to(context, "rnbqkbnr/Ppppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            data.getNode().toString should be ("<box><fen>rnbqkbnr/Ppppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5</fen><state>0</state></box>")

            val output : (String, State) = instance.from(data)
            output._1 should be ("rnbqkbnr/Ppppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            output._2 should be (whitePlayingState)
        }
    }
}
