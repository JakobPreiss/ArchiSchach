package RealChess

import BasicChess.StandartChess.{BasicChessFacade, Color, Piece, PieceType}
import BasicChess.StandartChess.PieceType.{BISHOP, KING, KNIGHT, QUEEN, ROOK}

import scala.annotation.tailrec
import scala.util.{Success, Try, Failure}

object LegalMoves {


    def isAttacker(board: Vector[Piece], attacker: Piece, position: Int, row: Int, colum: Int): Try[Boolean] = {
        BasicChessFacade.onBoard(position, row, colum) match {
            case Success(onBoard) =>
                Success(onBoard && board(position + 8 * row + colum) == attacker)
            case Failure(err) => Failure(err)
        }

    }

    def readyingLegalMoveData(fen: String): Try[(Vector[Piece], List[String], Int, Color, Color)] = {
        BasicChessFacade.fenToBoard(fen) match {
            case Success(board) =>
                val fenSplit: List[String] = fen.split(" ").toList
                extractColor(fenSplit(1)) match {
                    case Success((attackColorNum, moveColor, attackColor)) =>
                        Success((board, fenSplit, attackColorNum, moveColor, attackColor))
                    case Failure(exception) => Failure(exception)
                }
            case Failure(exception) => Failure(exception)


        }

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
                BasicChessFacade.pieceMoves(List(KNIGHT)) match {
                    case Success(attackList) => checkKnightAttack(board, moveColor, attackList)
                    case Failure(err) => Failure(err)
                }
            case Failure(err) => Failure(err)
        }
    }

    def checkSpacesInDirection(currentRow: Int, currentColum: Int, position: Int, pieces: List[Piece], board: Vector[Piece]): Try[Boolean] = {
        @tailrec
        def checkSpaceInDirection(currentRow: Int, currentColum: Int, position: Int, pieces: List[Piece], board: Vector[Piece]): Try[Boolean] = {
            BasicChessFacade.onBoard(position, currentRow, currentColum) match {
                case Failure(err) => Failure(err)
                case Success(value) if (!value) => Success(false)
                case Success(value) => board(position + 8 * currentRow + currentColum) match {
                    case piece if piece.equals(pieces.head) || piece.equals(pieces(1)) => Success(true)
                    case piece if (piece.pieceType == PieceType.EMPTY && piece.color == Color.EMPTY) => checkSpaceInDirection(currentRow, currentColum, position + 8 * currentRow + currentColum, pieces, board);
                    case _ => Success(false);
                }
            }
        }
        checkSpaceInDirection(currentRow, currentColum, position, pieces, board)
    }

    @tailrec
    def checkDirections(moves: List[(Int, Int)], board: Vector[Piece], position: Int, pieces: List[Piece]): Try[Boolean] = {
        moves match {
            case Nil => Success(false);
            case (rowDirection, columDirection) :: t => {
                checkSpacesInDirection(rowDirection, columDirection, position, pieces, board) match {
                    case Failure(exception) => Failure(exception)
                    case Success(value) if (value) => Success(true)
                    case Success(value) => checkDirections(t, board, position, pieces)
                }
            }
        }
    }

    def horizontalAttack(fen: String, position: Int): Try[Boolean] = {
        readyingLegalMoveData(fen) match {
            case Failure(exception) => Failure(exception)
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) => BasicChessFacade.pieceMoves(List(ROOK, QUEEN)) match {
                case Failure(exception) => Failure(exception)
                case Success(attacks) => checkDirections(attacks, board, position, List(Piece(ROOK, moveColor), Piece(QUEEN, moveColor)))
            }
        }
    }

    def verticalAttack(fen: String, position: Int): Try[Boolean] = {
        readyingLegalMoveData(fen) match {
            case Failure(exception) => Failure(exception)
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) => BasicChessFacade.pieceMoves(List(BISHOP, QUEEN)) match {
                case Failure(exception) => Failure(exception)
                case Success(attacks) => checkDirections(attacks, board, position, List(Piece(BISHOP, moveColor), Piece(QUEEN, moveColor)))
            }
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
            case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) => BasicChessFacade.pieceMoves(List(KING)) match {
                case Success(attackList) => checkKingAttack(board, moveColor, attackList)
                case Failure(err) => Failure(err)
            }
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
                val (from, to) = move;
                BasicChessFacade.piecePositions(board, Piece(PieceType.KING, moveColor)) match {
                    case Success(kingPos) =>
                        BasicChessFacade.makeMove(fen, move) match {
                            case Success(moveFen) if (from == kingPos.head) =>
                                isPosAttacked(moveFen, to) match {
                                    case Failure(err) => Failure(err)
                                    case Success(isAttacked) => Success(!isAttacked)}
                            case Success(moveFen) =>
                                isPosAttacked(moveFen, kingPos.head) match {
                                    case Failure(err) => Failure(err)
                                    case Success(isAttacked) => Success(!isAttacked)
                                }
                            case Failure(exception) => Failure(exception)
                        }
                    case Failure(exception) => Failure(exception)
            }
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

        BasicChessFacade.getAllPseudoLegalMoves(fen) match {
            case Failure(err) => Failure(err)
            case Success(pseudoMoves) => filterLegal(List(), pseudoMoves)
        }
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
