package cController.ControllerComponent.RealChessController

import Model.ChessComponent.ChessTrait
import cController.ControllerComponent.ControllerTrait
import cController.ControllerComponent.Extra.{ChessContext, Event, SetCommand, State, UndoInvoker}
import cController.ControllerComponent.StateComponent.ApiFileTrait
import util.Observable

class Controller(override var fen : String, var context : ChessContext, var output : String)(using val gameMode : ChessTrait)(using val fileapi: ApiFileTrait) extends Observable with ControllerTrait {
    var activeSquare : Option[Int] = None
    var current_theme: Int = 0
    
    def boardToString() : String = {gameMode.getBoardString(gameMode.fenToBoard(fen))}

    def createOutput() : String = {output}

    def resetBoard(): Unit = {
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        fileapi.printTo(context, fen)
        this.notifyObservers
    }

    def play(move : (Int, Int)) : Unit = {
        val legalMoves = gameMode.getAllLegalMoves(fen);
        if (!legalMoves.contains(move)) {
            output = "Das kannste nicht machen Bro (kein legaler Zug)"
        } else {
            UndoInvoker.doStep(new SetCommand(gameMode.makeMove(fen, move), fen, this))
            if (gameMode.canPromote(fen) != -1) {
                ringObservers
            }
            output = boardToString()
        }
        checkGameState(gameMode.getAllLegalMoves(fen))
        fileapi.printTo(context, fen)
        notifyObservers
    }

    def checkGameState(legalMoves: List[(Int, Int)]): Boolean = {
        val event: Event = Event(legalMoves.isEmpty, fen, gameMode.isRemis(fen, legalMoves))
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
    }

    def promotePawn(pieceKind : String) : Unit = {
        fen = gameMode.promote(pieceKind, fen, gameMode.canPromote(fen));
    }

    def undo(): Unit = {
        UndoInvoker.undoStep()
        output = boardToString()
        notifyObservers
    }

    def redo() : Unit = {
        UndoInvoker.redoStep()
        output = boardToString()
        notifyObservers
    }

    def squareClicked(clickedSquare: Int) : Unit = {
        if(gameMode.isColorPiece(fen, clickedSquare)) {
            activeSquare = Some(clickedSquare)
        } else if (!gameMode.isColorPiece(fen, clickedSquare) && activeSquare != None) {
            val square = activeSquare match {
                case Some(a) => a
                case None => -5
            }
            play(gameMode.translateCastle(gameMode.fenToBoard(fen), (square, clickedSquare)))
            activeSquare = None
        }
    }

    def nextTheme(): Unit = {
        current_theme = (current_theme + 1) % 19
        notifyObservers
    }
}

