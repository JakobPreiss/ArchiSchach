import SharedResources.{ChessContext, JsonResult}
import play.api.libs.json.{JsValue, Json, *}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.model.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import spray.json.*
import DefaultJsonProtocol.*
import GameStatePayload.{DataWrapper, GameStatePayload}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.xml.Utility


object GameStatePayload {
    case class GameStatePayload(fen: String, state: Int)
    implicit val format: Format[GameStatePayload] = Json.format[GameStatePayload]
    case class DataWrapper(node : Option[scala.xml.Node], json : Option[JsValue])
    implicit val wrapperFormat: Format[DataWrapper] = Json.format[DataWrapper]
}



class ApiRoutes(xmlApi: XMLApi) {

    val routes: Route = pathPrefix("xmlapi") {
        concat(
            path("to") {
                get {
                    parameters("context", "fen") { (context, fen) =>
                        onComplete(xmlApi.to(context, fen))
                        complete(JsonResult(DataWrapper(, to)))
                    }
                }
            },

            path("from") {
                get {
                    val data = xmlApi.to(context, context.fen)
                    val node = data.getNode()
                    val fen = (node \ "fen").text
                    val state = (node \ "state").text.toInt
                    complete(GameStatePayload(fen, state))
                }
            },

            path("print-to") {
                post {
                    entity(as[GameStatePayload]) { payload =>
                        val node = <box><fen>{payload.fen}</fen><state>{payload.state}</state></box>
                        val data = DataWrapper(Some(node), None)
                        val (fen, state) = xmlApi.from(data)
                        complete(GameStatePayload(fen, state.ordinal))
                    }
                }
            },
        )
    }
}


object XMLServer extends App {
    implicit val system: ActorSystem = ActorSystem("xml-api-system")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val xmlApi = new XMLApi()
    val context = ChessContext("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", State.WhiteTurn) // example

    val apiRoutes = new ApiRoutes(xmlApi, context)

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(apiRoutes.routes)
    println(s"Server now online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine()
    bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
}

