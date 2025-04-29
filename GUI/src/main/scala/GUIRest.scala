package GUI

import JsonProtocols.*
import Requests.MoveRequest.jsonFormat2
import SharedResources.{ChessContext, GenericHttpClient, JsonResult}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onComplete, parameter, parameters, path, pathPrefix, post}
import akka.http.scaladsl.server.Route
import spray.json.{DefaultJsonProtocol, JsValue, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import GUI.{GuiBoard, GuiMain, GuiMenu, GuiPromoWindow}
import SharedResources.GenericHttpClient.ec

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}



// JSON formats for domain types
object JsonProtocols extends DefaultJsonProtocol {
    case class InputPayload(input: String)
    implicit val inputFormat: RootJsonFormat[InputPayload] = jsonFormat1(InputPayload)
}

class GuiRoutes(board: GuiBoard, menu: GuiMenu, window: GuiPromoWindow)(implicit system: ActorSystem) {

    val routes: Route =
        pathPrefix("tui") {
            concat(
                path("update") {
                    post {
                        board.update
                        menu.update
                        complete("Updated")
                    }
                },
                path("special") {
                    post {
                        window.specialCase
                        complete("Special case triggered")
                    }
                },
                path("reverse") {
                    post {
                        board.reverseSpecialCase
                        window.reverseSpecialCase
                        complete("Reverse special case triggered")
                    }
                },
                path("error") {
                    post {
                        window.errorDisplay
                        complete("Error displayed")
                    }
                }
            )
        }
}

object GuiServer extends App {
    implicit val system: ActorSystem = ActorSystem("ControllerSystem")

    var observers: ListBuffer[String] = ListBuffer() // stores base URLs like "http://localhost:8081"

    val board = new GuiBoard()
    val menu = new GuiMenu()
    val window = new GuiPromoWindow()
    GuiMain.setComponents(board, menu, window)
    
    val routes = new GuiRoutes(board, menu, window).routes

    val registerNotifier: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
        baseUrl = "http://controller:8080",
        route = "/controller/controller/register",
        queryParams = Map("url" -> "http://gui:8080/gui")
    )
    registerNotifier.onComplete {
        case Success(value) => println("Registered successfully")
        case Failure(exception) => println(s"Failed to register: ${exception.getMessage}")
    }

    Http().newServerAt("0.0.0.0", 8080).bind(routes)
    println("Controller REST API running at http://0.0.0.0:8080/")
}