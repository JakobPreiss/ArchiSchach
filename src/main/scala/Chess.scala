import RealChess.RealChessFacade

import scala.io.StdIn.readLine
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import TUI.Tui
import GUI.GuiMain
import SharedResources.ChessTrait

object Chess {
    given ChessTrait = RealChessFacade()
    val controller = ChessModule.provideDuoChessJSON()
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
