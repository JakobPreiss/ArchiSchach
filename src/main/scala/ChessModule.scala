import Controller.ControllerTrait
import Controller.DuoChessController.RealController
import Controller.SoloChessController.EngineController
import JSON.JSONApi
import RealChess.{ChessApiClient, RealChessFacade}
import SharedResources.{ApiFileTrait, ChessContext, ChessTrait, DataWrapper, GenericHttpClient, JsonResult, State}
import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.{Named, Names}
import play.api.libs.json.*

import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.ec

import scala.concurrent.Future
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
                val translation: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                    baseUrl = "http://localhost:5001",
                    route = "/isValidFen",
                    queryParams = Map("fen" -> data._1)
                )
                translation.onComplete {
                    case Success(validFen) =>
                        return validFen.result
                    case Failure(err) =>
                        return "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                }
                
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
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

        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://localhost:5001",
            route = "/boardString",
            queryParams = Map("fen" -> arg1)
        )
        boardFuture.onComplete {
            case Success(value) =>
                val arg3 = value.result
                return new RealController(arg1, arg2, arg3)
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
                val arg3 = ""
                return new RealController(arg1, arg2, arg3)
        }
        
        val arg3 = ""
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

        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://localhost:5001",
            route = "/boardString",
            queryParams = Map("fen" -> arg1)
        )
        boardFuture.onComplete {
            case Success(value) =>
                val arg3 = value.result
                return new RealController(arg1, arg2, arg3)
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
                val arg3 = ""
                return new RealController(arg1, arg2, arg3)
        }

        val arg3 = ""
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

        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://localhost:5001",
            route = "/boardString",
            queryParams = Map("fen" -> arg1)
        )
        boardFuture.onComplete {
            case Success(value) =>
                val arg3 = value.result
                return new RealController(arg1, arg2, arg3)
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
                val arg3 = ""
                return new RealController(arg1, arg2, arg3)
        }

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

        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://localhost:5001",
            route = "/boardString",
            queryParams = Map("fen" -> arg1)
        )
        boardFuture.onComplete {
            case Success(value) =>
                val arg3 = value.result
                return new RealController(arg1, arg2, arg3)
            case Failure(err) =>
                println(s"Error: ${err.getMessage}")
                val arg3 = ""
                return new RealController(arg1, arg2, arg3)
        }

        val arg3 = ""
        new EngineController(arg1, arg2, arg3, 15)
    }

    private def defaultJson() = {
        "{\"Box\":{\"fen\":\"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\",\"state\":0}}"
    }
}