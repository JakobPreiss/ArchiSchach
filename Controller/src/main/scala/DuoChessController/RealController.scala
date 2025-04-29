package Controller.DuoChessController

import SharedResources.{ApiFileTrait, ChessContext, ChessTrait, Event, GenericHttpClient, JsonResult, State}
import Controller.ControllerTrait
import Controller.Extra.{SetCommand, UndoInvoker}
import Controller.Requests.{PromoteRequest, RemisRequest, SaveRequest}
import Requests.{Move, MoveRequest}
import SharedResources.util.Observable
import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.ec
import SharedResources.GenericHttpClient.optionFormat
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.GenericHttpClient.tuple2Format
import SharedResources.GenericHttpClient.BooleanJsonFormat
import SharedResources.GenericHttpClient.listFormat

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class RealController(override var fen : String, var context : ChessContext, var output : String, val gameMode: String, val saveApi: String) extends Observable with ControllerTrait {
    var activeSquare : Option[Int] = None
    var current_theme: Int = 0
    var errorMessage : String = ""

    def printTo(context: ChessContext, fen: String) = {
        val payload = SaveRequest(
            fen  = fen
        )
        val saveFuture: Future[JsonResult[String]] = GenericHttpClient.post[SaveRequest, JsonResult[String]](
            baseUrl = saveApi,
            route = "/apifile/printTo",
            payload = payload
        )
        saveFuture.onComplete {
            case Success(newFen: JsonResult[String]) =>
            case Failure(err) =>
                failureHandle(err.getMessage)
        }
    }

    def boardToString(): Unit = {
        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/boardString",
            queryParams = Map("fen" -> fen)
        )
        boardFuture.onComplete {
            case Success(value) =>
                output = value.result
            case Failure(err) =>
                failureHandle(err.getMessage)
                output = ""
        }
    }

    def createOutput() : Try[String] = {Success(output)}

    def resetBoard(): Unit = {
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = gameMode,
            route = "/chess/getAllLegalMoves",
            queryParams = Map("fen" -> fen)
        )
        legalMoves.onComplete {
            case Success(legalMoves) =>
                checkGameState(legalMoves.result)
                printTo(context, fen)
                boardToString()
                deRingObservers
            case Failure(value) => failureHandle(value.getMessage)
        }
    }

    def play(moveRaw : Try[(Int, Int)]) : Unit = {
        val move = moveRaw match {
            case Success(value : (Int, Int)) => value
            case Failure(err) => failureHandle(err.getMessage)
                return
        }
        def tryMove(move: (Int, Int), legalMoves: List[(Int, Int)]): Unit = {
            if (!legalMoves.contains(move)) {
                output = "Das kannste nicht machen Bro (kein legaler Zug)"
                checkGameState(legalMoves)
                notifyObservers
            } else {
                val payload = MoveRequest(
                    fen  = fen,
                    move = Move(from = move(0), to = move(1))
                )
                val makeMove: Future[JsonResult[String]] = GenericHttpClient.post[MoveRequest, JsonResult[String]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/makeMove",
                    payload = payload
                )
                makeMove.onComplete {
                    case Success(newFen: JsonResult[String]) =>
                        UndoInvoker.doStep(new SetCommand(newFen.result, fen, this))
                        checkPromotion()
                    case Failure(err) =>
                        failureHandle(err.getMessage)
                }
            }
        }

        def checkPromotion(): Unit = {
            val promote: Future[JsonResult[Option[Int]]] = GenericHttpClient.get[JsonResult[Option[Int]]](
                baseUrl = "http://basic-chess:8080",
                route = "/chess/canPromote",
                queryParams = Map("fen" -> fen)
            )
            promote.onComplete {
                case Success(promoteValue) =>
                    promoteValue.result match {
                        case Some(pos: Int) =>
                            output = "Welche Beförderung soll der Bauer erhalten? (Eingabemöglichkeiten: Q,q,N,n,B,b,R,r)"
                            ringObservers
                        case None =>
                            boardToString()

                            val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
                                baseUrl = gameMode,
                                route = "/chess/getAllLegalMoves",
                                queryParams = Map("fen" -> fen)
                            )
                            legalMoves.onComplete {
                                case Success(legalMoves) =>
                                    val state = checkGameState(legalMoves.result)
                                    printTo(context, fen)
                                    notifyObservers
                                case Failure(value) => failureHandle(value.getMessage)
                            }
                    }
                case Failure(err) => failureHandle(err.getMessage)
            }
        }

        val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = gameMode,
            route = "/chess/getAllLegalMoves",
            queryParams = Map("fen" -> fen)
        )
        legalMoves.onComplete {
            case Success(legalMoves) => tryMove(move, legalMoves.result)
            case Failure(value) => failureHandle(value.getMessage)
        }
    }

    def checkGameState(legalMoves: List[(Int, Int)]): Boolean = {
        val payload = RemisRequest(
            fen = fen,
            legalMoves = legalMoves
        )
        val remisFuture: Future[JsonResult[Boolean]] = GenericHttpClient.post[RemisRequest, JsonResult[Boolean]](
            baseUrl = gameMode,
            route = "/chess/promote",
            payload = payload
        )
        remisFuture.onComplete {
            case Success(remis) => val event: Event = Event(legalMoves.isEmpty, fen, remis.result)
                context.handle(event)
                context.state match {
                    case State.Remis => output += "\n \nRemis"
                        return false
                    case State.WhiteWon => output += "\n \nSchwarz wurde vernichtend geschlagen"
                        return false
                    case State.BlackWon => output += "\n \nWeiß wurde vernichtend geschlagen"
                        return false
                    case _ => return true
                }
            case Failure(err) =>
                failureHandle(err.getMessage)
                return false
        }
        false
    }

    def promotePawn(pieceKind: String): Unit = {
        val canPromote: Future[JsonResult[Option[Int]]] = GenericHttpClient.get[JsonResult[Option[Int]]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/canPromote",
            queryParams = Map("fen" -> fen)
        )
        canPromote.onComplete {
            case Success(promoteValue) =>
                promoteValue.result match {
                    case Some(pos: Int) =>
                        val payload = PromoteRequest (
                            piecename = pieceKind,
                            fen = fen,
                            position = pos
                        )
                        val promote: Future[JsonResult[String]] = GenericHttpClient.post[PromoteRequest, JsonResult[String]](
                            baseUrl = "http://basic-chess:8080",
                            route = "/chess/promote",
                            payload = payload
                        )
                        promote.onComplete {
                            case Success(updatedFen: JsonResult[String]) =>
                                UndoInvoker.doStep(new SetCommand(updatedFen.result, fen, this))
                                fen = updatedFen.result
                                boardToString()

                                val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
                                    baseUrl = gameMode,
                                    route = "/chess/getAllLegalMoves",
                                    queryParams = Map("fen" -> fen)
                                )
                                legalMoves.onComplete {
                                    case Success(legalMoves) =>
                                        val state = checkGameState(legalMoves.result)
                                        printTo(context, fen)
                                        deRingObservers
                                        notifyObservers
                                    case Failure(value) => failureHandle(value.getMessage)
                                }
                            case Failure(err) =>
                                failureHandle(err.getMessage)
                        }
                    case None => output = "Kein Bauer kann befördert werden"
                }
            case Failure(err) =>
                failureHandle(err.getMessage)
        }
    }

    def undo(): Unit = {
        UndoInvoker.undoStep()

        val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = gameMode,
            route = "/chess/getAllLegalMoves",
            queryParams = Map("fen" -> fen)
        )
        legalMoves.onComplete {
            case Success(legalMoves) =>
                checkGameState(legalMoves.result)
                printTo(context, fen)
                boardToString()
                notifyObservers
            case Failure(value) => failureHandle(value.getMessage)
        }
    }

    def redo() : Unit = {
        UndoInvoker.redoStep()
        val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = gameMode,
            route = "/chess/getAllLegalMoves",
            queryParams = Map("fen" -> fen)
        )
        legalMoves.onComplete {
            case Success(legalMoves) =>
                checkGameState(legalMoves.result)
                printTo(context, fen)
                boardToString()
                notifyObservers
            case Failure(value) => failureHandle(value.getMessage)
        }
    }

    def squareClicked(clickedSquare: Try[Int]) : Unit = {
        clickedSquare match {
            case Success(square : Int) =>
                val isColorPiece: Future[JsonResult[Boolean]] = GenericHttpClient.get[JsonResult[Boolean]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/isColorPiece",
                    queryParams = Map("fen" -> fen, "position" -> square.toString)
                )
                isColorPiece.onComplete {
                    case Success(value) =>
                        if (value.result) {
                            activeSquare = Some(square)
                        } else if (!value.result && activeSquare.isDefined) {
                            activeSquare match {
                                case Some(newSquare: Int) =>
                                    val translateCastleFromFen: Future[JsonResult[(Int, Int)]] = GenericHttpClient.get[JsonResult[(Int, Int)]](
                                        baseUrl = "http://basic-chess:8080",
                                        route = "/chess/translateCastleFromFen",
                                        queryParams = Map("fen" -> fen, "from" -> newSquare.toString, "to" -> square.toString)
                                    )
                                    translateCastleFromFen.onComplete {
                                        case Success(value) =>
                                            play(Success(value.result))
                                            activeSquare = None
                                        case Failure(err) =>
                                            failureHandle(err.getMessage)
                                            return
                                    }
                                case None => None
                            }

                        }
                    case Failure(err) =>
                        failureHandle(err.getMessage)
                        return
                }
            case Failure(err) =>
                failureHandle(err.getMessage)
        }
    }

    def nextTheme(): Unit = {
        current_theme = (current_theme + 1) % 19
        notifyObservers
    }

    def failureHandle(errorMsg : String): Unit = {
        errorMessage = errorMsg
        tellErrorToObservers
    }

    def getErrorMessage : Try[String] = {
        Success(errorMessage)
    }

    def translateMoveStringToInt(fen: String, move: String): Future[Try[(Int, Int)]] = {
        val translation: Future[JsonResult[(Int, Int)]] = GenericHttpClient.get[JsonResult[(Int, Int)]](
            baseUrl = "http://basic-chess:8080",
            route = "/chess/translateMoveStringToInt",
            queryParams = Map("fen" -> fen, "move" -> move)
        )
        translation.map(x => Success(x.result)).recover {
            case ex =>
                Failure(ex)
        }
    }
}

