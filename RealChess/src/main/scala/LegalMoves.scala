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
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object LegalMoves {


    def isAttacker(board: Vector[Piece], attacker: Piece, position: Int, row: Int, colum: Int): Try[Boolean] = {
        val translation: Future[JsonResult[Boolean]] = GenericHttpClient.get[JsonResult[Boolean]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/onBoard",
            queryParams = Map("position" -> position.toString, "row" -> row.toString, "colum" -> colum.toString),
        )
        translation.onComplete {
            case Success(onBoard) =>
                return Success(onBoard.result && board(position + 8 * row + colum) == attacker)
            case Failure(err) =>
                return Failure(err)
        }
        Failure(new Exception("Should not be here"))
    }

    def readyingLegalMoveData(fen: String): Try[(Vector[Piece], List[String], Int, Color, Color)] = {
        val translation: Future[JsonResult[Vector[Piece]]] = GenericHttpClient.get[JsonResult[Vector[Piece]]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/fenToBoard",
            queryParams = Map("fen" -> fen),
        )
        translation.onComplete {
            case Success(onBoard) =>
                val fenSplit: List[String] = fen.split(" ").toList
                extractColor(fenSplit(1)) match {
                    case Success((attackColorNum, moveColor, attackColor)) =>
                        return Success((onBoard.result, fenSplit, attackColorNum, moveColor, attackColor))
                    case Failure(exception) => return Failure(exception)
                }
            case Failure(err) =>
                return Failure(err)
        }

        Failure(new Exception("Should not be here"))
    }
    
    def pawnAttack(fen: String, position: Int): Try[Boolean] = {
        @tailrec
        def checkPawnAttack(board : Vector[Piece], moveColor : Color, moves: List[(Int, Int)]): Try[Boolean] = {
            moves match {
                case Nil => Success(false)
                case (rowDirection, columDirection) :: t => {
                    isAttacker(board, Piece(PieceType.PAWN, moveColor), position, rowDirection, columDirection) match {
                        case Failure(exception) => return Failure(exception)
                        case Success(value) if(value) => return Success(true)
                        case Success(value) => return checkPawnAttack(board, moveColor, t)
                    }
                }
            }
        }

        readyingLegalMoveData(fen) match {
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) =>
                val attacks: List[(Int, Int)] = List((attackColorNum * -1, attackColorNum), (attackColorNum * -1, attackColorNum * -1))
                checkPawnAttack(board, moveColor, attacks)
            case Failure(err) => Failure(err)
        }
    }

    def knightAttack(fen: String, position: Int): Try[Boolean] = {

        @tailrec def checkKnightAttack(board : Vector[Piece], moveColor : Color, moves: List[(Int, Int)]): Try[Boolean] = {
            moves match {
                case Nil => Success(false)
                case (rowDirection, columDirection) :: t => {
                    isAttacker(board, Piece(PieceType.KNIGHT, moveColor), position, rowDirection, columDirection) match {
                        case Failure(exception) => return Failure(exception)
                        case Success(value) if (value) => return Success(true)
                        case Success(value) => return checkKnightAttack(board, moveColor, t)
                    }
                }
            }
        }

        readyingLegalMoveData(fen) match {
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) =>
                val payload = PieceMovesRequest(
                    types = List(KNIGHT),
                )
                val pieceMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/pieceMoves",
                    payload = payload
                )
                pieceMoves.onComplete {
                    case Success(attackList) => return checkKnightAttack(board, moveColor, attackList.result)
                    case Failure(err) => return Failure(err)
                }
            case Failure(err) => Failure(err)
        }

        Failure(new Exception("Should not be here"))
    }

    def checkSpacesInDirection(currentRow: Int, currentColumn: Int, position: Int, pieces: List[Piece], board: Vector[Piece]): Future[Boolean] = {

        def checkSpace(currentRow: Int, currentColumn: Int, position: Int): Future[Boolean] = {
            GenericHttpClient.get[JsonResult[Boolean]](
                baseUrl = "http://basic-chess:8080",
                route = "/chess/onBoard",
                queryParams = Map(
                    "position" -> position.toString,
                    "row" -> currentRow.toString,
                    "colum" -> currentColumn.toString
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

    def horizontalAttack(fen: String, position: Int): Try[Boolean] = {
        readyingLegalMoveData(fen) match {
            case Failure(exception) => Failure(exception)
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) =>
                val payload = PieceMovesRequest(
                    types = List(ROOK, QUEEN),
                )
                val pieceMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/pieceMoves",
                    payload = payload
                )
                pieceMoves.onComplete {
                    case Success(attackList) =>
                        checkDirections(attackList.result, board, position, List(Piece(ROOK, moveColor), Piece(QUEEN, moveColor))).onComplete {
                            case Success(value) => return Success(value)
                            case Failure(err) => return Failure(err)
                        }
                    case Failure(err) => return Failure(err)
                }

                Failure(new Exception("Should not be here"))
        }
    }

    def verticalAttack(fen: String, position: Int): Try[Boolean] = {
        readyingLegalMoveData(fen) match {
            case Failure(exception) => Failure(exception)
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) =>
                val payload = PieceMovesRequest(
                    types = List(ROOK, QUEEN),
                )
                val pieceMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/pieceMoves",
                    payload = payload
                )
                pieceMoves.onComplete {
                    case Success(attackList) =>
                        checkDirections(attackList.result, board, position, List(Piece(BISHOP, moveColor), Piece(QUEEN, moveColor))).onComplete {
                            case Success(value) => return Success(value)
                            case Failure(err) => return Failure(err)
                        }
                    case Failure(err) => return Failure(err)
                }

                Failure(new Exception("Should not be here"))

        }
    }

    def kingAttack(fen: String, position: Int): Try[Boolean] = {
        @tailrec def checkKingAttack(board: Vector[Piece], moveColor: Color, moves: List[(Int, Int)]): Try[Boolean] = {
            moves match {
                case Nil => Success(false)
                case (rowDirection, columDirection) :: t => {
                    isAttacker(board, Piece(PieceType.KING, moveColor), position, rowDirection, columDirection) match {
                        case Failure(exception) => return Failure(exception)
                        case Success(value) if (value) => return Success(true)
                        case Success(value) => return checkKingAttack(board, moveColor, t)
                    }
                }
            }
        }

        readyingLegalMoveData(fen) match {

            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) =>
                val payload = PieceMovesRequest(
                    types = List(KING),
                )
                val pieceMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.post[PieceMovesRequest, JsonResult[List[(Int, Int)]]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/pieceMoves",
                    payload = payload
                )
                pieceMoves.onComplete {
                    case Success(attackList) => return checkKingAttack(board, moveColor, attackList.result)
                    case Failure(err) => return Failure(err)
                }

                Failure(new Exception("Should not be here"))
            case Failure(err) => Failure(err)
        }
    }

    def isPosAttacked(fen: String, position: Int): Try[Boolean] = {
        pawnAttack(fen, position) match {
            case Failure(err) => Failure(err)
            case Success(p) =>
                knightAttack(fen, position) match {
                    case Failure(err) => Failure(err)
                    case Success(n) =>
                        verticalAttack(fen, position) match {
                            case Failure(err) => Failure(err)
                            case Success(v) =>
                                horizontalAttack(fen, position) match {
                                    case Failure(err) => Failure(err)
                                    case Success(h) =>
                                        kingAttack(fen, position) match {
                                            case Failure(err) => Failure(err)
                                            case Success(k) =>
                                                Success(p || n || v || h || k)
                                        }
                                }

                        }

                }
        }
    }

    def isLegalMove(fen: String, move: (Int, Int)): Try[Boolean]= {
        readyingLegalMoveData(fen) match {
            case Failure(err) => Failure(err)
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) =>
                val (from, to) = move
                val payload = PiecePositionRequest(
                    board = board,
                    piece = Piece(PieceType.KING, moveColor),
                )
                val promote: Future[JsonResult[List[Int]]] = GenericHttpClient.post[PiecePositionRequest, JsonResult[List[Int]]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/piecePositions",
                    payload = payload
                )
                promote.onComplete {
                    case Failure(err) =>
                        println(s"Error: $err")
                        return Failure(err)
                    case Success(kingPos) =>
                        val payload = MoveRequest(
                            fen  = fen,
                            move = Move(from = kingPos.result(0), to = kingPos.result(1))
                        )
                        val makeMove: Future[JsonResult[String]] = GenericHttpClient.post[MoveRequest, JsonResult[String]](
                            baseUrl = "http://basic-chess:8080",
                            route = "/chess/makeMove",
                            payload = payload
                        )
                        makeMove.onComplete {
                            case Success(moveFen) if (from == kingPos.result.head) =>
                            isPosAttacked(moveFen.result, to) match {
                                case Failure(err) => Failure(err)
                                case Success(isAttacked) => Success(!isAttacked)
                            }
                            case Success(moveFen) =>
                            isPosAttacked(moveFen.result, kingPos.result.head) match {
                                case Failure(err) => Failure(err)
                                case Success(isAttacked) => Success(!isAttacked)
                            }
                            case Failure(exception) => Failure(exception)
                        }
                }
                Failure(new Exception("Should not be here"))
            }

    }

    def getAllLegalMoves(fen: String): Try[List[(Int, Int)]] = {
        @tailrec def filterLegal(accumulator: List[(Int, Int)], pseudoMoves: List[(Int, Int)]): Try[List[(Int, Int)]] = {
            pseudoMoves match {
                case Nil => Success(accumulator);
                case h :: t => {
                    isLegalMove(fen, h) match {
                        case Failure(err) => Failure(err)
                        case Success(isLegal) if (isLegal) => filterLegal(h :: accumulator, t)
                        case Success(isNotLegal) => filterLegal(accumulator, t)
                    }
                }
            }
        }

        val allPseudoLegalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/allPseudoLegalMoves",
            queryParams = Map("fen" -> fen)
        )
        allPseudoLegalMoves.onComplete {
            case Success(moves) =>
                filterLegal(List(), moves.result) match {
                    case Failure(err) => return Failure(err)
                    case Success(legalMoves) => return Success(legalMoves)
                }
            case Failure(err) =>
                return Failure(err)
        }

        Failure(new Exception("Failed to getAllLegalMoves"))
    }

    def isValidMove(move: (Int, Int), fen: String): Try[(Int, Int)] = {
        getAllLegalMoves(fen) match {
            case Success(legalMoves) =>
                legalMoves.contains(mapMove(move)) match {
                    case true => Success(move)
                    case false => Failure(new IllegalArgumentException("not a valid Move"))
                }
            case Failure(err) => Failure(err)
        }

    }

    def mapMove(move: (Int, Int)) : (Int, Int) = {
        move match {
            case (4, 2) => (-4, -1)
            case (4, 6) => (-3, -1)
            case (60, 62) => (-1, -1)
            case (60, 58) => (-2, -1)
            case _ => move
        }
    }

    def extractColor(color: String): Try[(Int, Color, Color)] = {
        color match {
            case "w" => Success((-1, Color.WHITE, Color.BLACK));
            case "b" => Success((1, Color.BLACK, Color.WHITE));
            case _ => Failure(new IllegalArgumentException("not a valid playing color"))
        }
    }
}
