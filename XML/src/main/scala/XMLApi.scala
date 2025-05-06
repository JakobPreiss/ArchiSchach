package XML

import SharedResources.{ApiFileTrait, ChessContext, DataWrapper, GenericHttpClient, JsonResult, State}

import java.io.*
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import SharedResources.GenericHttpClient.ec
import SharedResources.ChessJsonProtocol.StringJsonFormat
import scala.xml.XML

class XMLApi extends ApiFileTrait {
    def to(contextStateOrdinal: Int, fen: String): DataWrapper = {
        val xmldata = <box><fen>{fen}</fen><state>{contextStateOrdinal}</state></box>
        DataWrapper(Some(xmldata), None)
    }

    def from: String = {
        val xmlContent: Try[scala.xml.Node] = Try(XML.loadFile("src/main/resources/GameState.xml"))
        val wrapper: DataWrapper = xmlContent match {
            case Success(content) => DataWrapper(Some(content), None)
            case Failure(content) => DataWrapper(None, None)
        }
        val node : scala.xml.Node = wrapper.getNode()
        unpackToFen(wrapper,  ((node \ "fen").text, State.fromOrdinal((node \ "state").text.toInt)))
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

    def printTo(contextStateOrdinal: Int, fen : String) = {
        val writer = new PrintWriter(new File("src/main/resources/GameState.xml"))
        val data = to(contextStateOrdinal, fen).getNode()
        writer.write(data.toString())
        writer.close()
    }
}
