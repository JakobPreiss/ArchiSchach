package RealChess

import SharedResources.{ChessTrait, GenericHttpClient, JsonResult, Piece}
import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.ec

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RealChessFacade extends ChessTrait {

    def getAllLegalMoves(fen: String): Future[Try[List[(Int, Int)]]] = {
        GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        ).flatMap { validFen =>
            LegalMoves.getAllLegalMoves(validFen.result)
        }.recover { case err => Failure(err) }
    }

    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Future[Try[Boolean]] = {
        GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        ).flatMap { validFen =>
            Remis.isRemis(validFen.result, legalMoves)
        }.recover { case err => Failure(err) }
    }

    def getBestMove(fen: String, depth: Int): Future[Try[String]] = {
        GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isValidFen",
            queryParams = Map("fen" -> fen)
        ).map { validFen =>
            ChessApiClient.getBestMove(validFen.result, depth)
        }.recover { case err => Failure(err) }
    }
}
