package aViewTests

import scala.language.reflectiveCalls
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import Controller.DuoChessController.RealController
import Controller.Extra.ChessContext
import Controller.StateComponent.ApiFileTrait
import Controller.StateComponent.xmlSolution.XMLApi
import RealChess.RealChessFacade
import SharedResources.ChessTrait
import TUI.Tui

class TuiSpec  extends AnyWordSpec with Matchers{
    given ChessTrait = RealChessFacade()
    given ApiFileTrait = XMLApi()
    "Chess Tui" should {
        "interpret move e2 to e4 correctly" in {
            val controller = new RealController("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", new ChessContext(), "")
            val tui = new Tui(controller)
            tui.processInputLine("e2e4")
            controller.fen should be("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
        }
        "interpret a wrong move hallo correctly" in {
            val controller = new RealController("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", new ChessContext(), "")
            val tui = new Tui(controller)
            tui.processInputLine("hallo")
            controller.fen should be("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        }
    }

}
