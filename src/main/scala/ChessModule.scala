import Model.ChessComponent.ChessTrait
import Model.ChessComponent.DevourChess.DevourChessFacade
import Model.ChessComponent.RealChess.RealChessFacade
import cController.ControllerComponent.ControllerTrait
import cController.ControllerComponent.Extra.{ChessContext, State}
import cController.ControllerComponent.RealChessController.Controller
import cController.ControllerComponent.SoloChessController.EngineController
import cController.ControllerComponent.StateComponent.jsonSolution.JSONApi
import cController.ControllerComponent.StateComponent.{ApiFileTrait, DataWrapper}
import cController.ControllerComponent.StateComponent.xmlSolution.XMLApi
import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.{Named, Names}
import play.api.libs.json.*

import scala.io.Source
import scala.language.postfixOps
import scala.xml.XML

object ChessModule {


    def unpackToFen(dataWrapped: DataWrapper, fileApi: ApiFileTrait): String = {
        val data: (String, State) = fileApi.from(dataWrapped)
        data._2 match {
            case State.remisState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.whiteWonState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.blackWonState => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case _ => data._1
        }
    }


    def provideDuoChessXML(): ControllerTrait = {
        given ApiFileTrait = XMLApi()
        given ChessTrait = RealChessFacade()
        val fileApi = XMLApi()
        val xmlContent: scala.xml.Node = XML.loadFile("src/main/resources/GameState.xml")
        val wrapper: DataWrapper = DataWrapper(Some(xmlContent), None)
        val arg1 = unpackToFen(wrapper, fileApi)
        val arg2 = new ChessContext
        val arg3 = ""
        new Controller(arg1, arg2, arg3)
    }

    def provideDuoChessJSON(): ControllerTrait = {
        given ApiFileTrait = JSONApi()
        given ChessTrait = RealChessFacade()
        val fileApi = JSONApi()
        val filePath = "src/main/resources/GameState.json"
        val fileContents = Source.fromFile(filePath).getLines().mkString
        val json: JsValue = Json.parse(fileContents)
        val wrapper: DataWrapper = DataWrapper(None, Some(json))
        val arg1 = unpackToFen(wrapper, fileApi)
        val arg2 = new ChessContext
        val arg3 = ""
        new Controller(arg1, arg2, arg3)
    }

    def provideEngineChessXML(): ControllerTrait = {
        given ApiFileTrait = XMLApi()
        given ChessTrait = RealChessFacade()

        val fileApi = XMLApi()
        val xmlContent: scala.xml.Node = XML.loadFile("src/main/resources/GameState.xml")
        val wrapper: DataWrapper = DataWrapper(Some(xmlContent), None)
        val arg1 = unpackToFen(wrapper, fileApi)
        val arg2 = new ChessContext
        val arg3 = ""
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
        val arg3 = ""
        new EngineController(arg1, arg2, arg3, 15)
    }
}