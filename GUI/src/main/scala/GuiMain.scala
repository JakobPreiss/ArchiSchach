package GUI

import GUI.{GuiBoard, GuiMenu}
import Controller.ControllerTrait
import Controller.DuoChessController.RealController
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

    var controller : Option[ControllerTrait] = None

    
    
    def start(): Unit = {

        stage = new JFXApp3.PrimaryStage {
            
            title = "JP Morgan chess"
            scene = new Scene() {

                root = new BorderPane {

                    //padding
                    style = "-fx-background-color:BLACK"
                    left  = new GuiBoard(controller)
                    center = new GuiPromoWindow(controller)
                    right = new GuiMenu(controller)
                    style = "-fx-background-color: #F5F5DC;"
                    
                }
            }
            fullScreen = true
        }
    }

    def setController(controller: ControllerTrait): Unit = {
        this.controller = Some(controller)
    }
}