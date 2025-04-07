package cController.ControllerComponent.RealChessController

import Model.ChessComponent.ChessTrait
import cController.ControllerComponent.ControllerTrait
import cController.ControllerComponent.Extra.{ChessContext, Event, SetCommand, State, UndoInvoker}
import cController.ControllerComponent.StateComponent.ApiFileTrait
import util.Observable

import scala.util.Try

class Controller(override var fen : String, var context : ChessContext, var output : String)(using val gameMode : ChessTrait)(using val fileapi: ApiFileTrait) extends Observable with ControllerTrait {
    var activeSquare : Option[Int] = None
    var current_theme: Int = 0
    var errorMessage : String = ""
    
    def boardToString() : String = {
        gameMode.getBoardString(fen) match {
            case Success(value) => value
            case Failure(value) => 
                failureHandle(value.getMessage)
                ""
        }
    }

    def createOutput() : String = {output}

    def resetBoard(): Unit = {
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        checkGameState(gameMode.getAllLegalMoves(fen))
        fileapi.printTo(context, fen)
        notifyObservers
    }

    def play(move : (Int, Int)) : Unit = {
        def tryMove(move : (Int, Int), legalMoves : List[(Int, Int)]) : Unit = {
            if (!legalMoves.contains(move)) {
                output = "Das kannste nicht machen Bro (kein legaler Zug)"
                checkGameState(legalMoves)
            } else {
                gameMode.makeMove(fen, move) match {
                    case Success(newFen) => UndoInvoker.doStep(new SetCommand(newFen, fen, this))
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
                    case Success(legalMoves) => checkGameState(legalMoves)
                        fileapi.printTo(context, fen)
                        notifyObservers
                    case Failure(value) => failureHandle(value.getMessage)
                        return
                }
            }
        }

        gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves) => tryMove(move, legalMoves)
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

    def failureHandle(errorMsg : String): Unit = {
        errorMessage = errorMsg
        tellErrorToObservers
    }

    //give error an UI
    //notifyMyAss
    //-> Tui und Gui geben Error aus

    //Dominik macht Model
    //Jakob macht Controller Failure und Controller Methode und TUI/GUI Methoden
}

