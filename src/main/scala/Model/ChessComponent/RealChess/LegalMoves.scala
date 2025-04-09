package Model.ChessComponent.RealChess

import Model.*
import Model.ChessComponent.*
import Model.ChessComponent.BasicChessComponent.StandartChess.PieceType.{BISHOP, KING, KNIGHT, QUEEN, ROOK}
import Model.ChessComponent.BasicChessComponent.StandartChess.{BasicChessFacade, Color, Piece, PieceType}

import scala.annotation.tailrec
import scala.util.{Success, Try, Failure}

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

        val attacks: List[(Int, Int)] = List((attackColorNum * - 1, attackColorNum), (attackColorNum * -1, attackColorNum * -1))

        @tailrec
        def checkPawnAttack(moves: List[(Int, Int)]): Boolean = {
            moves match {
                case Nil => false
                case (rowDirection, columDirection) :: t => {
                    if (isAttacker(board, Piece(PieceType.PAWN, moveColor), position, rowDirection, columDirection)) {
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
                    if (isAttacker(board, Piece(PieceType.KNIGHT, moveColor), position, rowDirection, columDirection)) {
                        return true
                    }
                    checkKnightAttack(t)
                }
            }
        }

        checkKnightAttack(attacks)
    }


    def checkSpacesInDirection(currentRow: Int, currentColum: Int, position: Int, pieces: List[Piece], board: Vector[Piece]): Boolean = {

        @tailrec
        def checkSpaceInDirection(currentRow: Int, currentColum: Int, position: Int, pieces: List[Piece], board: Vector[Piece]): Boolean = {
            if (!BasicChessFacade.onBoard(position, currentRow, currentColum)) {
                return false;
            }
            board(position + 8 * currentRow + currentColum) match {
                case piece if piece.equals(pieces.head) || piece.equals(pieces(1)) => true
                case piece if(piece.pieceType == PieceType.EMPTY && piece.color == Color.EMPTY) => checkSpaceInDirection(currentRow, currentColum, position + 8 * currentRow + currentColum, pieces, board);
                case _ => false;
            }
        }


        checkSpaceInDirection(currentRow, currentColum, position, pieces, board)
    }

    @tailrec
    def checkDirections(moves: List[(Int, Int)], board: Vector[Piece], position: Int, pieces: List[Piece]): Boolean = {
        moves match {
            case Nil => false;
            case (rowDirection, columDirection) :: t => {
                if (checkSpacesInDirection(rowDirection, columDirection, position, pieces, board)) {
                    true;
                } else {
                    checkDirections(t, board, position, pieces)
                }
            }
        }
    }

    def horizontalAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)
        val attacks: List[(Int, Int)] = BasicChessFacade.pieceMoves(List(ROOK, QUEEN))
        checkDirections(attacks, board, position, List(Piece(ROOK, moveColor), Piece(QUEEN, moveColor)))
    }

    def verticalAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)
        val attacks: List[(Int, Int)] = BasicChessFacade.pieceMoves(List(BISHOP, QUEEN))
        checkDirections(attacks, board, position, List(Piece(BISHOP, moveColor), Piece(QUEEN, moveColor)))
    }

    def kingAttack(fen: String, position: Int): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)

        val attacks: List[(Int, Int)] = BasicChessFacade.pieceMoves(List(KING))

        @tailrec
        def checkKingAttack(moves: List[(Int, Int)]): Boolean = {
            moves match {
                case Nil => false
                case (rowDirection, columDirection) :: t => {
                    if (isAttacker(board, Piece(PieceType.KING, moveColor), position, rowDirection, columDirection)) {
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

    def makeMove(fen : String, move: (Int, Int)): String = {
        val fenSplit = fen.split(" ")
        val board = BasicChessFacade.fenToBoard(fen);
        val newBoard = move match {
            case (-1, -1) => {
                val (e, k, r) = BasicChessFacade.calculateMoveValues(Color.WHITE)
                board.updated(60, e).updated(62, k).updated(63, e).updated(61, r);
            }
            case (-2, -1) => {
                val (e, k, r) = BasicChessFacade.calculateMoveValues(Color.WHITE)
                board.updated(60, e).updated(58, k).updated(56, e).updated(59, r);
            }
            case (-3, -1) => {
                val (e, k, r) = BasicChessFacade.calculateMoveValues(Color.BLACK)
                board.updated(4, e).updated(6, k).updated(7, e).updated(5, r);
            }
            case (-4, -1) => {
                val (e, k, r) = BasicChessFacade.calculateMoveValues(Color.BLACK)
                board.updated(4, e).updated(2, k).updated(0, e).updated(3, r);
            }
            case _ => {
                val (from, to) = move;
                val from_piece = board(from);
                board.updated(from, Piece(PieceType.EMPTY, Color.EMPTY)).updated(to, from_piece);
            }
        }
        if (fenSplit(1) == "w") {
            BasicChessFacade.boardToFen(newBoard) + " b " + BasicChessFacade.updateCastleing(fenSplit(2), move) + " " + BasicChessFacade.updateEnpassant(fen, move) + " " + fenSplit(4) + " " + fenSplit(5)
        } else {
            BasicChessFacade.boardToFen(newBoard) + " w " + BasicChessFacade.updateCastleing(fenSplit(2), move) + " " + BasicChessFacade.updateEnpassant(fen, move) + " " + fenSplit(4) + " " + (fenSplit(5).toInt + 1).toString
        }
    }


    def isLegalMove(fen: String, move: (Int, Int)): Boolean = {
        val (board, fenSplit, attackColorNum, moveColor, attackColor) = readyingLegalMoveData(fen)
        val (from, to) = move;
        val kingPos: Int = BasicChessFacade.piecePositions(board, Piece(PieceType.KING, moveColor)).head
        val moveFen = makeMove(fen, move)
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

    // TODO: Implement the logic to check if the move is valid based on the current game state
    def isValidMove(move: (Int, Int), fen: String): Try[(Int, Int)] = {
        getAllLegalMoves(fen).contains(mapMove(move)) match {
            case true => Success(move)
            case false => Failure(new IllegalArgumentException("not a valid Move"))
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
}
