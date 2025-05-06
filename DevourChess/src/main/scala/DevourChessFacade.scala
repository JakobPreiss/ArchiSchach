package DevourChess

import SharedResources.{ChessTrait, GenericHttpClient, JsonResult}
import SharedResources.GenericHttpClient.{ec, StringJsonFormat}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class DevourChessFacade extends ChessTrait {

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
            Remis.isRemis(validFen.result)
        }.recover { case err => Failure(err) }
    }

    def getBestMove(fen: String, depth: Int): Future[Try[String]] = {
        Future.successful(Failure(new ClassNotFoundException("No Stockfish for DevourChess")))
    }
}
