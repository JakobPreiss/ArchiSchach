package DatabaseService

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import spray.json.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Await
import scala.concurrent.duration._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

// Bring in your real types
import SharedResources.{ApiFileTrait, ChessContext, DataWrapper, State, JsonResult}
import spray.json.DefaultJsonProtocol._ // brings JsonFormat[String], Int, your ChessContext, etc.
import SharedResources.JsonResult._ // brings your implicit jsonResultFormat[T]
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import SharedResources.ChessJsonProtocol.chessContextFormat

class ApiFileRoutes(databaseDAO: DatabaseDAO)(implicit system: ActorSystem) {
  import system.dispatcher

  val routes: Route =
    pathPrefix("apifile") {
      concat(
        path("from") {
          get {
            onComplete(databaseDAO.loadGameState()) {
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
              onComplete(Future.fromTry(Try(databaseDAO.saveGameState(fen, contextStateOrdinal)))) {
                case Success(_)  => complete(StatusCodes.OK, JsonResult("File written successfully"))
                case Failure(ex) => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        }
      )
    }
}

object DatabaseServer extends App {
  implicit val system: ActorSystem = ActorSystem("ApiFileSystem")

  val db = Database.forConfig("postgres")
  val databaseDAO: DatabaseDAO = new Slick(db)
  databaseDAO.init()

  val routes = new ApiFileRoutes(databaseDAO).routes

  Http().newServerAt("0.0.0.0", 8080).bind(routes)
  println("API-File service running at http://0.0.0.0:8080/")
}
