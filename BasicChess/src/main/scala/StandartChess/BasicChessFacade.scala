package BasicChess.StandartChess

import SharedResources.{Piece, Color, PieceType}
import BasicChess.BasicChessTrait
import scala.util.Try

object BasicChessFacade extends BasicChessTrait {
    def getBoardString(fen : String): Try[String] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            ChessBoard.getBoardString(ChessBoard.fenToBoard(validFen))
        }
    }

    def fenToBoard(fen: String): Try[Vector[Piece]] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            ChessBoard.fenToBoard(validFen)
        }
    }

    def getAllPseudoLegalMoves(fen: String): Try[List[(Int, Int)]] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            PseudoMovesFacade.subSystemOperation(validFen)
        }
    }

    def canPromote(fen: String): Try[Option[Int]] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            ChessBoard.canPromote(validFen)
        }
    }

    def promote(pieceName: String, fen: String, position: Int): Try[String] = {
        for {
            validPos <- ChessBoard.isValidPosition(position)
            validPieceName <- ChessBoard.isValidPieceName(pieceName)
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            val color = PseudoMoves.extractColor(fen.split(" ")(1))._3
            ChessBoard.promote(validPieceName, validFen, validPos, color)
        }
    }

    def isColorPiece(fen: String, position: Int): Try[Boolean] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            ChessBoard.isColorPiece(validFen, position)
        }
    }

    def translateCastle(board: Vector[Piece], move: (Int, Int)): Try[(Int, Int)] = {
        for {
            validBoard <- ChessBoard.isValidBoardVector(board)
        } yield {
            ChessBoard.translateCastle(validBoard, move)
        }
    }

    def translateCastleFromFen(fen : String, move: (Int, Int)): Try[(Int, Int)] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            val validBoard = ChessBoard.fenToBoard(validFen)
            ChessBoard.translateCastle(validBoard, move)
        }
    }


    def piecePositions(board: Vector[Piece], piece: Piece): Try[List[Int]] = {
        for {
            validBoard <- ChessBoard.isValidBoardVector(board)
        } yield {
            val test = PseudoMoves.piecePositions(validBoard, piece)
            test
        }
    }

    def piecesPositions(board: Vector[Piece], pieces: List[Piece]): Try[List[Int]] = {
        for {
            validBoard <- ChessBoard.isValidBoardVector(board)
        } yield {
            val test = PseudoMoves.piecesPositions(validBoard, pieces)
            test
        }
    }

    def onBoard(beginningPosition: Int, rowDirection: Int, columDirection: Int): Try[Boolean] = {
        for {
            validPos <- ChessBoard.isValidPosition(beginningPosition)
        } yield {
            PseudoMoves.onBoard(beginningPosition, rowDirection, columDirection)
        }
    }

    def boardToFen(board: Vector[Piece]): Try[String] = {
        for {
            validBoard <- ChessBoard.isValidBoardVector(board)
        } yield {
            ChessBoard.boardToFen(validBoard)
        }
    }

    def isDifferentColorPiece(fen: String, position: Int): Try[Boolean] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
            validPos <- ChessBoard.isValidPosition(position)
        } yield {
            ChessBoard.isDifferentColorPiece(validFen, validPos)
        }
    }

    def translateMoveStringToInt(fen: String, move: String): Try[(Int, Int)] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
            validMove <- ChessBoard.isValidMove(move)
        } yield {
            ChessBoard.translateMoveStringToInt(validFen, validMove)
        }
    }

    def getDefaultFen(): String = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    }

    def getDefaultBoard(): Vector[Piece] = {
        ChessBoard.getDefaultBoard()
    }

    def pieceMoves(pieceTypes: List[PieceType]): Try[List[(Int, Int)]] = {
        PseudoMoves.pieceMoves(pieceTypes)
    }

    def isValidFen(fen : String) : Try[String] = {
        ChessBoard.isValidFen(fen)
    }

    def updateCastleing(fenCastles: String, move:(Int, Int)): Try[String] = {
        for {
            validCastleString <- ChessBoard.isValidCastleString(fenCastles)
        } yield {
            ChessBoard.updateCastleing(validCastleString, move)
        }
    }

    def updateEnpassant(fen: String, move:(Int, Int)): Try[String] = {
        for {
            validFen <- ChessBoard.isValidFen(fen)
        } yield {
            ChessBoard.updateEnpassant(validFen, move)
        }
    }

    def calculateMoveValues(color: Color) : Try[(Piece, Piece, Piece)] = {
        for {
            validColor <- ChessBoard.checkValidPieceColor(color)
        } yield {
            ChessBoard.calculateMoveValues(validColor)
        }
    }

    def makeMove(fen: String, move: (Int, Int)): Try[String] = {
        for {
            validFen <- BasicChessFacade.isValidFen(fen)
        } yield {
            ChessBoard.makeMove(validFen, move)
        }
    }

    def isCorrectBoardVector(board: Vector[Piece]) : Try[Vector[Piece]] =
        ChessBoard.isValidBoardVector(board)

    def isValidMove(move: String): Try[String] = {
        ChessBoard.isValidMove(move)
    }
}
