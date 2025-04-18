package Controller.SoloChessController

import BasicChess.StandartChess.BasicChessFacade
import SharedResources.ChessTrait
import Controller.ControllerTrait
import Controller.Extra.{ChessContext, Event, SetCommand, State, UndoInvoker}
import Controller.StateComponent.ApiFileTrait
import com.google.inject.Inject
import SharedResources.util.Observable

import scala.util.{Failure, Success, Try}

class EngineController (override var fen : String, var context : ChessContext, var output : String, val depth: Int)(using val gameMode : ChessTrait)(using val fileapi: ApiFileTrait) extends Observable with ControllerTrait {
    var activeSquare : Option[Int] = Some(-5)
    var current_theme: Int = 0
    var errorMessage : String = ""

    def boardToString(): String = {
        BasicChessFacade.getBoardString(fen) match {
            case Success(value : String) => value
            case Failure(value) => failureHandle(value.getMessage)
                ""
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
    
    def createOutput() : String = output

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
            BasicChessFacade.makeMove(fen, move) match {
                case Success(newFen: String) => UndoInvoker.doStep(new SetCommand(newFen, fen, this))
                    checkPromotion(thisIsEngineMove)
                case Failure(err) => failureHandle(err.getMessage)
            }
        }
    }

    def checkPromotion(thisIsEngineMove : Boolean): Unit = {
        val canPromote = BasicChessFacade.canPromote(fen) match {
            case Success(value) => value
            case Failure(err) => failureHandle(err.getMessage)
                return
        }
        if (canPromote.isDefined) {
            output = "Welche Beförderung soll der Bauer erhalten? (Eingabemöglichkeiten: Q,q,N,n,B,b,R,r)"
            ringObservers
        } else {
            output = boardToString()
            gameMode.getAllLegalMoves(fen) match {
                case Success(legalMoves: List[(Int, Int)]) => val state = checkGameState(legalMoves)
                    fileapi.printTo(context, fen)
                    notifyObservers
                    if(!thisIsEngineMove) {engineMove()}
                case Failure(value) => failureHandle(value.getMessage)
                    return
            }
        }
    }

    def engineMove(): Unit = {
        if (context.state.ordinal > 1) {
            return
        }
        gameMode.getBestMove(fen, depth) match {
            case Success(engineMoveString: String) => BasicChessFacade.translateMoveStringToInt(fen, engineMoveString) match {
                case Success(engineMoveInt: (Int, Int)) => gameMode.getAllLegalMoves(fen) match {
                    case Success(legalMoves2: List[(Int, Int)]) => tryMove(engineMoveInt, legalMoves2, true)
                    case Failure(err) => failureHandle(err.getMessage)
                }
                case Failure(err) => failureHandle(err.getMessage)
            }
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def checkGameState(legalMoves: List[(Int, Int)]): Boolean = {
        gameMode.isRemis(fen, legalMoves) match {
            case Success(remis : Boolean) => val event: Event = Event(legalMoves.isEmpty, fen, remis)
                context.handle(event)
                context.state match {
                    case State.remisState => output += "\n \nRemis"
                        false
                    case State.whiteWonState => output += "\n \nSchwarz wurde vernichtend geschlagen"
                        false
                    case State.blackWonState => output += "\n \nWeiß wurde vernichtend geschlagen"
                        false
                    case _ => true
                }
            case Failure(err) =>
                failureHandle(err.getMessage)
                false
        }

    }

    def promotePawn(pieceKind: String): Unit = {
        BasicChessFacade.canPromote(fen) match {
            case Success(position : Option[Int]) =>
                position match {
                    case Some(pos : Int) =>
                        BasicChessFacade.promote(pieceKind, fen, pos) match {
                            case Success(updatedFen : String) =>
                                fen = updatedFen
                                output = boardToString()
                                deRingObservers
                                notifyObservers
                                engineMove()
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
                output = boardToString()
                notifyObservers
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def redo() : Unit = {
        UndoInvoker.redoStep()
        gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves : List[(Int, Int)]) => checkGameState(legalMoves)
                fileapi.printTo(context, fen)
                output = boardToString()
                notifyObservers
            case Failure(err) => failureHandle(err.getMessage)
        }
    }

    def squareClicked(clickedSquare: Try[Int]) : Unit = {
        clickedSquare match {
            case Success(square : Int) =>
                val colorPiece = BasicChessFacade.isColorPiece(fen, square) match {
                    case Success(isColor : Boolean) => isColor
                    case Failure(err) => failureHandle(err.getMessage)
                        return
                }

                if(colorPiece) {
                    activeSquare = Some(square)
                } else if (!colorPiece && activeSquare.isDefined) {
                    activeSquare match {
                        case Some(newSquare : Int) =>
                            play(BasicChessFacade.translateCastleFromFen(fen, (newSquare, square)))
                            activeSquare = None
                        case None => None
                    }

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

    def getErrorMessage: String = {
        errorMessage
    }

    def translateMoveStringToInt(fen: String, move: String): Try[(Int, Int)] = {
        BasicChessFacade.translateMoveStringToInt(fen, move)
    }
}
