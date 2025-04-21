package TUI

import Controller.ControllerTrait
import SharedResources.util.Observer

import SharedResources.GenericHttpClient.ec

import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class Tui(controller: ControllerTrait) extends Observer {
    var readMode = "move"
    controller.add(this)

    def processInputLine(input: String):Unit = {
        if(readMode == "move") {
            input match {
                case "undo" => controller.undo()
                case "redo" => controller.redo()
                case "reset" => controller.resetBoard()
                case move if move.matches("(([a-h][1-8][a-h][1-8])|undo|redo)") =>
                    controller.translateMoveStringToInt(controller.fen, move).onComplete {
                        case Success(result) =>
                            controller.play(result)
                        case Failure(err) =>
                            println("Error: " + err.getMessage)
                    }
                    
                case _ => println("Denk nochmal nach Bro")
            }
        } else {
            if(input.matches("^(Q|R|B|N|q|r|b|n)$")) {
                controller.promotePawn(input)
                update
            } else {
                println("Alter, da steht sogar, was du eingeben kannst!!!")
            }
        }
    }

    override def update: Unit =  {
        println(controller.createOutput())
        println("Bitte gib einen Zug ein: (Format z.B. von a1 nach c3 = a1c3)")
    }

    override def specialCase: Unit = {
        readMode = "promotion"
        println(controller.createOutput())
    }

    override def reverseSpecialCase : Unit = {
        readMode = "move"
    }

    override def errorDisplay : Unit = {
        println("Error: " + controller.getErrorMessage)
    }
}
