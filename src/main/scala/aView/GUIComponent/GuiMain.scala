package aView.GUIComponent

import aView.GUIComponent.{GuiBoard, GuiMenu}
import cController.ControllerComponent.ControllerTrait
import cController.ControllerComponent.RealChessController.Controller
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
import util.Observer

object GuiMain extends JFXApp3, Observer {

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

    def update: Unit = {}

    def specialCase: Unit = {}

    def reverseSpecialCase: Unit = {}

    def errorDisplay: Unit = {
        val controllerReal : ControllerTrait = controller match {
            case Some(contr) => contr
            case None => null
        }
        val errorStage = new Stage {
            title = "Error Message"
        }

        errorStage.scene = new Scene {
            root = new VBox {
                alignment = Pos.Center
                spacing = 10
                padding = Insets(20)
                children = Seq(
                    new Label(controllerReal.getErrorMessage),
                    new Button("Close") {
                        onAction = _ => errorStage.close()
                    }
                )
            }
        }
        errorStage.show()
    }
    
}