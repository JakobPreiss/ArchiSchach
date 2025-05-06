package Controller.SoloChessController

import SharedResources.{ApiFileTrait, ChessContext, ChessTrait, Event, GenericHttpClient, JsonResult, State}
import Controller.{ControllerServer, ControllerTrait}
import Controller.Extra.{SetCommand, UndoInvoker}
import Controller.Requests.{PromoteRequest, RemisRequest, SaveRequest}
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
import SharedResources.GenericHttpClient.listFormat
import SharedResources.GenericHttpClient.BooleanJsonFormat

class EngineController (override var fen : String, var context : ChessContext, var output : String, val depth: Int, val gameMode : String, val saveApi : String) extends Observable with ControllerTrait {
    var activeSquare : Option[Int] = Some(-5)
    var current_theme: Int = 0
    var errorMessage : String = ""

    def printTo(context: ChessContext, fen: String) = {
        val payload = SaveRequest(
            fen  = fen,
            ctx = context.state.ordinal
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
                ControllerServer.notifyObservers()
            case Failure(value) => failureHandle(value.getMessage)
        }
    }

    def createOutput() : Try[String] = Success(output)

    def play(moveRaw: Try[(Int, Int)]): Unit = {
        val move = moveRaw match {
            case Success(value : (Int, Int)) => value
            case Failure(err) => failureHandle(err.getMessage)
                return
        }

        val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
            baseUrl = gameMode,
            route = "/chess/getAllLegalMoves",
            queryParams = Map("fen" -> fen)
        )
        legalMoves.onComplete {
            case Success(legalMoves) =>
                tryMove(move, legalMoves.result, false)
            case Failure(value) => failureHandle(value.getMessage)
        }
    }

    def tryMove(move: (Int, Int), legalMoves: List[(Int, Int)], thisIsEngineMove : Boolean): Unit = {
        if (!legalMoves.contains(move)) {
            output = "Das kannste nicht machen Bro (kein legaler Zug)"
            checkGameState(legalMoves)
            ControllerServer.notifyObservers()
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
                    checkPromotion(thisIsEngineMove)
                case Failure(err) =>
                    failureHandle(err.getMessage)
            }
        }
    }

    def checkPromotion(thisIsEngineMove: Boolean): Unit = {
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
                        ControllerServer.ringObservers()
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
                                ControllerServer.notifyObservers()
                                if (!thisIsEngineMove) engineMove()
                            case Failure(value) => failureHandle(value.getMessage)
                        }
                }
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def engineMove(): Unit = {
        if (context.state.ordinal > 1) {
            return
        }

        val bestMove: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = gameMode,
            route = "/chess/getBestMove",
            queryParams = Map("fen" -> fen, "depth" -> depth.toString)
        )
        bestMove.onComplete {
            case Success(engineMove) =>
                val translation: Future[JsonResult[(Int, Int)]] = GenericHttpClient.get[JsonResult[(Int, Int)]](
                    baseUrl = "http://basic-chess:8080",
                    route = "/chess/translateMoveStringToInt",
                    queryParams = Map("fen" -> fen, "move" -> engineMove.result)
                )
                translation.onComplete {
                    case Success(promoteValue) =>
                        val legalMoves: Future[JsonResult[List[(Int, Int)]]] = GenericHttpClient.get[JsonResult[List[(Int, Int)]]](
                            baseUrl = gameMode,
                            route = "/chess/getAllLegalMoves",
                            queryParams = Map("fen" -> fen)
                        )
                        legalMoves.onComplete {
                            case Success(legalMoves) =>
                                tryMove(promoteValue.result, legalMoves.result, true)
                            case Failure(value) => failureHandle(value.getMessage)
                        }
                    case Failure(err) => failureHandle(err.getMessage)
                }
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
                                        ControllerServer.deRingObservers()
                                        ControllerServer.notifyObservers()
                                        if (!state) engineMove()
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
                ControllerServer.notifyObservers()
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
                ControllerServer.notifyObservers()
            case Failure(value) => failureHandle(value.getMessage)
        }
    }

    def squareClicked(clickedSquare: Try[Int]) : Unit = {
        println("Engine Controller called")
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
                                            failureHandle("/chess/translateCastleFromFen " + err.getMessage)
                                            return
                                    }
                                case None => None
                            }

                        }
                    case Failure(err) =>
                        failureHandle("/chess/isColorPiece " + err.getMessage)
                            return
                }
            case Failure(err) =>
                failureHandle("/chess/isColorPiece outside " + err.getMessage)
        }
    }

    def nextTheme(): Unit = {
        current_theme = (current_theme + 1) % 19
        ControllerServer.notifyObservers()
    }

    def failureHandle(errorMsg: String): Unit = {
        println("Got new error message: " + errorMsg)
        errorMessage = errorMsg
        ControllerServer.tellErrorToObservers()
    }

    def getErrorMessage: Try[String] = {
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
