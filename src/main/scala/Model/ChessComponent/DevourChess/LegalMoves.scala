package Model.ChessComponent.DevourChess

import Model.ChessComponent.BasicChessComponent.StandartChess.Color.EMPTY
import Model.ChessComponent.BasicChessComponent.StandartChess.{BasicChessFacade, Color, Piece, PieceType}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object LegalMoves {
    def getAllLegalMoves (fen : String) : List[(Int,Int)]= {
        @tailrec
        def filterLegalWithoutTake(accumulator: List[(Int, Int)], pseudoMoves: List[(Int, Int)]): List[(Int, Int)] = {
            pseudoMoves match {
                case Nil => accumulator;
                case h :: t => {
                    if (isTakingMove(fen, h._2)) {
                        filterLegalWithTake(List(h), t);
                    } else {
                        filterLegalWithoutTake(h :: accumulator, t);
                    }
                }
            }
        }

        @tailrec
        def filterLegalWithTake(accumulator: List[(Int, Int)], pseudoMoves: List[(Int, Int)]): List[(Int, Int)] = {
            pseudoMoves match {
                case Nil => accumulator;
                case h :: t => {
                    if (isTakingMove(fen, h._2)) {
                        filterLegalWithTake(h :: accumulator, t);
                    } else {
                        filterLegalWithTake(accumulator, t);
                    }
                }
            }
        }
        filterLegalWithoutTake(List(), BasicChessFacade.getAllPseudoLegalMoves(fen));
    }

    def isTakingMove(fen : String, attackedPosition : Int) : Boolean = {
        BasicChessFacade.isDifferentColorPiece(fen, attackedPosition)
    }

    def makeMove(fen: String, move: (Int, Int)): String = {
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

    // TODO: Implement the logic to check if the move is valid based on the current game state
    def isValidMove(move: (Int, Int), fen: String): Try[(Int, Int)] = {
        val legalMoves = getAllLegalMoves(fen)
        val realMove = mapMove(move)
        legalMoves.contains(realMove) match {
            case true => Success(move)
            case false => Failure(new IllegalArgumentException("not a valid Move"))
        }
    }

    def mapMove(move: (Int, Int)): (Int, Int) = {
        move match {
            case (4, 2) => (-4, -1)
            case (4, 6) => (-3, -1)
            case (60, 62) => (-1, -1)
            case (60, 58) => (-2, -1)
            case _ => move
        }
    }
}
