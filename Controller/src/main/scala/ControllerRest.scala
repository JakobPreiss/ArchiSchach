package Controller

import Controller.ControllerServer.{deRingObservers, notifyObservers, observers, ringObservers, tellErrorToObservers}
import Controller.DuoChessController.RealController
import Controller.SoloChessController.EngineController
import SharedResources.{ChessContext, JsonResult}
import SharedResources.util.{Observable, Observer}
import spray.json.*
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.server.Directives.*
import SharedResources.*
import SharedResources.ChessJsonProtocol.*
import SharedResources.Requests.{InitDuoRequest, InitEngineRequest}
import spray.json.DefaultJsonProtocol.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.model.HttpMethods.POST

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

// JSON formats for domain types
object JsonProtocols extends DefaultJsonProtocol {
  case class Move(from: Int, to: Int)
  implicit val moveFormat: RootJsonFormat[Move] = jsonFormat2(Move)
}

import JsonProtocols._

class ControllerRoutes(var controller: ControllerTrait)(implicit system: ActorSystem) {
  import system.dispatcher

  val routes: Route =
    pathPrefix("controller") {
      concat(
        // POST /controller/init
        path("init" / "engine") {
          post {
            entity(as[InitEngineRequest]) { req =>
              val newContext = new ChessContext()

              val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                baseUrl = "http://basic-chess:8080",
                route = "/chess/boardString",
                queryParams = Map("fen" -> req.fen)
              )
              boardFuture.onComplete {
                case Success(value) =>
                  val arg3 = value.result

                  this.controller = new EngineController(req.fen, newContext, arg3, req.depth, req.gameMode, req.api)
                case Failure(err) =>
                  println(s"Error: ${err.getMessage}")
                  val arg3 = ""
                  this.controller = new EngineController(req.fen, newContext, arg3, req.depth, req.gameMode, req.api)
              }

              complete(StatusCodes.OK, JsonResult(s"Initialized duo with FEN: ${req.fen}"))
            }
          }
        },
        path("init" / "duo") {
          post {
            entity(as[InitDuoRequest]) { req =>
              val newContext = new ChessContext()

              val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                baseUrl = "http://basic-chess:8080",
                route = "/chess/boardString",
                queryParams = Map("fen" -> req.fen)
              )
              boardFuture.onComplete {
                case Success(value) =>
                  val arg3 = value.result
                  this.controller = new RealController(req.fen, newContext, arg3, req.gameMode, req.api)
                case Failure(err) =>
                  println(s"Error: ${err.getMessage}")
                  val arg3 = ""
                  this.controller = new RealController(req.fen, newContext, arg3, req.gameMode, req.api)
              }
              complete(StatusCodes.OK, JsonResult(s"Initialized duo with FEN: ${req.fen}"))
            }
          }
        },

        // GET /controller/fen
        path("fen") {
          get {
            complete(JsonResult(controller.fen))
          } ~
            post {
              parameter("value") { value =>
                controller.fen = value
                complete(StatusCodes.OK, JsonResult("Ok"))
              }
            }
        },

        // GET /controller/resetBoard
        path("resetBoard") {
          post {
            controller.resetBoard()
            complete(StatusCodes.OK, JsonResult("Ok"))
          }
        },

        // GET /controller/context
        path("context") {
          get {
            complete(JsonResult(controller.context))
          } ~
            post {
              entity(as[JsValue]) { js =>
                val ctx = js.convertTo[ChessContext]
                controller.context = ctx
                complete(StatusCodes.OK, JsonResult("Ok"))
              }
            }
        },

        // GET /controller/currentTheme
        path("currentTheme") {
          get {
            complete(JsonResult(controller.current_theme))
          } ~
            post {
              parameter("value".as[Int]) { v =>
                controller.current_theme = v
                complete(StatusCodes.OK, JsonResult("Ok"))
              }
            }
        },

        // POST /controller/play
        path("play") {
          post {
            entity(as[Move]) { moveJson =>
              controller.play(Try((moveJson.from, moveJson.to)))
              complete(StatusCodes.OK, JsonResult("Ok"))
            }
          }
        },

        // POST /controller/undo
        path("undo") {
          post {
            controller.undo()
            complete(StatusCodes.OK, JsonResult("Ok"))
          }
        },

        // POST /controller/redo
        path("redo") {
          post {
            controller.redo()
            complete(StatusCodes.OK, JsonResult("Ok"))
          }
        },

        // GET /controller/createOutput
        path("createOutput") {
          get {
            onComplete(Future.fromTry(controller.createOutput())) {
              case Success(o) => complete(JsonResult(o))
              case Failure(ex) => complete(StatusCodes.BadRequest, ex.getMessage)
            }
          }
        },

        // POST /controller/promotePawn
        path("promotePawn") {
          post {
            entity(as[JsValue]) { js =>
              val piece = js.asJsObject.fields("pieceKind").convertTo[String]
              controller.promotePawn(piece)
              complete(StatusCodes.OK, JsonResult("Ok"))
            }
          }
        },

        // POST /controller/squareClicked
        path("squareClicked") {
          post {
            entity(as[JsValue]) { js =>
              val pos = js.asJsObject.fields("square").convertTo[Int]
              controller.squareClicked(Try(pos))
              complete(StatusCodes.OK, JsonResult("Ok"))
            }
          }
        },

        // POST /controller/nextTheme
        path("nextTheme") {
          post {
            controller.nextTheme()
            complete(StatusCodes.OK, JsonResult("Ok"))
          }
        },

        // GET /controller/errorMessage
        path("errorMessage") {
          get {
            onComplete(Future.fromTry(controller.getErrorMessage)) {
              case Success(msg) =>
                println("errorMessage SUCCESS: " + msg)
                complete(JsonResult(msg))
              case Failure(ex)  =>
                println("errorMessage FAILURE: " + ex.getMessage)
                complete(StatusCodes.BadRequest, ex.getMessage)
            }
          }
        },

        // GET /controller/translateMoveStringToInt
        path("translateMoveStringToInt") {
          get {
            parameters("fen", "move") { (fen, mv) =>
              onComplete(controller.translateMoveStringToInt(fen, mv)) {
                case Success(res) => res match {
                  case Success((from, to)) =>
                    complete(JsonResult(Move(from, to)))
                  case Failure(ex) =>
                    complete(StatusCodes.BadRequest, ex.getMessage)
                }
                case Failure(ex)  => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },

        path("register") {
          parameter("url") { url =>
            observers += url
            complete(JsonResult(s"Registered observer at $url"))
          }
        } ~
            path("notify") {
              post {
                notifyObservers()
                complete(JsonResult("Observers notified"))
              }
            } ~
            path("ring") {
              post {
                ringObservers()
                complete(JsonResult("Special case triggered on observers"))
              }
            } ~
            path("dering") {
              post {
                deRingObservers()
                complete(JsonResult("Reverse special case triggered"))
              }
            } ~
            path("error") {
              post {
                tellErrorToObservers()
                complete(JsonResult("Error told to observers"))
              }
            }

      )
    }
}

object ControllerServer extends App {
  implicit val system: ActorSystem = ActorSystem("ControllerSystem")

  // Your implementation of ControllerTrait
  val arg2 = new ChessContext
  val controllerImpl: ControllerTrait = new RealController("start_fen", arg2, "start_fen", "http://localhost:8080", "http://localhost:8080")

  val routes = new ControllerRoutes(controllerImpl).routes

  var observers: ListBuffer[String] = ListBuffer() // stores base URLs like "http://localhost:8081"

  def notifyObservers(): Unit = observers.foreach { url =>
    println(s"Notifying observer at $url")
    Http().singleRequest(HttpRequest(POST, uri = s"$url/update"))
  }

  def ringObservers(): Unit = observers.foreach { url =>
    println(s"Ringing observer at $url")
    Http().singleRequest(HttpRequest(POST, uri = s"$url/special"))
  }

  def deRingObservers(): Unit = observers.foreach { url =>
    println(s"DeRinging observer at $url")
    Http().singleRequest(HttpRequest(POST, uri = s"$url/reverse"))
  }

  def tellErrorToObservers(): Unit = observers.foreach { url =>
    println(s"Telling error to observer at $url")
    Http().singleRequest(HttpRequest(POST, uri = s"$url/error"))
  }

  Http().newServerAt("0.0.0.0", 8080).bind(routes)
  println("Controller REST API running at http://0.0.0.0:8080/")
}
