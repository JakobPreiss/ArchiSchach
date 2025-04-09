package Model.ChessComponent.BasicChessComponent.StandartChess

import Model.ChessComponent.BasicChessComponent.BasicChessTrait

import scala.util.Try

object BasicChessFacade extends BasicChessTrait {
    def getBoardString(fen : String): String = {
        ChessBoard.getBoardString(ChessBoard.fenToBoard(fen))
    }

    def fenToBoard(fen: String): Vector[Piece] = {
        ChessBoard.fenToBoard(fen)
    }

    def getAllPseudoLegalMoves(fen: String): List[(Int, Int)] = {
        PseudoMovesFacade.subSystemOperation(fen)
    }

    def canPromote(fen: String): Option[Int] = {
        ChessBoard.canPromote(fen)
    }

    def promote(pieceName: String, fen: String, position: Int): String = {
        ChessBoard.promote(pieceName, fen, position)
    }

    def isColorPiece(fen: String, position: Int): Boolean = {
        ChessBoard.isColorPiece(fen, position)
    }

    def translateCastle(board: Vector[Piece], move: (Int, Int)): (Int, Int) = {
        ChessBoard.translateCastle(board, move)
    }

    def piecePositions(board: Vector[Piece], piece: Piece): List[Int] = {
        PseudoMoves.piecePositions(board, piece)
    }
    
    def piecesPositions(board: Vector[Piece], pieces: List[Piece]): List[Int] = {
        PseudoMoves.piecesPositions(board, pieces)
    }

    def extractColor(color: String): (Int, Color, Color) = {
        PseudoMoves.extractColor(color)
    }

    def onBoard(beginningPosition: Int, rowDirection: Int, columDirection: Int): Boolean = {
        PseudoMoves.onBoard(beginningPosition, rowDirection, columDirection)
    }

    def boardToFen(board: Vector[Piece]): String = {
        ChessBoard.boardToFen(board)
    }

    def isDifferentColorPiece(fen: String, position: Int): Boolean = {
        ChessBoard.isDifferentColorPiece(fen, position)
    }

    def translateMoveStringToInt(fen: String, move: String): (Int, Int) = {
        ChessBoard.translateMoveStringToInt(fen, move)
    }

    def getDefaultFen(): String = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }

    def getDefaultBoard(): Vector[Piece] = {
        ChessBoard.getDefaultBoard()
    }

    def pieceMoves(pieceTypes: List[PieceType]): List[(Int, Int)] = {
        PseudoMoves.pieceMoves(pieceTypes)
    }

    def isValidFen(fen : String) : Try[String] = {
        ChessBoard.isValidFen(fen)
    }

    def updateCastleing(fenCastles: String, move:(Int, Int)): String = {
        ChessBoard.updateCastleing(fenCastles, move)
    }

    def updateEnpassant(fen: String, move:(Int, Int)): String = {
        ChessBoard.updateEnpassant(fen, move)
    }

    def calculateMoveValues(color: Color) : (Piece, Piece, Piece) = {
        ChessBoard.calculateMoveValues(color)
    }
}
