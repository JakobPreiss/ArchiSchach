package GUI

import SharedResources.{ChessTrait, GenericHttpClient, JsonResult}
import javafx.stage.Screen
import scalafx.application.{JFXApp3, Platform}
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.geometry.Pos.Center
import scalafx.scene.control.{Button, Label}
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.Priority.Always
import scalafx.scene.layout.*
import scalafx.scene.paint.*
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{Font, Text}
import scalafx.scene.{Node, Scene}
import scalafx.stage.Stage
import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.GenericHttpClient.ec

import java.nio.file.Paths
import java.nio.file.Paths.*
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class GuiBoard extends GridPane {
    def update: Unit = {
        updateGrid().recover { case ex ⇒
            println(s"Board update failed: ${ex.getMessage}")
        }
    }
    def specialCase: Unit = ()
    def reverseSpecialCase: Unit = {
        update
    }

    def errorDisplay: Unit = {}

    val screenBounds = Screen.getPrimary.getVisualBounds
    val varWidth = screenBounds.getWidth
    val varHeight = screenBounds.getHeight

    val color_pallets: Vector[(String, String, String)] = Vector(
        ("#cb9df0", "#F0C1E1", "#FFF9BF"),
        ("#9AA6B2", "#BCCCDC", "#D9EAFD"),
        ("#493628", "#AB886D", "#D6C0B3"),
        ("#FFB0B0", "#FFD09B", "#FFECC8"),
        ("#7E60BF", "#E4B1F0", "#FFE1FF"),
        ("#921A40", "#C75B7A", "#F4D9D0"),
        ("#508D4E", "#80AF81", "#D6EFD8"),
        ("#F19ED2", "#E8C5E5", "#F7F9F2"),
        ("#55AD9B", "#95D2B3", "#D8EFD3"),
        ("#FF7D29", "#FFBF78", "#FFEEA9"),
        ("#A7D477", "#F72C5B", "#FF748B"),
        ("#481E14", "#9B3922", "#F2613F"),
        ("#D20062", "#D6589F", "#D895DA"),
        ("#41C9E2", "#ACE2E1", "#F7EEDD"),
        ("#31363F", "#76ABAE", "#EEEEEE"),
        ("#503C3C", "#7E6363", "#A87C7C"),
        ("#424769", "#7077A1", "#F6B17A"),
        ("#DC84F3", "#E9A8F2", "#F3CCF3"),
        ("#5C5470", "#B9B4C7", "#FAF0E6")
    )

    update

    def currentTheme(): Future[Int] = {
        GenericHttpClient.get[JsonResult[Int]](
            baseUrl = "http://controller:8080",
            route = "/controller/currentTheme",
            queryParams = Map()
        ).map(_.result).recover {
            case e => println(s"Error: ${e.getMessage}"); 0
        }
    }

    def currentFen(): Future[String] = {
        GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://controller:8080",
            route = "/controller/fen",
            queryParams = Map()
        ).map(_.result).recover {
            case e => println(s"Error: ${e.getMessage}"); ""
        }
    }

    def squareClicked(move: Try[Int]): Unit = {
         move match {
            case Success(move) =>
                val payload = SquareClickedRequest(
                    square = move
                )

                val makeMove: Future[JsonResult[String]] = GenericHttpClient.post[SquareClickedRequest, JsonResult[String]](
                    baseUrl = "http://controller:8080",
                    route = "/controller/squareClicked",
                    payload = payload
                )
                makeMove.onComplete {
                    case Success(newFen: JsonResult[String]) =>
                    case Failure(err) => println("Error: (http://controller:8080/controller/squareClicked)" + err.getMessage)
                }
            case Failure(err) => println("Error: " + err.getMessage)
        }
    }


    private val pieceMap = Map(
        "p" -> "black-pawn", "r" -> "black-rook", "n" -> "black-knight",
        "b" -> "black-bishop", "q" -> "black-queen", "k" -> "black-king",
        "P" -> "white-pawn", "R" -> "white-rook", "N" -> "white-knight",
        "B" -> "white-bishop", "Q" -> "white-queen", "K" -> "white-king"
    )

    def updateGrid(): Future[Unit] = {
        // kick off both requests in parallel
        val themeF = currentTheme()
        val fenF = currentFen()

        // when both arrive, build Nodes and push to UI thread
        for {
            themeIdx ← themeF
            fenStr ← fenF
        } yield {

            // 1) Prepare a list of (Node, row, col) off the FX thread:
            val squares: Seq[(Node, Int, Int)] = {
                val flat = fenToList(fenStr).reverse.zipWithIndex
                val palette = color_pallets(themeIdx)
                flat.map { case (piece, idx) =>
                    val row = idx / 8
                    val col = idx % 8

                    // background rectangle
                    val bg = new Rectangle {
                        val colorHex =
                            if ((idx + (idx / 8)) % 2 == 0) palette._3
                            else palette._2
                        fill = Paint.valueOf(colorHex)
                        width = varHeight * 0.1
                        height = varHeight * 0.1
                        arcWidth = varHeight * 0.02
                        arcHeight = varHeight * 0.02
                    }

                    // transparent click‐target
                    val button1: Button = new Button() {
                        style = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;"
                        onAction = _ => squareClicked(Success(63 - idx))
                        focusWithin.apply()

                    }
                    button1.setPrefSize(varHeight * 0.1, varHeight * 0.1)

                    // assemble
                    val cellChildren =
                        if (piece == ".") Seq(bg, button1)
                        else {
                            val img = new ImageView(new Image(s"/pieces/${pieceMap(piece)}.png")) {
                                fitWidth = varHeight * 0.07
                                preserveRatio = true
                                effect = new DropShadow {
                                    color = Color.Black
                                    radius = 10
                                    spread = 0.2
                                }
                                style = s"-fx-effect: dropshadow(gaussian, ${palette._1}, 10, 0.8, 0, 0);"
                            }
                            Seq(bg, img, button1)
                        }

                    val stack = new StackPane {
                        children = cellChildren
                    }
                    (stack, row, col)
                }
            }

            // 2) Now actually mutate the GridPane on the FX thread:
            Platform.runLater {
                this.children.clear()
                squares.foreach { case (node, r, c) =>
                    this.add(node, c, r)
                }
                // set overall background
                this.style = s"-fx-background-color:${color_pallets(themeIdx)._1}"
            }
        }
    }

    
    def fenToList(fen : String) : List[String] = {
        
        def checkChars(charList: List[String], accumulator : List[String]) : List[String] = {
            charList match {
                case Nil => accumulator
                case h :: t => h match {
                    case "/" => checkChars(t, accumulator)
                    case h if(h.matches("(Q|R|P|B|N|K|k|q|r|p|n|b)")) => checkChars(t, accumulator :+ h)
                    case h if(h.matches("(1|2|3|4|5|6|7|8)")) =>
                        val tempInt = h.toInt
                        checkChars(t, accumulator ++ List.fill(tempInt)("."))
                    case _ => List()
                }
            }
        } 
        val chars : List[String] = fen.split(" ")(0).map(_.toString).toList 
        checkChars(chars, List())
    }

    //gridBoard.setPrefSize(screenBounds.getHeight, screenBounds.getHeight)
    for (row <- 0 until 8)
        {
            val rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Always); // allow row to grow
            rowConstraints.setFillHeight(true); // ask nodes to fill height for row
            // other settings as needed...
            this.getRowConstraints().add(rowConstraints);
        }
        for (col <- 0 until 8) {
            val columnConstraints = new ColumnConstraints();
            columnConstraints.setHgrow(Always); // allow column to grow
            columnConstraints.setFillWidth(true); // ask nodes to fill space for column
            // other settings as needed...
            this.getColumnConstraints().add(columnConstraints)
        }
        this.setPrefSize(varHeight * 0.9, varHeight)
        val marginScreenHeight = varHeight * 0.05
        BorderPane.setMargin(this, Insets(marginScreenHeight, marginScreenHeight * 3, marginScreenHeight, marginScreenHeight * 3))
}
