package DevourChess

import SharedResources.{ChessTrait, GenericHttpClient, JsonResult}

import SharedResources.GenericHttpClient.ec
import SharedResources.GenericHttpClient.StringJsonFormat

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class DevourChessFacade extends ChessTrait {

    def getAllLegalMoves(fen: String): Try[List[(Int, Int)]] = {
        val isDifferentColorPiece: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        )
        isDifferentColorPiece.onComplete {
            case Success(validFen) =>
                return LegalMoves.getAllLegalMoves(validFen.result)
            case Failure(err) =>
                return Failure(err)
        }

        Failure(new Exception("Failed to get isDifferentColorPiece"))
    }


    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Try[Boolean] = {
        val isDifferentColorPiece: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        )
        isDifferentColorPiece.onComplete {
            case Success(validFen) =>
                return Remis.isRemis(validFen.result)
            case Failure(err) =>
                return Failure(err)
        }

        Failure(new Exception("Failed to get isDifferentColorPiece"))
    }

    def getBestMove(fen: String, depth: Int): Try[String] = {
        Failure(new ClassNotFoundException("No Stockfish for DevourChess"))
    }

}
