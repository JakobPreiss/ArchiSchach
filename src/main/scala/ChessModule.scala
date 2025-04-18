import BasicChess.StandartChess.ChessBoard
import Controller.ControllerTrait
import Controller.DuoChessController.RealController
import Controller.Extra.{ChessContext, State}
import Controller.SoloChessController.EngineController
import Controller.StateComponent.jsonSolution.JSONApi
import Controller.StateComponent.xmlSolution.XMLApi
import Controller.StateComponent.{ApiFileTrait, DataWrapper}
import RealChess.RealChessFacade
import SharedResources.ChessTrait
import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.{Named, Names}
import play.api.libs.json.*

import scala.io.Source
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.xml.XML

object ChessModule {
    def unpackToFen(dataWrapped: DataWrapper, fileApi: ApiFileTrait): String = {
        val data: (String, State) = fileApi.from(dataWrapped)
        data._2 match {
            case State.remisState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.whiteWonState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.blackWonState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case _ =>
                ChessBoard.isValidFen(data._1) match {
                    case Success(newFen) => newFen
                    case Failure(error) => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                }
        }
    } 


    def provideDuoChessXML(): ControllerTrait = {
        given ApiFileTrait = XMLApi()
        given ChessTrait = RealChessFacade()
        val fileApi = XMLApi()
        val xmlContent: Try[scala.xml.Node] = Try(XML.loadFile("src/main/resources/GameState.xml"))
        val wrapper: DataWrapper = xmlContent match {
            case Success(content) => DataWrapper(Some(content), None)
            case Failure(content) => DataWrapper(None, None)
        }
        val arg1 = unpackToFen(wrapper, fileApi)
        val arg2 = new ChessContext
        val arg3 = ChessBoard.getBoardString(ChessBoard.fenToBoard(arg1))
        new RealController(arg1, arg2, arg3)
    }

    def provideDuoChessJSON(): ControllerTrait = {
        given ApiFileTrait = JSONApi()
        given ChessTrait = RealChessFacade()
        val fileApi = JSONApi()
        val filePath = "src/main/resources/GameState.json"
        val fileContents = Try(Source.fromFile(filePath).getLines().mkString) match {
            case Success(content) => content
            case Failure(content) => defaultJson()
        }
        val json: JsValue = Try(Json.parse(fileContents)) match {
            case Success(content) => content
            case Failure(content) => Json.parse(defaultJson())
        }
        val wrapper: DataWrapper = DataWrapper(None, Some(json))
        val arg1 = unpackToFen(wrapper, fileApi)
        val arg2 = new ChessContext
        val arg3 = ChessBoard.getBoardString(ChessBoard.getDefaultBoard())
        new RealController(arg1, arg2, arg3)
    }

    def provideEngineChessXML(): ControllerTrait = {
        given ApiFileTrait = XMLApi()
        given ChessTrait = RealChessFacade()

        val fileApi = XMLApi()
        val xmlContent: scala.xml.Node = XML.loadFile("src/main/resources/GameState.xml")
        val wrapper: DataWrapper = DataWrapper(Some(xmlContent), None)
        val arg1 = unpackToFen(wrapper, fileApi)
        val arg2 = new ChessContext
        val arg3 = ChessBoard.getBoardString(ChessBoard.getDefaultBoard())
        new EngineController(arg1, arg2, arg3, 10)
    }

    def provideEngineChessJSON(): ControllerTrait = {
        given ApiFileTrait = JSONApi()

        given ChessTrait = RealChessFacade()

        val fileApi = JSONApi()
        val filePath = "src/main/resources/GameState.json"
        val fileContents = Source.fromFile(filePath).getLines().mkString
        val json: JsValue = Json.parse(fileContents)
        val wrapper: DataWrapper = DataWrapper(None, Some(json))
        val arg1 = unpackToFen(wrapper, fileApi)
        val arg2 = new ChessContext
        val arg3 = ChessBoard.getBoardString(ChessBoard.getDefaultBoard())
        new EngineController(arg1, arg2, arg3, 15)
    }

    private def defaultJson() = {
        "{\"Box\":{\"fen\":\"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\",\"state\":0}}"
    }
}