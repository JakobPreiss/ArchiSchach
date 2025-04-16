package Controller

import SharedResources.ChessContext
import SharedResources.util.{Observable, Observer}

import scala.util.Try

trait ControllerTrait extends Observable {
    /**
     * variable String called fen representing the board state
     * @return fen
     */
    def fen: String

    /**
     * for changing the fen variable
     * @param value new fen
     */
    def fen_=(value: String): Unit

    /**
     * resets the board to the beginning state of every game and resets it in json or xml as well
     */
    def resetBoard(): Unit

    /**
     * variable context with a saved Game State
     * @return ChessContext Object
     */
    def context: ChessContext

    /**
     * for changing the context variable
     * @param value new context value
     */
    def context_=(value: ChessContext): Unit

    /**
     * variable current Theme of the Gui as Int
     * @return current Gui Theme as Int
     */
    def current_theme: Int

    /**
     * for changing the current theme (is usually done in a seperate method)
     * @param value
     */
    def current_theme_=(value: Int): Unit

    /**
     * checks if a move is playable and if so changes the Game state (fen and Chesscontext) accordingly
     * @param move Index Tupel from Square to Square
     */
    def play(move : Try[(Int, Int)]) : Unit

    /**
     * undo the last played move
     */
    def undo() : Unit

    /**
     * redo the last undid move
     */
    def redo() : Unit

    /**
     * createOutput returns the String output of the Tui
     * @return String output of the Tui
     */
    def createOutput() : Try[String]

    /**
     * promotePawn gets a pieceKind represented with one Letter from the Tui and changes the Board state accordingly (making the promotion)
     * @param pieceKind pieceKind represented with one Letter
     */
    def promotePawn(pieceKind : String) : Unit

    /**
     * squareClicked gets the Index of a Square from the Gui. If two clicked squares make a move (the last is saved) it plays that move
     * @param clickedSquare
     */
    def squareClicked(clickedSquare: Try[Int]) : Unit

    /**
     * nextTheme changes the current theme of the Gui
     */
    def nextTheme(): Unit

    /**
     * add is needed in the constuctor of the Tui and Gui so that the controller can notify them about changes
     * @param s Tui or Gui (Observer)
     */
    def add(s: Observer): Unit

    /**
     * get ErrorMessage returns the error message to the UI
     * @return ErrorMessage from failure
     */
    def getErrorMessage : Try[String]

    def translateMoveStringToInt (fen :String, move : String) : Try[(Int, Int)]
}


