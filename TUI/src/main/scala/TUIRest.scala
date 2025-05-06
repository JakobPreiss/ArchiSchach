package TUI

import JsonProtocols.*
import Requests.MoveRequest.jsonFormat2
import SharedResources.{ChessContext, GenericHttpClient, JsonResult}
import TUI.Tui
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onComplete, parameter, parameters, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import TUI.Tui
import SharedResources.GenericHttpClient.ec

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.io.StdIn
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

object TuiServer extends App {
    implicit val system: ActorSystem = ActorSystem("ControllerSystem")

    var observers: ListBuffer[String] = ListBuffer() // stores base URLs like "http://localhost:8081"

    val tui = new Tui()

    val routes = new TuiRoutes(tui).routes

    val registerNotifier: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
        baseUrl = "http://controller:8080",
        route = "/controller/register",
        queryParams = Map("url" -> "http://tui:8080/tui")
    )
    registerNotifier.onComplete {
        case Success(value) => println("Registered successfully")
        case Failure(exception) => println(s"Failed to register: ${exception.getMessage}")
    }

    Http().newServerAt("0.0.0.0", 8080).bind(routes).onComplete {
        case Success(_) =>
            println("Controller REST API running at http://0.0.0.0:8080/")

            //–– once the server is up, spin up your console loop ––
            Future {
                var input: String = ""
                while (input != "end") {
                    input = StdIn.readLine()
                    tui.processInputLine(input)
                }
                println("TUI loop terminated, shutting down…")
                system.terminate()
            }

        case Failure(ex) =>
            println(s"Failed to bind HTTP endpoint, terminating: ${ex.getMessage}")
            system.terminate()
    }
}