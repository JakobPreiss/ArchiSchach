package DevourChess

import SharedResources.{GenericHttpClient, JsonResult}

import SharedResources.GenericHttpClient.BooleanJsonFormat
import SharedResources.GenericHttpClient.ec
import SharedResources.GenericHttpClient.listFormat
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.GenericHttpClient.tuple2Format

import scala.annotation.tailrec
import scala.concurrent.Future
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
                            filterLegalWithTake(h :: accumulator, t)
                        case Success(isNotTaking) =>
                            filterLegalWithTake(accumulator, t)
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
                filterLegalWithoutTake(List(), moves.result) match {
                    case Failure(err) => return Failure(err)
                    case Success(legalMoves) => return Success(legalMoves)
                }
            case Failure(err) =>
                return Failure(err)
        }

        Failure(new Exception("Failed to getAllLegalMoves"))
    }

    def isTakingMove(fen : String, attackedPosition : Int) : Try[Boolean] = {
        val isDifferentColorPiece: Future[JsonResult[Boolean]] = GenericHttpClient.get[JsonResult[Boolean]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isDifferentColorPiece",
            queryParams = Map("fen" -> fen, "position" -> attackedPosition.toString)
        )
        isDifferentColorPiece.onComplete {
            case Success(value) =>
                return Success(value.result)
            case Failure(err) =>
                return Failure(err)
        }

        Failure(new Exception("Failed to get isDifferentColorPiece"))
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
