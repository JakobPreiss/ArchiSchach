package JSON;

import SharedResources.{ApiFileTrait, ChessContext, DataWrapper, GenericHttpClient, JsonResult, State}
import play.api.libs.json.{JsValue, Json}

import java.io.{File, PrintWriter}
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try}
import SharedResources.GenericHttpClient.ec

import SharedResources.GenericHttpClient.StringJsonFormat

class JSONApi extends ApiFileTrait {
    def to(context: ChessContext, fen: String): DataWrapper = {
        val stateNumber = context.state.ordinal
        val jsonData = Json.parse(s"{\"Box\": {\"fen\": \"$fen\", \"state\": $stateNumber}}")
        DataWrapper(None, Some(jsonData))
    }

    def from: String = {
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

        val value: JsValue = wrapper.getJson()
        val test = (value \ "Box" \ "fen").get
        val falseFen = Json.stringify((value \ "Box" \ "fen").get)
        val correctFen = falseFen.substring(1, falseFen.length - 1)
        unpackToFen(wrapper, (correctFen, State.fromOrdinal(Json.stringify((value \ "Box" \ "state").get).toInt)))
    }

    def unpackToFen(dataWrapped: DataWrapper, data: (String, State)): String = {
        data._2 match {
            case State.Remis => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.WhiteWon => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case State.BlackWon => "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            case _ =>
                val translation: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/isValidFen",
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

    private def defaultJson() = {
        "{\"Box\":{\"fen\":\"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\",\"state\":0}}"
    }

    def printTo(context: ChessContext, fen: String) = {
        val writer = new PrintWriter(new File("src/main/resources/GameState.json"))
        val data: JsValue  = to(context, fen).getJson()
        writer.write(Json.stringify(data))
        writer.close()
    }
}
