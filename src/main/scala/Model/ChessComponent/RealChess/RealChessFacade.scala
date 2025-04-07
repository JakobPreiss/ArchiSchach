package Model.ChessComponent.RealChess

import Model.ChessComponent.BasicChessComponent.StandartChess.{BasicChessFacade, Piece}
import Model.ChessComponent.ChessTrait

class RealChessFacade extends ChessTrait {

    def getAllLegalMoves(fen: String): List[(Int, Int)] = {
        //checkfen
        //if correct -> weitergeben
        //if not correct -> Failure an Controller geben
        LegalMoves.getAllLegalMoves(fen)
    }

    def makeMove(fen: String, move: (Int, Int)): String = {
        BasicChessFacade.makeMove(fen, move)
    }

    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Boolean = {
        Remis.isRemis(fen, legalMoves)
    }

    def canPromote(fen: String): Option[Int] = {
        BasicChessFacade.canPromote(fen)
    }

    def promote(pieceName: String, fen: String, position: Int): String = {
        BasicChessFacade.promote(pieceName, fen, position)
    }

    def isColorPiece(fen: String, position: Int): Boolean = {
        BasicChessFacade.isColorPiece(fen, position)
    }

    def translateCastle(fen : String, move: (Int, Int)): (Int, Int) = {
        BasicChessFacade.translateCastle(BasicChessFacade.fenToBoard(fen), move)
    }

    def getBestMove(fen: String, depth : Int): String = {
        ChessApiClient.getBestMove(fen, depth)
    }

    def translateMoveStringToInt(fen: String, move: String): (Int, Int) = {
        BasicChessFacade.translateMoveStringToInt(fen, move)
    }

    def getDefaultFen(): String = {
        BasicChessFacade.getDefaultFen()
    }

    def getDefaultBoard(): Vector[Piece] = {
        BasicChessFacade.getDefaultBoard()
    }

    def getBoardString(fen : String): String = {
        BasicChessFacade.getBoardString(fen)
    }
}
