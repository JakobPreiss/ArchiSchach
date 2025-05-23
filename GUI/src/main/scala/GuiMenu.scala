package GUI

import SharedResources.{ChessContext, ChessTrait, GenericHttpClient, JsonResult, State}
import javafx.stage.Screen
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.effect.DropShadow
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{Background, BackgroundFill, CornerRadii, GridPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import SharedResources.util.Observer
import SharedResources.GenericHttpClient.UnitJsonFormat
import SharedResources.GenericHttpClient.StringJsonFormat
import SharedResources.GenericHttpClient.ec
import SharedResources.ChessJsonProtocol.chessContextFormat
import scalafx.application.Platform

import scala.concurrent.Future
import scala.util.{Failure, Success}

class GuiMenu extends VBox, Observer {
    override def update: Unit = {
        val boardFuture: Future[JsonResult[ChessContext]] =
            GenericHttpClient.get[JsonResult[ChessContext]](
                baseUrl     = "http://controller:8080",
                route       = "/controller/context",
                queryParams = Map()
            )

        boardFuture.onComplete {
            case Success(value) =>
                // pick the right children sequence, *off* the FX thread
                val newKids = value.result.state match {
                    case State.Remis => {
                        val infoLabel = new Label("Remis") {
                            style = "-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-color: #F5F5DC;"
                            wrapText = true
                        }
                        Seq(themeButton, undoButton, redoButton, resetButton, infoLabel)
                    }
                    case State.WhiteWon => {
                        val infoLabel = new Label("Schwarz wurde vernichtend geschlagen") {
                            style = "-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-color: #F5F5DC;"
                            wrapText = true
                        }
                        Seq(themeButton, undoButton, redoButton, resetButton, infoLabel)
                    }
                    case State.BlackWon => {
                        val infoLabel = new Label("Weiß wurde vernichtend geschlagen") {
                            style = "-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-color: #F5F5DC;"
                            wrapText = true
                        }
                        Seq(themeButton, undoButton, redoButton, resetButton, infoLabel)
                    }
                    case _ => Seq(themeButton, undoButton, redoButton, resetButton)
                }

                // now *on* the FX thread, swap them in
                Platform.runLater {
                    children = newKids
                }

            case Failure(err) =>
                // on error, just show the buttons
                Platform.runLater {
                    children = Seq(themeButton, undoButton, redoButton, resetButton)
                }
        }
    }

    override def specialCase: Unit = ()
    override def reverseSpecialCase: Unit = ()
    override def errorDisplay: Unit = {}

    val screenBounds = Screen.getPrimary.getVisualBounds
    val vw = screenBounds.getWidth
    val vh = screenBounds.getHeight

    val themeButton = new Button("Theme") {
        prefWidth = vh * 0.1
        prefHeight = vh * 0.05
        style = "-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-color: #F5F5DC;"
        // Custom font and text color
        background = new Background(
            Array(new BackgroundFill(Color.White, new CornerRadii(10), Insets.Empty)) // White background with rounded corners
        )
        effect = new DropShadow { // Add a subtle shadow
            color = Color.Gray
            radius = 5
            spread = 0.2
        }
        onAction = _ =>
            val makeMove: Future[JsonResult[String]] = GenericHttpClient.post[Unit, JsonResult[String]](
                baseUrl = "http://controller:8080",
                route = "/controller/nextTheme",
                payload = {}
            )
            makeMove.onComplete {
                case Success(newFen: JsonResult[String]) =>
                case Failure(err) =>
            }
    }

    val undoButton = new Button("Undo") {
        prefWidth = vh * 0.1
        prefHeight = vh * 0.05
        style = "-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-color: #F5F5DC;"
        // Custom font and text color
        background = new Background(
            Array(new BackgroundFill(Color.White, new CornerRadii(10), Insets.Empty)) // White background with rounded corners
        )
        effect = new DropShadow { // Add a subtle shadow
            color = Color.Gray
            radius = 5
            spread = 0.2
        }
        onAction = _ =>
            val makeMove: Future[JsonResult[String]] = GenericHttpClient.post[Unit, JsonResult[String]](
                baseUrl = "http://controller:8080",
                route = "/controller/undo",
                payload = {}
            )
            makeMove.onComplete {
                case Success(newFen: JsonResult[String]) =>
                case Failure(err) =>
            }

    }

    val redoButton = new Button("Redo") {
        prefWidth = vh * 0.1
        prefHeight = vh * 0.05
        style = "-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-color: #F5F5DC;"
        // Custom font and text color
        background = new Background(
            Array(new BackgroundFill(Color.White, new CornerRadii(10), Insets.Empty)) // White background with rounded corners
        )
        effect = new DropShadow { // Add a subtle shadow
            color = Color.Gray
            radius = 5
            spread = 0.2
        }
        onAction = _ =>
            val makeMove: Future[JsonResult[String]] = GenericHttpClient.post[Unit, JsonResult[String]](
                baseUrl = "http://controller:8080",
                route = "/controller/redo",
                payload = {}
            )
            makeMove.onComplete {
                case Success(newFen: JsonResult[String]) =>
                case Failure(err) =>
            }
    }

    val resetButton = new Button("Reset") {
        prefWidth = vh * 0.1
        prefHeight = vh * 0.05
        style = "-fx-font-size: 16px; -fx-font-family: 'Roboto'; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-color: #F5F5DC;"
        // Custom font and text color
        background = new Background(
            Array(new BackgroundFill(Color.White, new CornerRadii(10), Insets.Empty)) // White background with rounded corners
        )
        effect = new DropShadow { // Add a subtle shadow
            color = Color.Gray
            radius = 5
            spread = 0.2
        }
        onAction = _ => {
            val resetBoard: Future[JsonResult[String]] = GenericHttpClient.post[Unit, JsonResult[String]](
                baseUrl = "http://controller:8080",
                route = "/controller/resetBoard",
                payload = {}
            )
            resetBoard.onComplete {
                case Success(newFen: JsonResult[String]) =>
                case Failure(err) =>
            }
        }
    }

    children = Seq(themeButton, undoButton, redoButton, resetButton)

    alignment = Pos.Center
    spacing = vh * 0.05


    this.setPrefSize(vh * 0.3, vh)
    val marginScreenHeight = vh * 0.05
    VBox.setMargin(this, Insets(marginScreenHeight, marginScreenHeight * 3, marginScreenHeight, marginScreenHeight * 3))
    style = "-fx-background-color: #A9A9A9;"

}
