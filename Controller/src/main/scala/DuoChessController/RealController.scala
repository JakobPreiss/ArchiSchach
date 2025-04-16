package Controller.DuoChessController

import BasicChess.StandartChess.BasicChessFacade
import SharedResources.{ApiFileTrait, ChessContext, ChessTrait, Event, State}
import Controller.ControllerTrait
import Controller.Extra.{SetCommand, UndoInvoker}
import com.google.inject.Inject
import SharedResources.util.Observable

import scala.util.{Failure, Success, Try}

class RealController(override var fen : String, var context : ChessContext, var output : String)(using val gameMode : ChessTrait)(using val fileapi: ApiFileTrait) extends Observable with ControllerTrait {
    var activeSquare : Option[Int] = None
    var current_theme: Int = 0
    var errorMessage : String = ""
    
    def boardToString() : String = {
        BasicChessFacade.getBoardString(fen) match {
            case Success(value) => value
            case Failure(value) => 
                failureHandle(value.getMessage)
                ""
        }
    }

    def createOutput() : String = {output}

    def resetBoard(): Unit = {
        fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        val legalMoves = gameMode.getAllLegalMoves(fen) match {
            case Success(legalMoves) => checkGameState(legalMoves)
            case Failure(err) =>
                failureHandle(err.getMessage)
                return
        }
        fileapi.printTo(context, fen)
        notifyObservers
        deRingObservers
    }

    def play(moveRaw : Try[(Int, Int)]) : Unit = {
        val move = moveRaw match {
            case Success(value : (Int, Int)) => value
            case Failure(err) => failureHandle(err.getMessage)
                return
        }
        def tryMove(move : (Int, Int), legalMoves : List[(Int, Int)]) : Unit = {
            if (!legalMoves.contains(move)) {
                output = "Das kannste nicht machen Bro (kein legaler Zug)"
                checkGameState(legalMoves)
                notifyObservers
            } else {
                BasicChessFacade.makeMove(fen, move) match {
                    case Success(newFen) => UndoInvoker.doStep(new SetCommand(newFen, fen, this))
                        checkPromotion()
                    case Failure(err) => failureHandle(err.getMessage)
                }
            }       
        }
        
        def checkPromotion(): Unit = {
            val canPromote = BasicChessFacade.canPromote(fen) match {
                case Success(value) => value
                case Failure(err) =>
                    failureHandle(err.getMessage)
                    return
            }
            if (canPromote.isDefined) {
                output = "Welche Beförderung soll der Bauer erhalten? (Eingabemöglichkeiten: Q,q,N,n,B,b,R,r)"
                ringObservers
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
                            case Failure(err) =>
                                failureHandle(err.getLocalizedMessage)
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
                    case Success(value) => value
                    case Failure(err) =>
                        failureHandle(err.getMessage)
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
            case Failure(err) => failureHandle(err.getMessage)
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

    def getErrorMessage : String = {
        errorMessage
    }
    
    def translateMoveStringToInt (fen :String, move : String) : Try[(Int, Int)] = {
        BasicChessFacade.translateMoveStringToInt(fen, move)
    }
}

