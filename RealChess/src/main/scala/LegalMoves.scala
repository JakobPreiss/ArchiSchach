package RealChess

import Requests.{Move, MoveRequest}
import SharedResources.PieceType.{BISHOP, KING, KNIGHT, QUEEN, ROOK}
import SharedResources.{Color, GenericHttpClient, JsonResult, Piece, PieceType}
import SharedResources.GenericHttpClient.ec
import SharedResources.GenericHttpClient.BooleanJsonFormat
import SharedResources.GenericHttpClient.vectorFormat
import SharedResources.PieceJsonProtocol.pieceFormat
import SharedResources.Requests.{PieceMovesRequest, PiecePositionRequest}
import SharedResources.GenericHttpClient.tuple2Format
import SharedResources.GenericHttpClient.listFormat
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.GenericHttpClient.StringJsonFormat

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object LegalMoves {


    def isAttacker(board: Vector[Piece], attacker: Piece, position: Int, row: Int, colum: Int): Future[Try[Boolean]] = {
        val translation: Future[JsonResult[Boolean]] = GenericHttpClient.get[JsonResult[Boolean]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/onBoard",
            queryParams = Map("beginningPosition" -> position.toString, "rowDirection" -> row.toString, "columnDirection" -> colum.toString),
        )
        translation.map { onBoard =>
            val index = position + 8 * row + colum
            // Safely access the board
            Success(onBoard.result && board(index) == attacker)
        }.recover {
            case err => Failure(err)
        }
    }

    def readyingLegalMoveData(fen: String): Future[Try[(Vector[Piece], List[String], Int, Color, Color)]] = {
        GenericHttpClient.get[JsonResult[Vector[Piece]]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/fenToBoard",
            queryParams = Map("fen" -> fen)
        ).map { onBoard =>
            val fenSplit = fen.split(" ").toList
            extractColor(fenSplit(1)).map {
                case (attackColorNum, moveColor, attackColor) =>
                    (onBoard.result, fenSplit, attackColorNum, moveColor, attackColor)
            }
        }.recover { case err => Failure(err) }
    }

    def pawnAttack(fen: String, position: Int): Future[Try[Boolean]] = {
        def checkPawnAttack(board: Vector[Piece], moveColor: Color, moves: List[(Int, Int)]): Future[Try[Boolean]] = moves match {
            case Nil => Future.successful(Success(false))
            case (row, col) :: tail =>
                isAttacker(board, Piece(PieceType.PAWN, moveColor), position, row, col).flatMap {
                    case Failure(e) => Future.successful(Failure(e))
                    case Success(true) => Future.successful(Success(true))
                    case Success(false) => checkPawnAttack(board, moveColor, tail)
                }
        }

        readyingLegalMoveData(fen).flatMap {
            case Failure(e) => Future.successful(Failure(e))
            case Success((board, _, attackColorNum, moveColor, _)) =>
                val attacks = List((attackColorNum * -1, attackColorNum), (attackColorNum * -1, attackColorNum * -1))
                checkPawnAttack(board, moveColor, attacks)
        }
    }

    def knightAttack(fen: String, position: Int): Future[Try[Boolean]] = {
        def checkKnightAttack(board: Vector[Piece], moveColor: Color, moves: List[(Int, Int)]): Future[Try[Boolean]] = moves match {
            case Nil => Future.successful(Success(false))
            case (row, col) :: tail =>
                isAttacker(board, Piece(PieceType.KNIGHT, moveColor), position, row, col).flatMap {
                    case Failure(e) => Future.successful(Failure(e))
                    case Success(true) => Future.successful(Success(true))
                    case Success(false) => checkKnightAttack(board, moveColor, tail)
                }
        }

        readyingLegalMoveData(fen).flatMap {
            case Failure(e) => Future.successful(Failure(e))
            case Success((board, _, _, moveColor, _)) =>
                val payload = PieceMovesRequest(List(KNIGHT))
                GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    "http://basic-chess:8080",
                    "/chess/pieceMoves",
                    payload
                ).flatMap { response =>
                    checkKnightAttack(board, moveColor, response.result)
                }.recover { case e => Failure(e) }
        }
    }

    def checkSpacesInDirection(currentRow: Int, currentColumn: Int, position: Int, pieces: List[Piece], board: Vector[Piece]): Future[Boolean] = {

        def checkSpace(currentRow: Int, currentColumn: Int, position: Int): Future[Boolean] = {
            GenericHttpClient.get[JsonResult[Boolean]](
                baseUrl = "http://basic-chess:8080",
                route = "/chess/onBoard",
                queryParams = Map(
                    "beginningPosition" -> position.toString,
                    "rowDirection" -> currentRow.toString,
                    "columnDirection" -> currentColumn.toString
                )
            ).flatMap { result =>
                if (!result.result) Future.successful(false)
                else board.lift(position + 8 * currentRow + currentColumn) match {
                    case Some(piece) if piece == pieces.head || piece == pieces(1) =>
                        Future.successful(true)

                    case Some(piece) if piece.pieceType == PieceType.EMPTY && piece.color == Color.EMPTY =>
                        checkSpace(currentRow, currentColumn, position + 8 * currentRow + currentColumn) // Recursive call (async style)

                    case _ =>
                        Future.successful(false)
                }
            }.recoverWith {
                case ex => Future.failed(ex)
            }
        }

        checkSpace(currentRow, currentColumn, position)
    }

    def checkDirections(moves: List[(Int, Int)], board: Vector[Piece], position: Int, pieces: List[Piece]): Future[Boolean] = {
        moves match {
            case Nil => Future.successful(false)
            case (rowDirection, colDirection) :: tail =>
                checkSpacesInDirection(rowDirection, colDirection, position, pieces, board).flatMap {
                    case true => Future.successful(true)
                    case false => checkDirections(tail, board, position, pieces)
                }.recoverWith {
                    case ex => Future.failed(ex)
                }
        }
    }

    def horizontalAttack(fen: String, position: Int): Future[Try[Boolean]] = {
        readyingLegalMoveData(fen).flatMap {
            case Failure(e) => Future.successful(Failure(e))
            case Success((board, _, _, moveColor, _)) =>
                val payload = PieceMovesRequest(List(ROOK, QUEEN))
                GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    "http://basic-chess:8080",
                    "/chess/pieceMoves",
                    payload
                ).flatMap { response =>
                    checkDirections(response.result, board, position, List(Piece(ROOK, moveColor), Piece(QUEEN, moveColor))).map(Success(_))
                }.recover { case e => Failure(e) }
        }
    }

    def verticalAttack(fen: String, position: Int): Future[Try[Boolean]] = {
        readyingLegalMoveData(fen).flatMap {
            case Failure(e) => Future.successful(Failure(e))
            case Success((board, _, _, moveColor, _)) =>
                val payload = PieceMovesRequest(List(ROOK, QUEEN))
                GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    "http://basic-chess:8080",
                    "/chess/pieceMoves",
                    payload
                ).flatMap { response =>
                    checkDirections(response.result, board, position, List(Piece(BISHOP, moveColor), Piece(QUEEN, moveColor))).map(Success(_))
                }.recover { case e => Failure(e) }
        }
    }

    def kingAttack(fen: String, position: Int): Future[Try[Boolean]] = {
        def checkKingAttack(board: Vector[Piece], moveColor: Color, moves: List[(Int, Int)]): Future[Try[Boolean]] = moves match {
            case Nil => Future.successful(Success(false))
            case (row, col) :: tail =>
                isAttacker(board, Piece(PieceType.KING, moveColor), position, row, col).flatMap {
                    case Failure(e) => Future.successful(Failure(e))
                    case Success(true) => Future.successful(Success(true))
                    case Success(false) => checkKingAttack(board, moveColor, tail)
                }
        }

        readyingLegalMoveData(fen).flatMap {
            case Failure(e) => Future.successful(Failure(e))
            case Success((board, _, _, moveColor, _)) =>
                val payload = PieceMovesRequest(List(KING))
                GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    "http://basic-chess:8080",
                    "/chess/pieceMoves",
                    payload
                ).flatMap { response =>
                    checkKingAttack(board, moveColor, response.result)
                }.recover { case e => Failure(e) }
        }
    }

    def isPosAttacked(fen: String, position: Int): Future[Try[Boolean]] = {
        for {
            pawn <- pawnAttack(fen, position)
            knight <- knightAttack(fen, position)
            vertical <- verticalAttack(fen, position)
            horizontal <- horizontalAttack(fen, position)
            king <- kingAttack(fen, position)
        } yield for {
            p <- pawn
            n <- knight
            v <- vertical
            h <- horizontal
            k <- king
        } yield p || n || v || h || k
    }

    private def isLegalMove(fen: String, move: (Int,Int))
                           (implicit ec: ExecutionContext): Future[Try[Boolean]] = {
        readyingLegalMoveData(fen).flatMap {
            // couldn’t parse the board or colors
            case Failure(err) =>
                Future.successful(Failure(err))

            case Success((board, _, _, moveColor, _)) =>
                val (from, to) = move
                // 1) try the move
                val moveReq = MoveRequest(fen, Move(from = from, to = to))
                GenericHttpClient
                  .post[MoveRequest, JsonResult[String]](
                      "http://basic-chess:8080",
                      "/chess/makeMove",
                      moveReq
                  )
                  .flatMap { moveFenResult =>
                      val newFen = moveFenResult.result

                      // 2) rebuild board and colors from the new FEN
                      readyingLegalMoveData(newFen).flatMap {
                          case Failure(err2) =>
                              Future.successful(Failure(err2))

                          case Success((newBoard, _, _, _, _)) =>
                              // 3) find the king’s new index
                              val kingPosReq = PiecePositionRequest(newBoard, Piece(PieceType.KING, moveColor))
                              GenericHttpClient
                                .post[PiecePositionRequest, JsonResult[List[Int]]](
                                    "http://basic-chess:8080",
                                    "/chess/piecePositions",
                                    kingPosReq
                                )
                                .flatMap { kingPosResult =>
                                    val kingIndex = kingPosResult.result.head
                                    // 4) see if that square is attacked
                                    isPosAttacked(newFen, kingIndex).map {
                                        case Success(attacked) => Success(!attacked)
                                        case Failure(attErr)   => Failure(attErr)
                                    }
                                }
                                .recover { case e =>
                                    Failure(new Throwable("Error fetching king position: " + e.getMessage))
                                }
                      }
                  }
                  .recover { case e =>
                      Failure(new Throwable("Error making move: " + e.getMessage))
                  }
        }
    }

    def getAllLegalMoves(fen: String): Future[Try[List[(Int, Int)]]] = {
        def filterLegal(acc: List[(Int, Int)], pseudoMoves: List[(Int, Int)]): Future[Try[List[(Int, Int)]]] = pseudoMoves match {
            case Nil => Future.successful(Success(acc))
            case h :: t =>
                isLegalMove(fen, h).flatMap {
                    case Failure(err) => Future.successful(Failure(err))
                    case Success(true) => filterLegal(h :: acc, t)
                    case Success(false) => filterLegal(acc, t)
                }
        }

        GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/allPseudoLegalMoves",
            queryParams = Map("fen" -> fen)
        ).flatMap { moves =>
            val filterLegalResult = filterLegal(Nil, moves.result)
            filterLegalResult
        }.recover { case err => Failure(err) }
    }

    def isValidMove(move: (Int, Int), fen: String): Future[Try[(Int, Int)]] = {
        getAllLegalMoves(fen).map {
            case Success(legalMoves) if legalMoves.contains(mapMove(move)) => Success(move)
            case Success(_) => Failure(new IllegalArgumentException("not a valid Move"))
            case Failure(err) => Failure(err)
        }
    }

    def mapMove(move: (Int, Int)): (Int, Int) = move match {
        case (4, 2) => (-4, -1)
        case (4, 6) => (-3, -1)
        case (60, 62) => (-1, -1)
        case (60, 58) => (-2, -1)
        case _ => move
    }

    def extractColor(color: String): Try[(Int, Color, Color)] = color match {
        case "w" => Success((-1, Color.WHITE, Color.BLACK))
        case "b" => Success((1, Color.BLACK, Color.WHITE))
        case _ => Failure(new IllegalArgumentException("not a valid playing color"))
    }
}
