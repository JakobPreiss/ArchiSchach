package XML

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import spray.json.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

// Bring in your real types
import SharedResources.{ApiFileTrait, ChessContext, DataWrapper, State, JsonResult}
import spray.json.DefaultJsonProtocol._ // brings JsonFormat[String], Int, your ChessContext, etc.
import SharedResources.JsonResult._ // brings your implicit jsonResultFormat[T]
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import SharedResources.ChessJsonProtocol.chessContextFormat

class ApiFileRoutes(apiFileService: ApiFileTrait)(implicit system: ActorSystem) {
  import system.dispatcher

  val routes: Route =
    pathPrefix("apifile") {
      concat(
        path("from") {
          get {
            onComplete(apiFileService.from) {
              case Success(data) => complete(StatusCodes.OK, JsonResult(data))
              case Failure(ex)   => complete(StatusCodes.BadRequest, ex.getMessage)
            }
          }
        },

        // POST /apifile/printTo
        path("printTo") {
          post {
            entity(as[JsValue]) { json =>
              val fen = json.asJsObject.fields("fen").convertTo[String]
              val contextStateOrdinal = json.asJsObject.fields("ctx").convertTo[Int]
              onComplete(Future.fromTry(Try(apiFileService.printTo(contextStateOrdinal, fen)))) {
                case Success(_)  => complete(StatusCodes.OK, JsonResult("File written successfully"))
                case Failure(ex) => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        }
      )
    }
}

object ApiFileServer extends App {
  implicit val system: ActorSystem = ActorSystem("ApiFileSystem")

  // Provide your real implementation here
  val apiFileService: ApiFileTrait = new XMLApi

  val routes = new ApiFileRoutes(apiFileService).routes

  Http().newServerAt("0.0.0.0", 8080).bind(routes)
  println("API-File service running at http://0.0.0.0:8080/")
}
