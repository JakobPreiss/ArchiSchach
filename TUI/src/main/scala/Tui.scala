package TUI

import SharedResources.util.Observer
import SharedResources.GenericHttpClient.ec
import SharedResources.{GenericHttpClient, JsonResult}
import SharedResources.JsonResult.tuple2Format
import SharedResources.ChessJsonProtocol.tuple2Format
import SharedResources.GenericHttpClient.tuple2Format
import SharedResources.PieceJsonProtocol.tuple2Format
import SharedResources.GenericHttpClient.UnitJsonFormat
import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.Requests.{PlayRequest, PromotePawnRequest}

import scala.concurrent.Future
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}

class Tui extends Observer {
    var readMode = "move"

    def processInputLine(input: String):Unit = {
        if(readMode == "move") {
            input match {
                case "undo" =>
                    val resetBoard: Future[JsonResult[String]] = GenericHttpClient.post[Unit, JsonResult[String]](
                        baseUrl = "http://controller:8080",
                        route = "/controller/undo",
                        payload = {}
                    )
                    resetBoard.onComplete {
                        case Success(newFen: JsonResult[String]) =>
                        case Failure(err) =>
                    }
                case "redo" =>
                    val makeMove: Future[JsonResult[String]] = GenericHttpClient.post[Unit, JsonResult[String]](
                        baseUrl = "http://controller:8080",
                        route = "/controller/redo",
                        payload = {}
                    )
                    makeMove.onComplete {
                        case Success(newFen: JsonResult[String]) =>
                        case Failure(err) =>
                    }
                case "reset" =>
                    val resetBoard: Future[JsonResult[String]] = GenericHttpClient.post[Unit, JsonResult[String]](
                        baseUrl = "http://controller:8080",
                        route = "/controller/resetBoard",
                        payload = {}
                    )
                    resetBoard.onComplete {
                        case Success(newFen: JsonResult[String]) =>
                        case Failure(err) =>
                    }
                case move if move.matches("(([a-h][1-8][a-h][1-8])|undo|redo)") =>
                    val fenFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
                        baseUrl = "http://controller:8080",
                        route = "/controller/fen",
                        queryParams = Map()
                    )
                    fenFuture.onComplete {
                        case Success(fen) =>
                            val translateFuture: Future[JsonResult[(Int, Int)]] = GenericHttpClient.get[JsonResult[(Int, Int)]](
                                baseUrl = "http://controller:8080",
                                route = "/controller/translateMoveStringToInt",
                                queryParams = Map(
                                    "fen" -> fen.result,
                                    "move" -> move
                                )
                            )
                            translateFuture.onComplete {
                                case Success(result) =>
                                    val payload = PlayRequest(
                                        move = result.result
                                    )
                                    
                                    val makeMove: Future[Unit] = GenericHttpClient.post[PlayRequest, Unit](
                                        baseUrl = "http://controller:8080",
                                        route = "/controller/squareClicked",
                                        payload = payload
                                    )
                                    makeMove.onComplete {
                                        case Success(result) =>
                                        case Failure(err) =>
                                            println("Error: " + err.getMessage)
                                    }

                                case Failure(err) =>
                                    println("Error: " + err.getMessage)
                            }
                        case Failure(err) =>
                            println("Error: " + err.getMessage)
                    }
                case _ => println("Denk nochmal nach Bro")
            }
        } else {
            if(input.matches("^(Q|R|B|N|q|r|b|n)$")) {
                val payload = PromotePawnRequest(
                    pieceKind = input
                )

                val boardFuture: Future[JsonResult[Int]] = GenericHttpClient.post[Unit, JsonResult[Int]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/promotePawn",
                    payload = payload
                )
                boardFuture.onComplete {
                    case Success(value) =>
                        update
                    case Failure(err) =>

                }

            } else {
                println("Alter, da steht sogar, was du eingeben kannst!!!")
            }
        }
    }

    override def update: Unit =  {
        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://controller:8080",
            route = "/controller/createOutput",
            queryParams = Map()
        )
        boardFuture.onComplete {
            case Success(value) =>
                println(value.result)
            case Failure(err) =>
                println("Error: Could not get error")
        }
        println("Bitte gib einen Zug ein: (Format z.B. von a1 nach c3 = a1c3)")
    }

    override def specialCase: Unit = {
        readMode = "promotion"
        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://controller:8080",
            route = "/controller/createOutput",
            queryParams = Map()
        )
        boardFuture.onComplete {
            case Success(value) =>
                println(value.result)
            case Failure(err) =>
                println("Error: Could not get error")
        }
    }

    override def reverseSpecialCase : Unit = {
        readMode = "move"
    }

    override def errorDisplay : Unit = {
        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://controller:8080",
            route = "/controller/errorMessage",
            queryParams = Map()
        )
        boardFuture.onComplete {
            case Success(value) =>
                println("Error: " + value.result)
            case Failure(err) =>
                println("Error: Could not get error")
        }
    }
}