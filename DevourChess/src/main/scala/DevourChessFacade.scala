package DevourChess

import BasicChess.StandartChess.{BasicChessFacade}
import SharedResources.Piece.*
import SharedResources.ChessTrait

import scala.util.{Failure, Success, Try}

class DevourChessFacade extends ChessTrait {

    def getAllLegalMoves(fen: String): Try[List[(Int, Int)]] = {
        BasicChessFacade.isValidFen(fen) match {
            case Failure(err) => Failure(err)
            case Success(validFen) =>
                LegalMoves.getAllLegalMoves(validFen)
        }
    }


    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Try[Boolean] = {
        BasicChessFacade.isValidFen(fen) match {
            case Failure(err) => Failure(err)
            case Success(validFen) =>
                Remis.isRemis(validFen)
        }
    }

    def getBestMove(fen: String, depth: Int): Try[String] = {
        Failure(new ClassNotFoundException("No Stockfish for DevourChess"))
    }

}
