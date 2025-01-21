package cController.ControllerComponent.StateComponent

import play.api.libs.json.*

import scala.xml.Node

case class DataWrapper(node : Option[scala.xml.Node], json : Option[JsValue]) {
    def getNode() = {
        node match {
            case Some(value) => value
            case None => new Node:
                override def label: String = "fail"

                override def child: collection.Seq[Node] = ???
        }
    }

    def getJson() = {
        json match {
            case Some(jsvalue) => jsvalue
            case None => Json.toJson(0)
        }
    }
}