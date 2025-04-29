package GUI

import GUI.{GuiBoard, GuiMenu}
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label}
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.*
import scalafx.scene.paint.*
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Text
import scalafx.scene.{Node, Scene}
import scalafx.stage.Stage
import SharedResources.util.Observer

object GuiMain extends JFXApp3 {

    var board : Option[GuiBoard] = None
    var menu : Option[GuiMenu] = None
    var promoWindow : Option[GuiPromoWindow] = None
    
    def start(): Unit = {

        stage = new JFXApp3.PrimaryStage {
            
            title = "JP Morgan chess"
            scene = new Scene() {

                root = new BorderPane {

                    //padding
                    style = "-fx-background-color:BLACK"
                    board match {
                        case None =>
                        case Some(b) =>
                            promoWindow match {
                                case None =>
                                case Some(w) =>
                                    menu match {
                                        case None =>
                                        case Some(m) =>
                                            left = b
                                            center = w
                                            right = m
                                    }
                            }
                    }
                    style = "-fx-background-color: #F5F5DC;"
                    
                }
            }
            fullScreen = true
        }
    }

    def setComponents(board: GuiBoard, menu: GuiMenu, promoWindow: GuiPromoWindow): Unit = {
        this.board = Some(board)
        this.menu = Some(menu)
        this.promoWindow = Some(promoWindow)
    }
}