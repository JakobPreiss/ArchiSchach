package ControllerTests

import Controller.ControllerTrait
import Controller.Extra.{Command, SetCommand, UndoInvoker}
import scala.util.Try

class ControllerStub(override var fen : String) extends ControllerTrait {
    val invoker = UndoInvoker
    override var current_theme: Int = 0

    def play(move: scala. util. Try[(Int, Int)]): Unit = {invoker.doStep(new SetCommand(move.get.productIterator.mkString("-"), fen, this))}

    def undo() : Unit = {invoker.undoStep()}

    def redo() : Unit = {invoker.redoStep()}

    def boardToString(): String = {fen}

    def createOutput(): String = {fen}

    def promotePawn(pieceKind: String): Unit = {}

    def squareClicked(clickedSquare: scala. util. Try[Int]): Unit = {}

    def nextTheme(): Unit = {}

    def context: Controller. Extra. ChessContext = ???
    
    def context_=(value: Controller. Extra. ChessContext): Unit = ???

    override def resetBoard(): Unit = ???

    def getErrorMessage: String = ???

    def translateMoveStringToInt (fen: String, move: String): Try[(Int, Int)] = ???
}
