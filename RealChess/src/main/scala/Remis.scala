package RealChess

import SharedResources.Requests.PiecePositionRequest
import SharedResources.{GenericHttpClient, JsonResult, Piece, PieceType}

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.listFormat
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.GenericHttpClient.ec

object Remis {
    def isPatt(fen: String, legalMoves: List[(Int, Int)]): Try[Boolean] = {
        if (legalMoves.isEmpty) {
            LegalMoves.readyingLegalMoveData(fen) match {
                case Failure(err) => Failure(err)
                case Success((board, fenSplit, attackColorNum, moveColor, attackColor)) =>
                    val payload = PiecePositionRequest (
                        board = board,
                        piece = Piece(PieceType.KING, moveColor),
                    )
                    val promote: Future[JsonResult[List[Int]]] = GenericHttpClient.post[PiecePositionRequest, JsonResult[List[Int]]](
                        baseUrl = "http://localhost:5001",
                        route = "/piecePositions",
                        payload = payload
                    )
                    promote.onComplete {
                        case Failure(err) => 
                            println(s"Error: $err")
                            return Failure(err)
                        case Success(kingPos) =>
                            LegalMoves.isPosAttacked(fen, kingPos.result.head) match {
                                case Failure(err) => Failure(err)
                                case Success(isAttacked) if (isAttacked) => return Success(false)
                                case Success(isNotAttacked) => return Success(true)
                            }
                            
                    }
                    Success(false)
                }
        } else {
            Success(false)
        }
    }
    
    

    def isMaterial(fen: String): Boolean = {
        
        @tailrec
        def searchPieces(pieceList: List[Char], hadBishop: Boolean, hasWhiteExtra: Option[Boolean]): Boolean = {
            pieceList match {
                case Nil => true; //Changed to true
                case h::t => h match {
                    case 'p' => false
                    case 'P' => false
                    case 'r' => false
                    case 'R' => false
                    case 'q' => false
                    case 'Q' => false
                    case 'n' =>
                        hasWhiteExtra match {
                            case Some(a) =>
                                if (a) {
                                    false
                                } else {
                                    searchPieces(t, hadBishop, Some(false));
                                }
                            case None => searchPieces(t, hadBishop, Some(false));
                        }
                        
                    case 'N' =>
                        hasWhiteExtra match {
                            case Some(a) =>
                                if (!a) {
                                    false
                                } else {
                                    searchPieces(t, hadBishop, Some(true));
                                }
                            case None => searchPieces(t, hadBishop, Some(true));
                        }
                        
                    case 'b' =>
                        hasWhiteExtra match {
                            case Some(a) =>
                                if (a) {
                                    false
                                } else {
                                    false
                                }
                            case None => searchPieces(t, true, Some(false))

                        }
                    case 'B' =>
                        hasWhiteExtra match {
                            case Some(a) =>
                                if (!a) {
                                    false
                                } else {
                                    false
                                }
                            case None =>
                                    searchPieces(t, true, Some(true))
                        }
                    case _ => searchPieces(t, hadBishop, hasWhiteExtra)
                }
            }
        }
        searchPieces(fen.split(" ")(0).toList, false, None);
    }

    def isRemis(fen: String, legalMoves: List[(Int, Int)]) : Try[Boolean] = {
        isPatt(fen, legalMoves) match {
            case Failure(err) => Failure(err)
            case Success(patt) => Success(patt || isMaterial(fen))
        }
    }
}
