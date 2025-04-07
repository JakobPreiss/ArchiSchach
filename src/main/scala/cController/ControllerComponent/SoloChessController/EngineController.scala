package cController.ControllerComponent.SoloChessController

import Model.ChessComponent.ChessTrait
import cController.ControllerComponent.ControllerTrait
import cController.ControllerComponent.Extra.{ChessContext, Event, SetCommand, State, UndoInvoker}
import cController.ControllerComponent.StateComponent.ApiFileTrait
import com.google.inject.Inject
import util.Observable

import scala.util.{Success, Try}

class EngineController (override var fen : String, var context : ChessContext, var output : String, val depth: Int)(using val gameMode : ChessTrait)(using val fileapi: ApiFileTrait) extends Observable with ControllerTrait {
    var activeSquare : Int = -5;
    var current_theme: Int = 0;
    var errorMessage : String = ""

    def boardToString(): String = {
        gameMode.getBoardString(fen) match {
            case Success(value : String) => value
            case Failure(value) => failureHandle(value.getMessage)
                ""
        }
    }
    
    def resetBoard(): Unit = {
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        checkGameState(gameMode.getAllLegalMoves(fen))
        fileapi.printTo(context, fen)
        this.notifyObservers
    }
    
    def createOutput() : String = output

    def play(move: (Int, Int)): Unit = {
        def tryMove(move: (Int, Int), legalMoves: List[(Int, Int)]): Unit = {
            if (!legalMoves.contains(move)) {
                output = "Das kannste nicht machen Bro (kein legaler Zug)"
                checkGameState(legalMoves)
            } else {
                gameMode.makeMove(fen, move) match {
                    case Success(newFen : String) => UndoInvoker.doStep(new SetCommand(newFen, fen, this))
                    case Failure(err) => failureHandle(err.getMessage)
                }
            }
        }

        def checkPromotion(): Unit = {
            if (gameMode.canPromote(fen).isDefined) {
                ringObservers
                output = "Welche Beförderung soll der Bauer erhalten? (Eingabemöglichkeiten: Q,q,N,n,B,b,R,r)"
            } else {
                output = boardToString()
                gameMode.getAllLegalMoves(fen) match {
                    case Success(legalMoves : List[(Int, Int)]) => 
                        val state = checkGameState(legalMoves)
                        fileapi.printTo(context, fen)
                        notifyObservers
                    case Failure(value) => failureHandle(value.getMessage)
                        return
                }
            }
        }
        
        def engineMove(state : Boolean) : Unit = {
            if (state) {
                return
            }
            gameMode.getBestMove(fen, depth) match {
                case Success(engineMoveString : String) =>
                    gameMode.translateMoveStringToInt(fen, engineMoveString) match {
                        case Success(engineMoveInt : (Int, Int)) =>
                            gameMode.getAllLegalMoves(fen) match {
                                case Success(legalMoves2 : List[(Int, Int)]) =>
                                    tryMove(engineMoveInt, legalMoves2)
                                case Failure(err) => failureHandle(err.getMessage)
                            }
                        case Failure(err) => failureHandle(err.getMessage)
                    }
                case Failure(err) => failureHandle(err.getMessage)
            }
        }    

        gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves : List[(Int, Int)]) => tryMove(move, legalMoves)
            case Failure(value) => failureHandle(value.getMessage)
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
        }

    }

    def promotePawn(pieceKind: String): Unit = {
        gameMode.canPromote(fen) match {
            case Success(position : Int) =>
                gameMode.promote(pieceKind, fen, position) match {
                    case Success(updatedFen : String) =>
                        fen = updatedFen
                        output = boardToString()
                        deRingObservers
                    case Failure(err) =>
                        failureHandle(err.getMessage)
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
                if(gameMode.isColorPiece(fen, square)) {
                    activeSquare = Some(square)
                } else if (!gameMode.isColorPiece(fen, square) && activeSquare != None) {
                    activeSquare match {
                        case Some(newSquare : Int) =>
                            play(gameMode.translateCastle(fen, (newSquare, square)))
                            activeSquare = None
                        case None => None
                    }

                }
            case Failure(err) => failureHandle(err)
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
}
