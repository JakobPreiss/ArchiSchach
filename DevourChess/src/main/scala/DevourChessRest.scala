package DevourChess

import DevourChess.DevourChessFacade
import SharedResources.{ChessTrait, JsonResult}

import scala.util.{Failure, Success, Try}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import spray.json.*

import scala.concurrent.Future

object JsonProtocols extends DefaultJsonProtocol {
  // Represent moves as JSON objects
  case class Move(from: Int, to: Int)
  implicit val moveFormat: RootJsonFormat[Move] = jsonFormat2(Move)
}

import JsonProtocols._

class ChessRoutes(chessService: ChessTrait)(implicit system: ActorSystem) {
  import system.dispatcher

  val routes: Route =
    pathPrefix("chess") {
      concat(
        // GET /chess/getAllLegalMoves?fen=...
        path("getAllLegalMoves") {
          get {
            parameter("fen") { fen =>
              onComplete(Future.fromTry(chessService.getAllLegalMoves(fen))) {
                case Success(moves) =>
                  val jsMoves = moves.map { case (f, t) => Move(f, t) }
                  complete(JsonResult(jsMoves))
                case Failure(ex) => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },

        // POST /chess/isRemis
        path("isRemis") {
          post {
            entity(as[JsValue]) { json =>
              val obj   = json.asJsObject
              val fen   = obj.fields("fen").convertTo[String]
              val moves = obj.fields("legalMoves").convertTo[List[Move]].map(m => (m.from, m.to))
              onComplete(Future.fromTry(chessService.isRemis(fen, moves))) {
                case Success(res)  => complete(JsonResult(res))
                case Failure(ex)   => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },

        // GET /chess/getBestMove?fen=...&depth=...
        path("getBestMove") {
          get {
            parameters("fen", "depth".as[Int]) { (fen, depth) =>
              onComplete(Future.fromTry(chessService.getBestMove(fen, depth))) {
                case Success(best) => complete(JsonResult(best))
                case Failure(ex)   => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        }
      )
    }
}

object ChessServer extends App {
  implicit val system: ActorSystem = ActorSystem("ChessSystem")

  // Provide your implementation of ChessTrait
  val chessService: ChessTrait = DevourChessFacade()

  val routes = new ChessRoutes(chessService).routes
  Http().newServerAt("0.0.0.0", 8080).bind(routes)
  println("Chess REST API running at http://0.0.0.0:8080/")
}
