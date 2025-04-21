package DevourChess

import SharedResources.PieceType.{BISHOP, KING, KNIGHT, PAWN, QUEEN, ROOK}
import SharedResources.Color.{BLACK, EMPTY, WHITE}
import SharedResources.Requests.PiecesPositionRequest
import SharedResources.{Color, GenericHttpClient, JsonResult, Piece, PieceType}

import SharedResources.PieceJsonProtocol.pieceFormat
import SharedResources.GenericHttpClient.vectorFormat
import SharedResources.GenericHttpClient.ec
import SharedResources.GenericHttpClient.listFormat
import SharedResources.GenericHttpClient.IntJsonFormat


import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Remis {
    //für den Fall, dass 2 pieces sich nicht gegenseitig schlagen können (also eig. nur bei 2 Läufern auf unterschiedlichen Farben
    def isRemis(fen : String) : Try[Boolean] = {
        val bishopPieceList = List(Piece(PieceType.BISHOP, BLACK), Piece(PieceType.BISHOP, WHITE))
        val OtherPieceList = List(Piece(PAWN, BLACK), Piece(PAWN, WHITE), Piece(ROOK, BLACK), Piece(ROOK, WHITE),
            Piece(QUEEN, WHITE), Piece(QUEEN, BLACK), Piece(KING, WHITE), Piece(KING, BLACK), Piece(KNIGHT, WHITE),
            Piece(KNIGHT, BLACK))

        val fenToBoard: Future[JsonResult[Vector[Piece]]] = GenericHttpClient.get[JsonResult[Vector[Piece]]](
            baseUrl = "http://localhost:5001",
            route = "/fenToBoard",
            queryParams = Map("fen" -> fen)
        )
        fenToBoard.onComplete {
            case Success(board) =>
                val payload = PiecesPositionRequest (
                    board = board.result,
                    piece = OtherPieceList,
                )
                val piecePositions: Future[JsonResult[List[Int]]] = GenericHttpClient.post[PiecesPositionRequest, JsonResult[List[Int]]](
                    baseUrl = "http://localhost:5001",
                    route = "/piecesPositions",
                    payload = payload
                )
                piecePositions.onComplete {
                    case Failure(err) =>
                        return Failure(err)
                    case Success(otherPieces) if(otherPieces.result.nonEmpty) =>
                        return Success(false)
                    case Success(noOtherPieces) =>
                        val payload2 = PiecesPositionRequest (
                            board = board.result,
                            piece = bishopPieceList,
                        )
                        val bishopPositions: Future[JsonResult[List[Int]]] = GenericHttpClient.post[PiecesPositionRequest, JsonResult[List[Int]]](
                            baseUrl = "http://localhost:5001",
                            route = "/piecesPositions",
                            payload = payload2
                        )
                        bishopPositions.onComplete {
                            case Failure(err) => Failure(err)
                            case Success(bishopPositions) =>
                                val Color1 = bishopPositions.result(0) match {
                                    case white1 if (bishopPositions.result(0) / 8) % 2 == 0 && bishopPositions.result(0) % 2 == 0 => Color.WHITE
                                    case black1 if (bishopPositions.result(0) / 8) % 2 == 0 && bishopPositions.result(0) % 2 == 1 => Color.BLACK
                                    case black2 if (bishopPositions.result(0) / 8) % 2 == 1 && bishopPositions.result(0) % 2 == 0 => Color.BLACK
                                    case white2 if (bishopPositions.result(0) / 8) % 2 == 1 && bishopPositions.result(0) % 2 == 1 => Color.WHITE
                                }
                                val Color2 = bishopPositions.result(1) match {
                                    case white1 if (bishopPositions.result(1) / 8) % 2 == 0 && bishopPositions.result(1) % 2 == 0 => Color.WHITE
                                    case black1 if (bishopPositions.result(1) / 8) % 2 == 0 && bishopPositions.result(1) % 2 == 1 => Color.BLACK
                                    case black2 if (bishopPositions.result(1) / 8) % 2 == 1 && bishopPositions.result(1) % 2 == 0 => Color.BLACK
                                    case white2 if (bishopPositions.result(1) / 8) % 2 == 1 && bishopPositions.result(1) % 2 == 1 => Color.WHITE
                                }
                                return Success(Color1 != Color2)
                        }
                }
            case Failure(err) =>
                return Failure(err)
        }

        Failure(new Exception("Failed to get board"))
    }
}
