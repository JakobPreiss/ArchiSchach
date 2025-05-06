package DevourChess

import SharedResources.{GenericHttpClient, JsonResult}
import SharedResources.GenericHttpClient.{BooleanJsonFormat, ec, listFormat, IntJsonFormat, tuple2Format}

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object LegalMoves {

    def isTakingMove(fen: String, attackedPosition: Int): Future[Try[Boolean]] = {
        GenericHttpClient.get[JsonResult[Boolean]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/isDifferentColorPiece",
            queryParams = Map("fen" -> fen, "position" -> attackedPosition.toString)
        ).map { response =>
            Success(response.result)
        }.recover { case err => Failure(err) }
    }

    def getAllLegalMoves(fen: String): Future[Try[List[(Int, Int)]]] = {
        def filterLegalWithoutTake(
                                    acc: List[(Int, Int)],
                                    pseudo: List[(Int, Int)]
                                  ): Future[Try[List[(Int, Int)]]] = pseudo match {
            case Nil => Future.successful(Success(acc))
            case h :: t =>
                isTakingMove(fen, h._2).flatMap {
                    case Failure(err) => Future.successful(Failure(err))
                    case Success(true) => filterLegalWithTake(List(h), t)
                    case Success(false) => filterLegalWithoutTake(h :: acc, t)
                }
        }

        def filterLegalWithTake(
                                 acc: List[(Int, Int)],
                                 pseudo: List[(Int, Int)]
                               ): Future[Try[List[(Int, Int)]]] = pseudo match {
            case Nil => Future.successful(Success(acc))
            case h :: t =>
                isTakingMove(fen, h._2).flatMap {
                    case Failure(err) => Future.successful(Failure(err))
                    case Success(true) => filterLegalWithTake(h :: acc, t)
                    case Success(false) => filterLegalWithTake(acc, t)
                }
        }

        GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/allPseudoLegalMoves",
            queryParams = Map("fen" -> fen)
        ).flatMap { response =>
            filterLegalWithoutTake(Nil, response.result)
        }.recover { case err => Failure(err) }
    }

    def isValidMove(move: (Int, Int), fen: String): Future[Try[(Int, Int)]] = {
        val realMove = mapMove(move)
        getAllLegalMoves(fen).map {
            case Failure(err) => Failure(err)
            case Success(moves) if moves.contains(realMove) => Success(move)
            case Success(_) => Failure(new IllegalArgumentException("not a valid Move"))
        }
    }

    def mapMove(move: (Int, Int)): (Int, Int) = move match {
        case (4, 2) => (-4, -1)
        case (4, 6) => (-3, -1)
        case (60, 62) => (-1, -1)
        case (60, 58) => (-2, -1)
        case _ => move
    }
}
