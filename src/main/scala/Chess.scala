import scala.io.StdIn.readLine
import Model.ChessComponent.BasicChess.StandartChess.ChessBoard
import Model.ChessComponent.ChessTrait
import aView.GUIComponent.GuiMain
import aView.TUIComponent.Tui
import cController.ControllerComponent.RealChessController.{ChessContext, Controller}
import Model.ChessComponent.Default.given

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Chess {
    val gameMode: ChessTrait = summon[ChessTrait]
    val controller = new Controller(using gameMode, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", new ChessContext(), ChessBoard.getBoardString(ChessBoard.getDefaultBoard()));
    val tui = new Tui(controller)
    controller.notifyObservers


    def main(args: Array[String]): Unit = {

        GuiMain.setController(controller)
        Future {
            GuiMain.main(args)
        }

        var input: String = ""
        while (input != "end") {
            input = readLine()
            tui.processInputLine(input)
        }
    }
}
