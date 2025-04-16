package RealChess

import BasicChess.StandartChess.{BasicChessFacade}
import SharedResources.Piece
import SharedResources.ChessTrait

import scala.util.{Try, Success, Failure}

class RealChessFacade extends ChessTrait {

    def getAllLegalMoves(fen: String): Try[List[(Int, Int)]] = {
        BasicChessFacade.isValidFen(fen) match {
            case Failure(exception) => Failure(exception)
            case Success(validFen) =>
                LegalMoves.getAllLegalMoves(validFen)
        }
    }

    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Try[Boolean] = {
        BasicChessFacade.isValidFen(fen) match {
            case Failure(exception) => Failure(exception)
            case Success(validFen) =>
                Remis.isRemis(validFen, legalMoves)
        }
    }

    // Special case because getBestMove returns a Try[String] instead of a String
    def getBestMove(fen: String, depth: Int): Try[String] = {
        val validFen = BasicChessFacade.isValidFen(fen) match {
            case Success(validFen) => validFen
            case Failure(exception) =>
                return Failure(exception)
        }
        ChessApiClient.getBestMove(validFen, depth)
    }
}
