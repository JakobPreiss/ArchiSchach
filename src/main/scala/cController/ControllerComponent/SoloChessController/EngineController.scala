package cController.ControllerComponent.SoloChessController

import Model.ChessComponent.ChessTrait
import cController.ControllerComponent.ControllerTrait
import cController.ControllerComponent.Extra.{ChessContext, Event, SetCommand, State, UndoInvoker}
import cController.ControllerComponent.StateComponent.ApiFileTrait
import com.google.inject.Inject
import util.Observable

class EngineController (override var fen : String, var context : ChessContext, var output : String, val depth: Int)(using val gameMode : ChessTrait)(using val fileapi: ApiFileTrait) extends Observable with ControllerTrait {
    var activeSquare : Int = -5;
    var current_theme: Int = 0;

    def boardToString() : String = {gameMode.getBoardString(gameMode.fenToBoard(fen))}

    def resetBoard(): Unit = {
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        fileapi.printTo(context, fen)
        this.notifyObservers
    }
    
    def createOutput() : String = output

    def play(move : (Int, Int)) : Unit = {
        val legalMoves = gameMode.getAllLegalMoves(fen);
        if (!legalMoves.contains(move)) {
            output = "Das kannste nicht machen Bro (kein legaler Zug)"
            notifyObservers
            return
        } else {
            UndoInvoker.doStep(new SetCommand(gameMode.makeMove(fen, move), fen, this))
            if (gameMode.canPromote(fen) != -1) {
                ringObservers
                output = "Welche Beförderung soll der Bauer erhalten? (Eingabemöglichkeiten: Q,q,N,n,B,b,R,r)"
            } else {
                output = boardToString()
                val state = checkGameState(legalMoves)

                notifyObservers
                if (state) {
                    return
                }

                val engineMoveString = gameMode.getBestMove(fen, depth)

                val engineMoveInt = gameMode.translateMoveStringToInt(fen, engineMoveString)

                UndoInvoker.doStep(new SetCommand(gameMode.makeMove(fen, engineMoveInt), fen, this))
                val legalMoves2 = gameMode.getAllLegalMoves(fen);
                output = boardToString()
                checkGameState(legalMoves2)
            }
        }
        notifyObservers
    }

    def checkGameState(legalMoves : List[(Int, Int)]) : Boolean = {
        val event: Event = Event(legalMoves.isEmpty, fen, gameMode.isRemis(fen, legalMoves))
        context.handle(event)
        context.state match {
            case State.remisState => output += "\n \nRemis"
                true
            case State.whiteWonState => output += "\n \nSchwarz wurde vernichtend geschlagen"
                true
            case State.blackWonState => output += "\n \nWeiß wurde vernichtend geschlagen"
                true
            case _ => false
        }
    }

    def promotePawn(pieceKind : String) : Unit = {
        fen = gameMode.promote(pieceKind, fen, gameMode.canPromote(fen));
        output = boardToString()
        deRingObservers
    }

    def undo(): Unit = {
        UndoInvoker.undoStep()
        checkGameState(gameMode.getAllLegalMoves(fen))
        fileapi.printTo(context, fen)
        output = boardToString()
        notifyObservers
    }

    def redo() : Unit = {
        UndoInvoker.redoStep()
        checkGameState(gameMode.getAllLegalMoves(fen))
        fileapi.printTo(context, fen)
        output = boardToString()
        notifyObservers
    }

    def squareClicked(clickedSquare: Int) : Unit = {
        if(gameMode.isColorPiece(fen, clickedSquare)) {
            activeSquare = clickedSquare
        } else if (!gameMode.isColorPiece(fen, clickedSquare) && activeSquare != -5) {
            play(gameMode.translateCastle(gameMode.fenToBoard(fen), (activeSquare, clickedSquare)))
            activeSquare = -5
        }
    }

    def nextTheme(): Unit = {
        current_theme = (current_theme + 1) % 19
        notifyObservers
    }
}
