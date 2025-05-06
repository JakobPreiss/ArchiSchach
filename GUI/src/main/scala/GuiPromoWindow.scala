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

import scalafx.application.Platform

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

    def currentTheme(): Future[Int] = {
        GenericHttpClient
          .get[JsonResult[Int]](
              baseUrl = "http://controller:8080",
              route = "/controller/currentTheme",
              queryParams = Map()
          )
          .map(_.result) // extract the Int
          .recover { case err =>
              println(s"[currentTheme] error fetching theme: ${err.getMessage}")
              0 // fallback
          }
    }

    /** Send the “promote pawn” request and log the result.
     * Returns a Future[Int] with the new promotion value (or 0 on error). */
    def promotePawn(pieceKind: String): Future[Int] = {
        val payload = PromotePawnRequest(pieceKind = pieceKind)
        GenericHttpClient
          .post[Unit, JsonResult[Int]](
              baseUrl = "http://controller:8080",
              route = "/controller/promotePawn",
              payload = payload
          )
          .map { resp =>
              println(s"[promotePawn] Pawn promoted to: ${resp.result}")
              resp.result
          }
          .recover { case err =>
              println(s"[promotePawn] error promoting pawn: ${err.getMessage}")
              0
          }
    }

    override def update: Unit = ()
    override def errorDisplay: Unit = {
        val boardFuture: Future[JsonResult[String]] =
            GenericHttpClient.get[JsonResult[String]](
                baseUrl    = "http://controller:8080",
                route      = "/controller/errorMessage",
                queryParams= Map()
            )

        boardFuture.onComplete {
            case Success(value) =>
                // wrap the mutation in the FX thread
                Platform.runLater {
                    val errMsg = new Text(value.result)
                    children = Seq(errMsg)
                }

            case Failure(_) =>
                Platform.runLater {
                    val errMsg = new Text("Could not read error message")
                    children = Seq(errMsg)
                }
        }
    }

    def showPieces(): Unit = {
        // 1) fetch the context → list of piece‐image paths
        val pathsFuture: Future[List[String]] =
            GenericHttpClient
              .get[JsonResult[ChessContext]](
                  baseUrl     = "http://controller:8080",
                  route       = "/controller/context",
                  queryParams = Map()
              )
              .map(_.result.state match {
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
                  case _ => List.empty
              })

        // 2) fetch the current theme (you already have this as Future[Int])
        val themeFuture: Future[Int] = currentTheme()

        // 3) combine them: once we have both the paths and the theme index...
        val panesFuture: Future[List[StackPane]] = for {
            paths <- pathsFuture
            theme <- themeFuture
        } yield {
            // now build the StackPanes using the *synchronous* theme value
            val highlightColor = color_pallets(theme)._1

            def getImage(path: String): ImageView =
                new ImageView {
                    image       = new Image(path)
                    fitWidth    = varHeight * 0.07
                    preserveRatio = true
                    // use the captured highlightColor
                    style = s"-fx-effect: dropshadow(gaussian, $highlightColor, 10, 0.8, 0, 0);"
                    effect = new DropShadow {
                        color  = Color.Black
                        radius = 10
                        spread = 0.2
                    }
                }

            def getButton(kind: String): Button = {
                val b = new Button {
                    style    = "-fx-background-color: transparent;"
                    onAction = _ => promotePawn(kind)
                }
                b.prefWidth  = varHeight * 0.1
                b.prefHeight = varHeight * 0.1
                b
            }

            // zip images & buttons into StackPanes
            val kinds = List("r","n","b","q")
            paths.zip(kinds).map { case (path, kind) =>
                new StackPane {
                    children = List( getImage(path), getButton(kind) )
                }
            }
        }

        // 4) when it’s all ready, push it onto the FX thread
        panesFuture.onComplete {
            case Success(panes) =>
                Platform.runLater {
                    children = panes
                }
            case Failure(ex) =>
                Platform.runLater {
                    children = Seq(new Text(s"Couldn’t show pieces:\n${ex.getMessage}"))
                }
        }
    }

    alignment = Pos.Center
    spacing = varHeight * 0.1

    this.setPrefSize(varWidth * 0.25, varHeight)
    val marginScreenHeight = varHeight * 0.15
    VBox.setMargin(this, Insets(marginScreenHeight, marginScreenHeight * 3, marginScreenHeight, marginScreenHeight * 3))
    style = "-fx-background-color: #A9A9A9;"
}
