package ControllerTests

import BasicChess.StandartChess.ChessBoard
import Controller.SoloChessController.EngineController
import Controller.DuoChessController.RealController
import Controller.Extra.{ChessContext, Event, State}
import Controller.StateComponent.xmlSolution.XMLApi
import Controller.StateComponent.{ApiFileTrait, DataWrapper}
import RealChess.RealChessFacade
import SharedResources.ChessTrait
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import SharedResources.util.Observer

import scala.util.Success
import scala.xml.XML

class EngineControllerSpec extends AnyWordSpec {
    def unpackToFen(dataWrapped: DataWrapper, fileApi: ApiFileTrait): String = {
        val data: (String, State) = fileApi.from(dataWrapped)
        data._2 match {
            case State.remisState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.whiteWonState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.blackWonState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case _ => data._1
        }
    }

    given ApiFileTrait = XMLApi()

    given ChessTrait = RealChessFacade()

    val fileApi = XMLApi()
    val xmlContent: scala.xml.Node = XML.loadFile("src/main/resources/GameState.xml")
    val wrapper: DataWrapper = DataWrapper(Some(xmlContent), None)
    val arg1 = unpackToFen(wrapper, fileApi)
    val arg2 = new ChessContext
    val arg3 = ""
    val ec = EngineController(arg1, arg2, arg3, 10)

    "EngineControllerSpec" should {
        "resetBoard Test" in {
            val b = ec.resetBoard()
            ec.fen should be("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        }

        "createOutput Test" in {
            ec.createOutput() should be(ec.output)
        }

        "play Test" in {
            ec.resetBoard()
            ec.play(Success(ChessBoard.translateMoveStringToInt(ec.fen, "e2e4")))
            ec.fen should be ("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2")
        }

        "checkGameState Test" in {
            ec.checkGameState(List()) should be (true)
        }


        "promote correctly" in {
            ec.fen = "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR w KQkq - 0 5"
            ec.promotePawn("Q")
            ec.fen should be ("rqbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR w KQkq - 0 5")

        }

        "do redo and undo correctly" in {
            ec.fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            ec.play(Success(ChessBoard.moveToIndex("e2","e4")))
            val save = ec.fen
            ec.undo()
            ec.undo()
            ec.fen should be ("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

            ec.redo()
            ec.redo()
            ec.fen should be(save)
        }

        "implement squareClicked correctly" in {
            ec.fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            ec.squareClicked(Success(7))
            ec.activeSquare should be(None)

            ec.squareClicked(Success(60))
            ec.activeSquare should be(Some(60))

        }

        "switch themes correctly" in {
            ec.fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            ec.nextTheme()
            ec.current_theme = 1
        }
    }
}