package Model.ChessComponent.RealChess

import Model.*
import Model.ChessComponent.*
import Model.ChessComponent.BasicChessComponent.StandartChess.PieceType.{BISHOP, KING, KNIGHT, QUEEN, ROOK}
import Model.ChessComponent.BasicChessComponent.StandartChess.{BasicChessFacade, Color, Piece, PieceType}

import scala.annotation.tailrec

object LegalMoves {


    def isAttacker(board: Vector[Piece], attacker: Piece, position: Int, row: Int, colum: Int): Boolean = {
        BasicChessFacade.onBoard(position, row, colum) && board(position + 8 * row + colum) == attacker
    }

    def readyingLegalMoveData(fen: String): (Vector[Piece], List[String], Int, Color, Color) = {
        val board: Vector[Piece] = BasicChessFacade.fenToBoard(fen)
        val fenSplit: List[String] = fen.split(" ").toList

        val (attackColorNum, moveColor, attackColor): (Int, Color, Color) = BasicChessFacade.extractColor(fenSplit(1))

        (board, fenSplit, attackColorNum, moveColor, attackColor)
    }
    
    def pawnAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)

        val attacks: List[(Int, Int)] = List((attackColorNum, attackColorNum), (attackColorNum, attackColorNum * -1))

        @tailrec
        def checkPawnAttack(moves: List[(Int, Int)]): Boolean = {
            moves match {
                case Nil => false
                case (rowDirection, columDirection) :: t => {
                    if (isAttacker(board, Piece(PieceType.PAWN, attackColor), position, rowDirection, columDirection)) {
                        return true
                    }
                    checkPawnAttack(t)
                }
            }
        }

        checkPawnAttack(attacks)
    }

    def knightAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)

        val attacks: List[(Int, Int)] = BasicChessFacade.pieceMoves(List(KNIGHT))

        @tailrec
        def checkKnightAttack(moves: List[(Int, Int)]): Boolean = {
            moves match {
                case Nil => false
                case (rowDirection, columDirection) :: t => {
                    if (isAttacker(board, Piece(PieceType.KNIGHT, attackColor), position, rowDirection, columDirection)) {
                        return true
                    }
                    checkKnightAttack(t)
                }
            }
        }

        checkKnightAttack(attacks)
    }


    def horizontalAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)

        val attacks: List[(Int, Int)] = BasicChessFacade.pieceMoves(List(ROOK, QUEEN))

        @tailrec
        def checkSpaceInDirection(currentRow: Int, currentColum: Int, position: Int): Boolean = {
            if (!BasicChessFacade.onBoard(position, currentRow, currentColum)) {
                return false;
            }

            board(position + 8 * currentRow + currentColum) match {
                case Piece(e, `attackColor`) if e == PieceType.ROOK || e == PieceType.QUEEN => true
                case Piece(PieceType.EMPTY, Color.EMPTY) => checkSpaceInDirection(currentRow, currentColum, position + 8 * currentRow + currentColum);
                case _ => false;
            }
        }


        @tailrec
        def checkHorizontalDirection(moves: List[(Int, Int)]): Boolean = {
            moves match {
                case Nil => false;
                case (rowDirection, columDirection) :: t => {
                    if(checkSpaceInDirection(rowDirection, columDirection, position)) {
                        true;
                    } else {
                        checkHorizontalDirection(t);
                    }
                }
            }
        }

        checkHorizontalDirection(attacks);
    }

    def verticalAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)

        val attacks: List[(Int, Int)] = BasicChessFacade.pieceMoves(List(BISHOP, QUEEN))

        @tailrec
        def checkSpaceInDirection(lr: Int, lc: Int, pos: Int): Boolean = {
            if (!BasicChessFacade.onBoard(pos, lr, lc)) {
                return false;
            }

            board(pos + 8 * lr + lc) match {
                case Piece(e, `attackColor`) if e == PieceType.BISHOP || e == PieceType.QUEEN => true;
                case Piece(PieceType.EMPTY, Color.EMPTY) => checkSpaceInDirection(lr, lc, pos + 8 * lr + lc);
                case _ => false;
            }
        }


        @tailrec
        def checkVerticalDirection(moves: List[(Int, Int)]): Boolean = {
            moves match {
                case Nil => false;
                case (rd, cd) :: t => {
                    if (checkSpaceInDirection(rd, cd, position)) {
                        true;
                    } else {
                        checkVerticalDirection(t);
                    }
                }
            }
        }

        checkVerticalDirection(attacks);
    }

    def kingAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)

        val attacks: List[(Int, Int)] = BasicChessFacade.pieceMoves(List(KING))

        @tailrec
        def checkKingAttack(moves: List[(Int, Int)]): Boolean = {
            moves match {
                case Nil => false
                case (rowDirection, columDirection) :: t => {
                    if (isAttacker(board, Piece(PieceType.KING, attackColor), position, rowDirection, columDirection)) {
                        return true
                    }
                    checkKingAttack(t)
                }
            }
        }

        checkKingAttack(attacks)
    }



    def isPosAttacked(fen: String, position: Int): Boolean = {
        pawnAttack(fen, position) || knightAttack(fen, position) || verticalAttack(fen, position) || horizontalAttack(fen, position) || kingAttack(fen, position)
    }

    private def calculateMoveValues(color: Color) = {
        val e = Piece(PieceType.EMPTY, Color.EMPTY)
        val K = Piece(PieceType.KING, color)
        val R = Piece(PieceType.ROOK, color)
        (e, K, R)
    }

    def makeMove(board: Vector[Piece], move: (Int, Int)): Vector[Piece] = {
        move match {
            case (-1, -1) => {
                val (e, k, r) = calculateMoveValues(Color.WHITE)
                board.updated(60, e).updated(62, k).updated(63, e).updated(61, r);
            }
            case (-2, -1) => {
                val (e, k, r) = calculateMoveValues(Color.WHITE)
                board.updated(60, e).updated(58, k).updated(56, e).updated(59, r);
            }
            case (-3, -1) => {
                val (e, k, r) = calculateMoveValues(Color.BLACK)
                board.updated(4, e).updated(6, k).updated(7, e).updated(5, r);
            }
            case (-4, -1) => {
                val (e, k, r) = calculateMoveValues(Color.BLACK)
                board.updated(4, e).updated(2, k).updated(0, e).updated(3, r);
            }
            case _ => {
                val (from, to) = move;
                val from_piece = board(from);
                board.updated(from, Piece(PieceType.EMPTY, Color.EMPTY)).updated(to, from_piece);
            }
        }

    }

    def isLegalMove(fen: String, move: (Int, Int)): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)
        val (from, to) = move;
        val kingPos: Int = BasicChessFacade.piecePositions(board, Piece(PieceType.KING, moveColor)).head
        val moveFen = BasicChessFacade.boardToFen(makeMove(board, move)) + " " + fenSplit.tail.mkString(" ")
        if (from == kingPos) {
            !isPosAttacked(moveFen, to)
        } else {
            !isPosAttacked(moveFen, kingPos)
        }
    }

    def getAllLegalMoves(fen: String): List[(Int, Int)] = {
        @tailrec
        def filterLegal(accumulator: List[(Int, Int)], pseudoMoves: List[(Int, Int)]): List[(Int, Int)] = {
            pseudoMoves match {
                case Nil => accumulator;
                case h :: t => {
                    if (isLegalMove(fen, h)) {
                        filterLegal(h::accumulator, t);
                    } else {
                        filterLegal(accumulator, t);
                    }
                }
            }
        }
        filterLegal(List(), BasicChessFacade.getAllPseudoLegalMoves(fen));
    }

}
