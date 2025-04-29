import RealChess.RealChessFacade

import scala.io.StdIn.readLine
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import TUI.Tui
import GUI.GuiMain
import SharedResources.ChessTrait

object Chess {
    val tui = new Tui()

    def main(args: Array[String]): Unit = {
        ChessModule.provideDuoChessJSON()

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
