package DevourChess

import SharedResources.PieceType.{BISHOP, KING, KNIGHT, PAWN, QUEEN, ROOK}
import SharedResources.Color.{BLACK, WHITE}
import SharedResources.Requests.PiecesPositionRequest
import SharedResources.{Color, GenericHttpClient, JsonResult, Piece, PieceType}
import SharedResources.PieceJsonProtocol.pieceFormat
import SharedResources.GenericHttpClient.{vectorFormat, listFormat, IntJsonFormat, ec}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Remis {

    def isRemis(fen: String): Future[Try[Boolean]] = {
        val bishopPieceList = List(Piece(BISHOP, BLACK), Piece(BISHOP, WHITE))
        val otherPieceList = List(
            Piece(PAWN, BLACK), Piece(PAWN, WHITE),
            Piece(ROOK, BLACK), Piece(ROOK, WHITE),
            Piece(QUEEN, WHITE), Piece(QUEEN, BLACK),
            Piece(KING, WHITE), Piece(KING, BLACK),
            Piece(KNIGHT, WHITE), Piece(KNIGHT, BLACK)
        )

        // Step 1: Get the board
        GenericHttpClient.get[JsonResult[Vector[Piece]]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/fenToBoard",
            queryParams = Map("fen" -> fen)
        ).flatMap { boardResponse =>
            val board = boardResponse.result

            // Step 2: Check if there are any other pieces (not bishops)
            val payloadOther = PiecesPositionRequest(board, otherPieceList)
            GenericHttpClient.post[PiecesPositionRequest, JsonResult[List[Int]]](
                baseUrl = "http://basic-chess:8080",
                route = "/chess/piecesPositions",
                payload = payloadOther
            ).flatMap {
                case JsonResult(otherPositions) if otherPositions.nonEmpty =>
                    Future.successful(Success(false)) // Not remis if other pieces exist

                case _ => // Step 3: Check bishops' colors
                    val payloadBishops = PiecesPositionRequest(board, bishopPieceList)
                    GenericHttpClient.post[PiecesPositionRequest, JsonResult[List[Int]]](
                        baseUrl = "http://basic-chess:8080",
                        route = "/chess/piecesPositions",
                        payload = payloadBishops
                    ).map { case JsonResult(positions) =>
                        if (positions.size != 2) return Future.successful(Success(false))

                        def squareColor(pos: Int): Color = {
                            val rowEven = (pos / 8) % 2 == 0
                            val colEven = (pos % 2) == 0
                            if (rowEven == colEven) WHITE else BLACK
                        }

                        val c1 = squareColor(positions(0))
                        val c2 = squareColor(positions(1))
                        Success(c1 != c2) // Different colors means remis
                    }
            }
        }.recover { case err => Failure(err) }
    }
}
