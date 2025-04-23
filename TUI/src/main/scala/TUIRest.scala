
import Controller.ControllerTrait
import Controller.JsonProtocols.jsonFormat2
import JsonProtocols.*
import Requests.MoveRequest.jsonFormat2
import SharedResources.{ChessContext, JsonResult}
import TUI.Tui
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onComplete, parameter, parameters, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import TUI.Tui
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}



// JSON formats for domain types
object JsonProtocols extends DefaultJsonProtocol {
    case class InputPayload(input: String)
    implicit val inputFormat: RootJsonFormat[InputPayload] = jsonFormat1(InputPayload)
}

class TuiRoutes(tui: Tui)(implicit system: ActorSystem) {

    val routes: Route =
        pathPrefix("tui") {
            concat(
                path("processInputLine") {
                    post {
                        entity(as[InputPayload]) { payload =>
                            tui.processInputLine(payload.input)
                            complete(StatusCodes.OK, s"Processed input: '${payload.input}'")
                        }
                    }
                },
                path("update") {
                    post {
                        tui.update
                        complete("Updated")
                    }
                },
                path("special") {
                    post {
                        tui.specialCase
                        complete("Special case triggered")
                    }
                },
                path("reverse") {
                    post {
                        tui.reverseSpecialCase
                        complete("Reverse special case triggered")
                    }
                },
                path("error") {
                    post {
                        tui.errorDisplay
                        complete("Error displayed")
                    }
                }
            )
        }
}

object ControllerServer extends App {
    implicit val system: ActorSystem = ActorSystem("ControllerSystem")

    var observers: ListBuffer[String] = ListBuffer() // stores base URLs like "http://localhost:8081"

    val controller: ControllerTrait = ??? // Replace with your controller
    val tui = new Tui(controller)

    val routes = new TuiRoutes(tui).routes

    Http().newServerAt("0.0.0.0", 5005).bind(routes)
    println("Controller REST API running at http://localhost:5005/")
}