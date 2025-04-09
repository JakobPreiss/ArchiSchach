package ControllerTests

import Model.ChessComponent.ChessTrait
import Model.ChessComponent.RealChess.RealChessFacade
import cController.ControllerComponent.Extra.ChessContext
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import scala.util.{Try, Success, Failure}

class UndoRedoSpec extends AnyWordSpec {
    "UndoInvoker" should {
        "undo and redo moves correctly" in {
            val controller = new ControllerStub("0-0")
            controller.play(Success(1, 2)) //e2e4
            controller.play(Success(3, 4)) //e7e5
            controller.fen should be ("3-4")

            controller.undo()
            controller.fen should be ("1-2")

            controller.undo()
            controller.fen should be ("0-0")

            controller.redo()
            controller.fen should be ("1-2")

            controller.redo()
            controller.fen should be ("3-4")

            controller.redo()
            controller.fen should be ("3-4")

        }
    }
}
