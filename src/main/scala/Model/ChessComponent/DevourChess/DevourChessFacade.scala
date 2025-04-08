package Model.ChessComponent.DevourChess

import Model.ChessComponent.BasicChessComponent.StandartChess.{BasicChessFacade, ChessBoard, Piece}
import Model.ChessComponent.ChessTrait
import Model.ChessComponent.RealChess.Remis

import scala.util.{Failure, Success, Try}

class DevourChessFacade extends ChessTrait {
    private def withValidFen[T](fen: String)(f: String => T): Try[T] = {
        ChessBoard.isValidFen(fen).map(validFen => f(validFen))
    }

    def getBoardString(fen :String): Try[String] = {
        withValidFen(fen) { validFen =>
            BasicChessFacade.getBoardString(validFen)
        }
    }

    def getAllLegalMoves(fen: String): Try[List[(Int, Int)]] = {
        withValidFen(fen) { validFen =>
            LegalMoves.getAllLegalMoves(validFen)
        }
    }

    def makeMove(fen: String, move: (Int, Int)): Try[String] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
            validMove <- ChessBoard.isValidMove(move)
        } yield {
            BasicChessFacade.makeMove(validFen, validMove)
        }
    }

    def canPromote(fen: String): Try[Option[Int]] = {
        val validFen = ChessBoard.isValidFen(fen) match {
            case Success(validFen) => validFen
            case Failure(exception) =>
                return Failure(exception)
        }

        Success(BasicChessFacade.canPromote(validFen))
    }

    def promote(pieceName: String, fen: String, position: Int): Try[String] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
            validPosition <- ChessBoard.isValidPosition(fen, position)
            validPieceName <- ChessBoard.isValidPieceName(pieceName)
        } yield {
            BasicChessFacade.promote(validPieceName, validFen, validPosition)
        }
    }

    def isColorPiece(fen: String, position: Int): Try[Boolean] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
            validPosition <- ChessBoard.isValidPosition(fen, position)
        } yield {
            BasicChessFacade.isColorPiece(validFen, validPosition)
        }
    }

    def translateCastle(fen: String, move: (Int, Int)): Try[(Int, Int)] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
            validMove <- ChessBoard.isValidMove(move)
        } yield {
            BasicChessFacade.translateCastle(BasicChessFacade.fenToBoard(validFen), validMove)
        }
    }

    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Try[Boolean] = {
        withValidFen(fen) { validFen =>
            Remis.isRemis(validFen, legalMoves)
        }
    }

    def getBestMove(fen: String, depth: Int): Try[String] = {Success("")}

    def translateMoveStringToInt(fen: String, move: String): Try[(Int, Int)] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
            validMove <- ChessBoard.isValidMove(move)
        } yield {
            BasicChessFacade.translateMoveStringToInt(validFen, validMove)
        }
    }

    def getDefaultFen(): String = {
        BasicChessFacade.getDefaultFen()
    }

    def getDefaultBoard(): Vector[Piece] = {
        BasicChessFacade.getDefaultBoard()
    }
}
