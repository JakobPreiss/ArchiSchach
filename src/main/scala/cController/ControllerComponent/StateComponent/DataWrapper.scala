package cController.ControllerComponent.StateComponent

import play.api.libs.json.*

import scala.xml.Node

case class DataWrapper(node : Option[scala.xml.Node], json : Option[JsValue]) {
    def getNode() = {
        node match {
            case Some(value) => value
            case None => defaultNode()
        }
    }

    private def defaultNode() = {
        <box><fen>rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1</fen><state>0</state></box>
    }

    def getJson() = {
        json match {
            case Some(jsvalue) => jsvalue
            case None => Json.toJson(0)
        }
    }
}