package Model.ChessComponent.DevourChess

import Model.BasicChessComponent.StandartChess.{BasicChessFacade, Color, Piece, PieceType}
import Model.BasicChessComponent.StandartChess.Color.EMPTY

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object LegalMoves {
    def getAllLegalMoves (fen : String) : Try[List[(Int,Int)]]= {
        @tailrec
        def filterLegalWithoutTake(accumulator: List[(Int, Int)], pseudoMoves: List[(Int, Int)]): Try[List[(Int, Int)]] = {
            pseudoMoves match {
                case Nil => Success(accumulator)
                case h :: t => {
                    isTakingMove(fen, h._2) match {
                        case Failure(err) => Failure(err)
                        case Success(isTaking) if(isTaking) =>
                           filterLegalWithTake(List(h), t)
                        case Success(isNotTaking) =>
                            filterLegalWithoutTake(h :: accumulator, t)
                    }
                }
            }
        }

        @tailrec
        def filterLegalWithTake(accumulator: List[(Int, Int)], pseudoMoves: List[(Int, Int)]): Try[List[(Int, Int)]] = {
            pseudoMoves match {
                case Nil => Success(accumulator);
                case h :: t => {
                    isTakingMove(fen, h._2) match {
                        case Failure(err) => Failure(err)
                        case Success(isTaking) if (isTaking) =>
                            filterLegalWithTake(List(h), t)
                        case Success(isNotTaking) =>
                            filterLegalWithTake(accumulator, t)
                    }
                }
            }
        }

        BasicChessFacade.getAllPseudoLegalMoves(fen) match {
            case Failure(err) => Failure(err)
            case Success(pseudoMoves) =>
                filterLegalWithoutTake(List(), pseudoMoves) match {
                    case Failure(err) => Failure(err)
                    case Success(legalMoves) => Success(legalMoves)
                }
        }

    }

    def isTakingMove(fen : String, attackedPosition : Int) : Try[Boolean] = {
        BasicChessFacade.isDifferentColorPiece(fen, attackedPosition)
    }

    def isValidMove(move: (Int, Int), fen: String): Try[(Int, Int)] = {
        val realMove = mapMove(move)
        getAllLegalMoves(fen) match {
            case Failure(err) => Failure(err)
            case Success(legalMoves) if(legalMoves.contains(realMove)) => Success(move)
            case Success(legalMoves) => Failure(new IllegalArgumentException("not a valid Move"))
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
