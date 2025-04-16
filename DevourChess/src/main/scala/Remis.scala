package DevourChess

import BasicChess.StandartChess.BasicChessFacade
import SharedResources.PieceType.{BISHOP, KING, KNIGHT, PAWN, QUEEN, ROOK}
import SharedResources.Color.{BLACK, EMPTY, WHITE}
import SharedResources.{Color, Piece, PieceType}

import scala.util.{Failure, Success, Try}

object Remis {
    //für den Fall, dass 2 pieces sich nicht gegenseitig schlagen können (also eig. nur bei 2 Läufern auf unterschiedlichen Farben
    def isRemis(fen : String) : Try[Boolean] = {
        val bishopPieceList = List(Piece(PieceType.BISHOP, BLACK), Piece(PieceType.BISHOP, WHITE))
        val OtherPieceList = List(Piece(PAWN, BLACK), Piece(PAWN, WHITE), Piece(ROOK, BLACK), Piece(ROOK, WHITE),
            Piece(QUEEN, WHITE), Piece(QUEEN, BLACK), Piece(KING, WHITE), Piece(KING, BLACK), Piece(KNIGHT, WHITE),
            Piece(KNIGHT, BLACK))
        BasicChessFacade.fenToBoard(fen) match {
            case Failure(err) => Failure(err)
            case Success(board) =>
                BasicChessFacade.piecesPositions(board, OtherPieceList) match {
                    case Failure(err) => Failure(err)
                    case Success(otherPieces) if(otherPieces.nonEmpty) =>
                        Success(false)
                    case Success(noOtherPieces) =>
                        BasicChessFacade.piecesPositions(board, bishopPieceList) match {
                            case Failure(err) => Failure(err)
                            case Success(bishopPositions) =>
                                val Color1 = bishopPositions(0) match {
                                    case white1 if(bishopPositions(0) / 8) % 2 == 0 && bishopPositions(0) % 2 == 0 => Color.WHITE
                                    case black1 if(bishopPositions(0) / 8) % 2 == 0 && bishopPositions(0) % 2 == 1 => Color.BLACK
                                    case black2 if(bishopPositions(0) / 8) % 2 == 1 && bishopPositions(0) % 2 == 0 => Color.BLACK
                                    case white2 if(bishopPositions(0) / 8) % 2 == 1 && bishopPositions(0) % 2 == 1 => Color.WHITE
                                }
                                val Color2 = bishopPositions(1) match {
                                    case white1 if(bishopPositions(1) / 8) % 2 == 0 && bishopPositions(1) % 2 == 0 => Color.WHITE
                                    case black1 if(bishopPositions(1) / 8) % 2 == 0 && bishopPositions(1) % 2 == 1 => Color.BLACK
                                    case black2 if(bishopPositions(1) / 8) % 2 == 1 && bishopPositions(1) % 2 == 0 => Color.BLACK
                                    case white2 if(bishopPositions(1) / 8) % 2 == 1 && bishopPositions(1) % 2 == 1 => Color.WHITE
                                }
                                Success(Color1 != Color2)
                        }
                }
        }
    }
}
