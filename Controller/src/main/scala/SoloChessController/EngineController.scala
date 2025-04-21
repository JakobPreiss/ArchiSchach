package Controller.SoloChessController

import SharedResources.{ApiFileTrait, ChessContext, ChessTrait, Event, GenericHttpClient, JsonResult, State}
import Controller.ControllerTrait
import Controller.Extra.{SetCommand, UndoInvoker}
import Controller.Requests.PromoteRequest
import Requests.{Move, MoveRequest}
import com.google.inject.Inject
import SharedResources.util.Observable
import spray.json.{JsNumber, JsObject, JsString, JsonFormat}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.ec
import SharedResources.GenericHttpClient.optionFormat
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.GenericHttpClient.tuple2Format
import SharedResources.GenericHttpClient.BooleanJsonFormat

class EngineController (override var fen : String, var context : ChessContext, var output : String, val depth: Int)(using val gameMode : ChessTrait)(using val fileapi: ApiFileTrait) extends Observable with ControllerTrait {
    var activeSquare : Option[Int] = Some(-5)
    var current_theme: Int = 0
    var errorMessage : String = ""

    def boardToString(): Unit = {
        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://localhost:5001",
            route = "/boardString",
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

    def resetBoard(): Unit = {
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        val legalMoves = gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves) => checkGameState(legalMoves)
            case Failure(err) =>
                failureHandle(err.getMessage)
                return
        }

        fileapi.printTo(context, fen)
        this.notifyObservers
    }

    def createOutput() : Try[String] = Success(output)

    def play(moveRaw: Try[(Int, Int)]): Unit = {
        val move = moveRaw match {
            case Success(value : (Int, Int)) => value
            case Failure(err) => failureHandle(err.getMessage)
                return
        }

        gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves : List[(Int, Int)]) => tryMove(move, legalMoves, false)
            case Failure(value) => failureHandle(value.getMessage)
        }

    }

    def tryMove(move: (Int, Int), legalMoves: List[(Int, Int)], thisIsEngineMove : Boolean): Unit = {
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
                baseUrl = "http://localhost:5001",
                route = "/makeMove",
                payload = payload
            )
            makeMove.onComplete {
                case Success(newFen: JsonResult[String]) =>
                    UndoInvoker.doStep(new SetCommand(newFen.result, fen, this))
                    checkPromotion(thisIsEngineMove)
                case Failure(err) =>
                    failureHandle(err.getMessage)
            }
        }
    }

    def checkPromotion(thisIsEngineMove: Boolean): Unit = {
        val promote: Future[JsonResult[Option[Int]]] = GenericHttpClient.get[JsonResult[Option[Int]]](
            baseUrl = "http://localhost:5001",
            route = "/canPromote",
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
                        gameMode.getAllLegalMoves(fen) match {
                            case Success(legalMoves) =>
                                val state = checkGameState(legalMoves)
                                fileapi.printTo(context, fen)
                                notifyObservers
                                if (!thisIsEngineMove) engineMove()

                            case Failure(err) =>
                                failureHandle(err.getMessage)
                        }
                }
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def engineMove(): Unit = {
        if (context.state.ordinal > 1) {
            return
        }
        gameMode.getBestMove(fen, depth) match {
            case Success(engineMoveString: String) =>
                val translation: Future[JsonResult[(Int, Int)]] = GenericHttpClient.get[JsonResult[(Int, Int)]](
                    baseUrl = "http://localhost:5001",
                    route = "/translateMoveStringToInt",
                    queryParams = Map("fen" -> fen, "move" -> engineMoveString)
                )
                translation.onComplete {
                    case Success(promoteValue) =>
                        gameMode.getAllLegalMoves(fen) match {
                            case Success(legalMoves2: List[(Int, Int)]) => tryMove(promoteValue.result, legalMoves2, true)
                            case Failure(err) => failureHandle(err.getMessage)
                    case Failure(err) => failureHandle(err.getMessage)
                }
            }
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def checkGameState(legalMoves: List[(Int, Int)]): Boolean = {
        gameMode.isRemis(fen, legalMoves) match {
            case Success(remis : Boolean) => val event: Event = Event(legalMoves.isEmpty, fen, remis)
                context.handle(event)
                context.state match {
                    case State.Remis => output += "\n \nRemis"
                        false
                    case State.WhiteWon => output += "\n \nSchwarz wurde vernichtend geschlagen"
                        false
                    case State.BlackWon => output += "\n \nWeiß wurde vernichtend geschlagen"
                        false
                    case _ => true
                }
            case Failure(err) =>
                failureHandle(err.getMessage)
                false
        }

    }

    def promotePawn(pieceKind: String): Unit = {
        val canPromote: Future[JsonResult[Option[Int]]] = GenericHttpClient.get[JsonResult[Option[Int]]](
            baseUrl = "http://localhost:5001",
            route = "/canPromote",
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
                            baseUrl = "http://localhost:5001",
                            route = "/promote",
                            payload = payload
                        )
                        promote.onComplete {
                            case Success(updatedFen: JsonResult[String]) =>
                                UndoInvoker.doStep(new SetCommand(updatedFen.result, fen, this))
                                fen = updatedFen.result
                                boardToString()
                                gameMode.getAllLegalMoves(fen) match {
                                    case Success(legalMoves) =>
                                        val state = checkGameState(legalMoves)
                                        fileapi.printTo(context, fen)
                                        deRingObservers
                                        notifyObservers
                                        if (!state) engineMove()
                                    case Failure(err) =>
                                        failureHandle(err.getMessage)
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
        gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves : List[(Int, Int)]) =>
                checkGameState(legalMoves)
                fileapi.printTo(context, fen)
                boardToString()
                notifyObservers
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def redo() : Unit = {
        UndoInvoker.redoStep()
        gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves : List[(Int, Int)]) => checkGameState(legalMoves)
                fileapi.printTo(context, fen)
                boardToString()
                notifyObservers
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def squareClicked(clickedSquare: Try[Int]) : Unit = {
        clickedSquare match {
            case Success(square : Int) =>
                val isColorPiece: Future[JsonResult[Boolean]] = GenericHttpClient.get[JsonResult[Boolean]](
                    baseUrl = "http://localhost:5001",
                    route = "/isColorPiece",
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
                                        baseUrl = "http://localhost:5001",
                                        route = "/translateCastleFromFen",
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

    def failureHandle(errorMsg: String): Unit = {
        errorMessage = errorMsg
        tellErrorToObservers
    }

    def getErrorMessage: Try[String] = {
        Success(errorMessage)
    }

    def translateMoveStringToInt(fen: String, move: String): Future[Try[(Int, Int)]] = {
        val translation: Future[JsonResult[(Int, Int)]] = GenericHttpClient.get[JsonResult[(Int, Int)]](
            baseUrl = "http://localhost:5001",
            route = "/translateMoveStringToInt",
            queryParams = Map("fen" -> fen, "move" -> move)
        )
        translation.map(x => Success(x.result)).recover {
            case ex =>
                Failure(ex)
        }
    }
}
