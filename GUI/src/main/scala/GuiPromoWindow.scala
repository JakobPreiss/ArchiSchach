package GUI

import SharedResources.{ChessContext, GenericHttpClient, JsonResult, State}
import javafx.stage.Screen
import scalafx.scene.control.Button
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.BackgroundPosition.Center
import scalafx.scene.layout.{StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.text.Text
import SharedResources.util.Observer
import SharedResources.GenericHttpClient.ec
import SharedResources.GenericHttpClient.IntJsonFormat
import SharedResources.GenericHttpClient.UnitJsonFormat
import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.ChessJsonProtocol.chessContextFormat
import SharedResources.Requests.PromotePawnRequest

import scala.util.{Failure, Success, Try}
import scala.annotation.tailrec
import scala.concurrent.Future

class GuiPromoWindow extends VBox, Observer {
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

    override def reverseSpecialCase: Unit = {
        children = Seq()
    }
    override def specialCase: Unit = {
        showPieces()
    }

    def currentTheme: Int = {
        val boardFuture: Future[JsonResult[Int]] = GenericHttpClient.get[JsonResult[Int]](
            baseUrl = "http://controller:8080",
            route = "/controller/currentTheme",
            queryParams = Map()
        )
        boardFuture.onComplete {
            case Success(value) =>
                return value.result
            case Failure(err) =>
                println("Error fetching current theme: ")
                return 0
        }

        0
    }

    def promotePawn(pieceKind: String): Unit = {
        val payload = PromotePawnRequest(
            pieceKind = pieceKind
        )

        val boardFuture: Future[JsonResult[Int]] = GenericHttpClient.post[Unit, JsonResult[Int]](
            baseUrl = "http://controller:8080",
            route = "/controller/promotePawn",
            payload = payload
        )
        boardFuture.onComplete {
            case Success(value) =>
                println("Pawn promoted to: " + value.result)
            case Failure(err) =>
                println("Error promoting pawn: " + err.getMessage)
        }
    }

    override def update: Unit = ()
    override def errorDisplay: Unit = {
        val boardFuture: Future[JsonResult[String]] = GenericHttpClient.get[JsonResult[String]](
            baseUrl = "http://controller:8080",
            route = "/controller/errorMessage",
            queryParams = Map()
        )
        boardFuture.onComplete {
            case Success(value) =>
                val errMsg = new Text(value.result)
                children = Seq(errMsg)
            case Failure(err) =>
                val errMsg = new Text("Could not read error message")
                children = Seq(errMsg)
        }
    }

    def showPieces(): Unit = {
        val boardFuture: Future[JsonResult[ChessContext]] = GenericHttpClient.get[JsonResult[ChessContext]](
            baseUrl = "http://controller:8080",
            route = "/controller/context",
            queryParams = Map()
        )
        val pathsFuture: Future[List[String]] = boardFuture.map { value =>
            value.result.state match {
                case State.BlackPlaying => List(
                    "/pieces/black-rook.png",
                    "/pieces/black-knight.png",
                    "/pieces/black-bishop.png",
                    "/pieces/black-queen.png"
                )
                case State.WhitePlaying => List(
                    "/pieces/white-rook.png",
                    "/pieces/white-knight.png",
                    "/pieces/white-bishop.png",
                    "/pieces/white-queen.png"
                )
                case _ => List()
            }
        }

        def getImage(path: String) : ImageView = {
            new ImageView { //padding = Insets(vh * 0.2, 0, 0, 0)
                image = new Image(path)
                fitWidth = varHeight * 0.07
                preserveRatio = true
                //alignmentInParent = Center
                val lcol3 = color_pallets(currentTheme)._1
                style = s"-fx-effect: dropshadow(gaussian, $lcol3, 10, 0.8, 0, 0);"
                effect = new DropShadow {
                    color = Color.Black
                    radius = 10
                    spread = 0.2
                }
            }
        }

        def getButton(pieceKind: String) : Button = {
             val button = new Button() {
                style = "-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0;"
                onAction = _ => promotePawn(pieceKind)
                //focusWithin.apply()
            }
                button.setPrefSize(varHeight * 0.1, varHeight * 0.1)
                return button
        }

        def getPieceList(pathList: List[String]) : List[StackPane] = {
            val imageList = pathList.map(getImage: String => ImageView)
            val buttonInputList = List("r", "n", "b", "q")
            val buttonList = buttonInputList.map(getButton: String => Button)
            
            @tailrec
            def buildStackPaneRecursive(imgList: List[ImageView], bList: List[Button], accumulator : List[StackPane]) : List[StackPane] = {
                (imgList, bList) match {
                    case (Nil, _) | (_, Nil) => accumulator
                    case(hI :: tI, hB :: tB) =>
                        val stackPane = new StackPane() {
                            children = List(hI, hB) //richtige reihenfolge?
                        }
                        buildStackPaneRecursive(tI, tB, accumulator.appended(stackPane))
                }
            }
            buildStackPaneRecursive(imageList, buttonList, List())
        }

        pathsFuture.onComplete {
            case Success(paths) =>
                val pieceList = getPieceList(paths)
                children = pieceList
        }
    }

    alignment = Pos.Center
    spacing = varHeight * 0.1

    this.setPrefSize(varWidth * 0.25, varHeight)
    val marginScreenHeight = varHeight * 0.15
    VBox.setMargin(this, Insets(marginScreenHeight, marginScreenHeight * 3, marginScreenHeight, marginScreenHeight * 3))
    style = "-fx-background-color: #A9A9A9;"
}
