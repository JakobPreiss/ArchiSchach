package BasicChess

import BasicChess.StandartChess.BasicChessFacade
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import SharedResources.{Color, ColorJsonFormat, JsonResult, Piece, PieceType, PieceTypeJsonFormat}

// Spray JSON imports – you’ll need to define your own jsonFormats for your domain classes
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

/**
 * Define JSON support for your custom types.
 * Note: You need to adjust the fields and formats according to your actual Piece, Color, and PieceType definitions.
 */
object JsonProtocols extends DefaultJsonProtocol {
  implicit val pieceFormat: RootJsonFormat[Piece] = jsonFormat2(Piece) // adjust number of fields and names

  // Helpers for moves and other composite objects
  case class Move(from: Int, to: Int)
  implicit val moveFormat: RootJsonFormat[Move] = jsonFormat2(Move)
}

import JsonProtocols._

/**
 * BasicChessRoutes maps all BasicChessTrait methods to REST endpoints.
 *
 * Each endpoint wraps the call to the chessService method, converts the Try result into a Future via Future.fromTry,
 * and then completes the request.
 *
 * The route endpoints include:
 *
 * - GET /chess/boardString?fen=...
 * - GET /chess/fenToBoard?fen=...
 * - GET /chess/allPseudoLegalMoves?fen=...
 * - GET /chess/canPromote?fen=...
 * - POST /chess/promote (with JSON: { "pieceName": "...", "fen": "...", "position": ... })
 * - GET /chess/isColorPiece?fen=...&position=...
 * - POST /chess/translateCastle (with JSON: { "board": [...], "move": { "from": ..., "to": ... } })
 * - GET /chess/translateCastleFromFen?fen=...&from=...&to=...
 * - POST /chess/piecesPositions (with JSON: { "board": [...], "pieces": [...] })
 * - POST /chess/piecePositions (with JSON: { "board": [...], "piece": { ... } })
 * - GET /chess/onBoard?beginningPosition=...&rowDirection=...&columnDirection=...
 * - POST /chess/boardToFen (with JSON: { "board": [...] })
 * - GET /chess/isDifferentColorPiece?fen=...&position=...
 * - GET /chess/getDefaultFen
 * - GET /chess/translateMoveStringToInt?fen=...&move=...
 * - GET /chess/getDefaultBoard
 * - POST /chess/pieceMoves (with JSON: { "pieceTypes": [...] })
 * - GET /chess/isValidFen?fen=...
 * - POST /chess/updateCastleing (with JSON: { "fenCastles": "...", "move": { "from": ..., "to": ... } })
 * - POST /chess/updateEnpassant (with JSON: { "fen": "...", "move": { "from": ..., "to": ... } })
 * - GET /chess/calculateMoveValues?color=...
 * - POST /chess/isCorrectBoardVector (with JSON: { "board": [...] })
 * - GET /chess/isValidMove?move=...
 * - POST /chess/makeMove (with JSON: { "fen": "...", "move": { "from": ..., "to": ... } })
 */
class BasicChessRoutes(chessService: BasicChessTrait)(implicit system: ActorSystem) {
  import system.dispatcher

  val routes: Route =
    pathPrefix("chess") {
      concat(
        // GET https://localhost:8081/chess/boardString?fen=...
        path("boardString") {
          get {
            parameter("fen") { fen =>
              onComplete(Future.fromTry(chessService.getBoardString(fen))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/fenToBoard?fen=...
        path("fenToBoard") {
          get {
            parameter("fen") { fen =>
              onComplete(Future.fromTry(chessService.fenToBoard(fen))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/allPseudoLegalMoves?fen=...
        path("allPseudoLegalMoves") {
          get {
            parameter("fen") { fen =>
              onComplete(Future.fromTry(chessService.getAllPseudoLegalMoves(fen))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/canPromote?fen=...
        path("canPromote") {
          get {
            parameter("fen") { fen =>
              onComplete(Future.fromTry(chessService.canPromote(fen))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/promote with JSON payload
        // JSON body expected: { "pieceName": "...", "fen": "...", "position": ... }
        path("promote") {
          post {
            entity(as[JsValue]) { json =>
              val pieceName = json.asJsObject.fields("pieceName").convertTo[String]
              val fen       = json.asJsObject.fields("fen").convertTo[String]
              val position  = json.asJsObject.fields("position").convertTo[Int]
              onComplete(Future.fromTry(chessService.promote(pieceName, fen, position))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/isColorPiece?fen=...&position=...
        path("isColorPiece") {
          get {
            parameters("fen", "position".as[Int]) { (fen, pos) =>
              onComplete(Future.fromTry(chessService.isColorPiece(fen, pos))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/translateCastle with JSON payload
        // JSON body expected: { "board": [...], "move": { "from": ..., "to": ... } }
        path("translateCastle") {
          post {
            entity(as[JsValue]) { json =>
              val board    = json.asJsObject.fields("board").convertTo[Vector[Piece]]
              val moveJson = json.asJsObject.fields("move").asJsObject
              val from     = moveJson.fields("from").convertTo[Int]
              val to       = moveJson.fields("to").convertTo[Int]
              onComplete(Future.fromTry(chessService.translateCastle(board, (from, to)))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/translateCastleFromFen?fen=...&from=...&to=...
        path("translateCastleFromFen") {
          get {
            parameters("fen", "from".as[Int], "to".as[Int]) { (fen, from, to) =>
              onComplete(Future.fromTry(chessService.translateCastleFromFen(fen, (from, to)))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/piecesPositions with JSON payload
        // JSON body expected: { "board": [...], "pieces": [...] }
        path("piecesPositions") {
          post {
            entity(as[JsValue]) { json =>
              val board  = json.asJsObject.fields("board").convertTo[Vector[Piece]]
              val pieces = json.asJsObject.fields("pieces").convertTo[List[Piece]]
              onComplete(Future.fromTry(chessService.piecesPositions(board, pieces))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/piecePositions with JSON payload
        // JSON body expected: { "board": [...], "piece": { ... } }
        path("piecePositions") {
          post {
            entity(as[JsValue]) { json =>
              val board = json.asJsObject.fields("board").convertTo[Vector[Piece]]
              val piece = json.asJsObject.fields("piece").convertTo[Piece]
              onComplete(Future.fromTry(chessService.piecePositions(board, piece))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/onBoard?beginningPosition=...&rowDirection=...&columnDirection=...
        path("onBoard") {
          get {
            parameters("beginningPosition".as[Int], "rowDirection".as[Int], "columnDirection".as[Int]) { (bp, rd, cd) =>
              onComplete(Future.fromTry(chessService.onBoard(bp, rd, cd))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/boardToFen with JSON payload
        // JSON body expected: { "board": [...] }
        path("boardToFen") {
          post {
            entity(as[JsValue]) { json =>
              val board = json.asJsObject.fields("board").convertTo[Vector[Piece]]
              onComplete(Future.fromTry(chessService.boardToFen(board))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/isDifferentColorPiece?fen=...&position=...
        path("isDifferentColorPiece") {
          get {
            parameters("fen", "position".as[Int]) { (fen, pos) =>
              onComplete(Future.fromTry(chessService.isDifferentColorPiece(fen, pos))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/getDefaultFen
        path("getDefaultFen") {
          get {
            complete(chessService.getDefaultFen())
          }
        },
        // GET /chess/translateMoveStringToInt?fen=...&move=...
        path("translateMoveStringToInt") {
          get {
            parameters("fen", "move") { (fen, moveStr) =>
              onComplete(Future.fromTry(chessService.translateMoveStringToInt(fen, moveStr))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/getDefaultBoard
        path("getDefaultBoard") {
          get {
            complete(JsonResult(chessService.getDefaultBoard()))
          }
        },
        // POST /chess/pieceMoves with JSON payload
        // JSON body expected: { "pieceTypes": [...] }
        path("pieceMoves") {
          post {
            entity(as[JsValue]) { json =>
              val pieceTypes = json.asJsObject.fields("pieceTypes").convertTo[List[PieceType]]
              onComplete(Future.fromTry(chessService.pieceMoves(pieceTypes))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/isValidFen?fen=...
        path("isValidFen") {
          get {
            parameter("fen") { fen =>
              onComplete(Future.fromTry(chessService.isValidFen(fen))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/updateCastleing with JSON payload
        // JSON body expected: { "fenCastles": "...", "move": { "from": ..., "to": ... } }
        path("updateCastleing") {
          post {
            entity(as[JsValue]) { json =>
              val fenCastles = json.asJsObject.fields("fenCastles").convertTo[String]
              val moveJson   = json.asJsObject.fields("move").asJsObject
              val from       = moveJson.fields("from").convertTo[Int]
              val to         = moveJson.fields("to").convertTo[Int]
              onComplete(Future.fromTry(chessService.updateCastleing(fenCastles, (from, to)))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/updateEnpassant with JSON payload
        // JSON body expected: { "fen": "...", "move": { "from": ..., "to": ... } }
        path("updateEnpassant") {
          post {
            entity(as[JsValue]) { json =>
              val fen      = json.asJsObject.fields("fen").convertTo[String]
              val moveJson = json.asJsObject.fields("move").asJsObject
              val from     = moveJson.fields("from").convertTo[Int]
              val to       = moveJson.fields("to").convertTo[Int]
              onComplete(Future.fromTry(chessService.updateEnpassant(fen, (from, to)))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/calculateMoveValues?color=...
        path("calculateMoveValues") {
          get {
            parameter("color") { colorStr =>
              // Assuming Color.withName parses the provided string into a Color
              onComplete(Future.fromTry(chessService.calculateMoveValues(Color.fromString(colorStr)))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/isCorrectBoardVector with JSON payload
        // JSON body expected: { "board": [...] }
        path("isCorrectBoardVector") {
          post {
            entity(as[JsValue]) { json =>
              val board = json.asJsObject.fields("board").convertTo[Vector[Piece]]
              onComplete(Future.fromTry(chessService.isCorrectBoardVector(board))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // GET /chess/isValidMove?move=...
        path("isValidMove") {
          get {
            parameter("move") { moveStr =>
              onComplete(Future.fromTry(chessService.isValidMove(moveStr))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        },
        // POST /chess/makeMove with JSON payload
        // JSON body expected: { "fen": "...", "move": { "from": ..., "to": ... } }
        path("makeMove") {
          post {
            entity(as[JsValue]) { json =>
              val fen      = json.asJsObject.fields("fen").convertTo[String]
              val moveJson = json.asJsObject.fields("move").asJsObject
              val from     = moveJson.fields("from").convertTo[Int]
              val to       = moveJson.fields("to").convertTo[Int]
              onComplete(Future.fromTry(chessService.makeMove(fen, (from, to)))) {
                case Success(result) => complete(JsonResult(result))
                case Failure(ex)     => complete(StatusCodes.BadRequest, ex.getMessage)
              }
            }
          }
        }
      )
    }
}

object BasicChessServer extends App {
  implicit val system: ActorSystem = ActorSystem("BasicChessSystem")

  // Instantiate your chess service (which implements BasicChessTrait)
  // You will need to provide an actual implementation.
  private val chessService: BasicChessTrait = BasicChessFacade

  // Create the routes instance
  private val routes = new BasicChessRoutes(chessService).routes

  // Bind the routes to an HTTP server
  val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(routes)

  println("Basic Chess Rest API at http://0.0.0.0:8080/")
}
