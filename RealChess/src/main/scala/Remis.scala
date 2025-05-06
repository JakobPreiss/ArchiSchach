package RealChess

import SharedResources.Requests.PiecePositionRequest
import SharedResources.{GenericHttpClient, JsonResult, Piece, PieceType}
import SharedResources.GenericHttpClient.{StringJsonFormat, listFormat, IntJsonFormat, ec}

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Remis {

    def isPatt(fen: String, legalMoves: List[(Int, Int)]): Future[Try[Boolean]] = {
        if (legalMoves.nonEmpty) {
            Future.successful(Success(false))
        } else {
            LegalMoves.readyingLegalMoveData(fen).flatMap {
                case Failure(err) => Future.successful(Failure(err))
                case Success((board, _, _, moveColor, _)) =>
                    val payload = PiecePositionRequest(
                        board = board,
                        piece = Piece(PieceType.KING, moveColor)
                    )

                    GenericHttpClient
                      .post[PiecePositionRequest, JsonResult[List[Int]]](
                          baseUrl = "http://basic-chess:8080",
                          route = "/chess/piecePositions",
                          payload = payload
                      )
                      .flatMap { kingPos =>
                          LegalMoves.isPosAttacked(fen, kingPos.result.head).map {
                              case Failure(err) => Failure(err)
                              case Success(isAttacked) => Success(!isAttacked)
                          }
                      }
                      .recover { case err => Failure(err) }
            }
        }
    }

    def isMaterial(fen: String): Boolean = {

        @tailrec
        def searchPieces(pieceList: List[Char], hadBishop: Boolean, hasWhiteExtra: Option[Boolean]): Boolean = {
            pieceList match {
                case Nil => true
                case h :: t => h match {
                    case 'p' | 'P' | 'r' | 'R' | 'q' | 'Q' => false

                    case 'n' =>
                        hasWhiteExtra match {
                            case Some(true) => false
                            case _ => searchPieces(t, hadBishop, Some(false))
                        }

                    case 'N' =>
                        hasWhiteExtra match {
                            case Some(false) => false
                            case _ => searchPieces(t, hadBishop, Some(true))
                        }

                    case 'b' =>
                        hasWhiteExtra match {
                            case Some(true) => false
                            case _ => false // bishop + minor = not remis
                        }

                    case 'B' =>
                        hasWhiteExtra match {
                            case Some(false) => false
                            case _ => false
                        }

                    case _ => searchPieces(t, hadBishop, hasWhiteExtra)
                }
            }
        }

        searchPieces(fen.split(" ")(0).toList, false, None)
    }

    def isRemis(fen: String, legalMoves: List[(Int, Int)]): Future[Try[Boolean]] = {
        isPatt(fen, legalMoves).map {
            case Failure(err) => Failure(err)
            case Success(patt) => Success(patt || isMaterial(fen))
        }
    }
}
