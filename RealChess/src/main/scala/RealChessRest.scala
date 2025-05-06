package RealChess

import RealChess.RealChessFacade
import SharedResources.{ChessTrait, JsonResult}

import scala.util.{Failure, Success, Try}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import spray.json.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*

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
              onComplete(chessService.getAllLegalMoves(fen)) {
                case Success(moves) =>
                  moves match {
                    case Success(legalMoves) =>
                      println("Legal moves: " + legalMoves)
                      complete(JsonResult(legalMoves))
                    case Failure(ex)         => complete(StatusCodes.InternalServerError, "Unwrap error " + ex.getMessage)
                  }
                case Failure(ex) => complete(StatusCodes.BadRequest, "Upper error " + ex.getMessage)
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
              val moves = obj.fields("legalMoves").convertTo[List[(Int, Int)]]
              onComplete(chessService.isRemis(fen, moves)) {
                case Success(res)  =>
                  res match {
                    case Success(isRemis) => complete(JsonResult(isRemis))
                    case Failure(ex)      => complete(StatusCodes.InternalServerError, ex.getMessage)
                  }
                case Failure(ex)   => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },

        // GET /chess/getBestMove?fen=...&depth=...
        path("getBestMove") {
          get {
            parameters("fen", "depth".as[Int]) { (fen, depth) =>
              onComplete(chessService.getBestMove(fen, depth)) {
                case Success(best) =>
                  best match {
                    case Success(move) => complete(JsonResult(move))
                    case Failure(ex)   => complete(StatusCodes.InternalServerError, ex.getMessage)
                  }
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
  val chessService: ChessTrait = new RealChessFacade()

  val routes = new ChessRoutes(chessService).routes
  Http().newServerAt("0.0.0.0", 8080).bind(routes)
  println("Chess REST API running at http://0.0.0.0:8080/")
}
