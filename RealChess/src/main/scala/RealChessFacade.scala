package RealChess

import SharedResources.{ChessTrait, GenericHttpClient, JsonResult, Piece}

import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.ec

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RealChessFacade extends ChessTrait {

    def getAllLegalMoves(fen: String): Try[List[(Int, Int)]] = {
        val translation: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        )
        translation.onComplete {
            case Success(validFen) =>
                return LegalMoves.getAllLegalMoves(validFen.result)
            case Failure(err) =>
                return Failure(err)
        }
        Failure(new Exception("Failed to get legal moves"))
    }

    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Try[Boolean] = {
        val translation: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        )
        translation.onComplete {
            case Success(validFen) =>
                return Remis.isRemis(validFen.result, legalMoves)
            case Failure(err) =>
                return Failure(err)
        }
        Failure(new Exception("Failed to get remis"))
    }

    // Special case because getBestMove returns a Try[String] instead of a String
    def getBestMove(fen: String, depth: Int): Try[String] = {
        val translation: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        )
        translation.onComplete {
            case Success(validFen) =>
                return ChessApiClient.getBestMove(validFen.result, depth)
            case Failure(err) =>
                return Failure(err)
        }
        Failure(new Exception("Failed to get remis"))
    }
}
