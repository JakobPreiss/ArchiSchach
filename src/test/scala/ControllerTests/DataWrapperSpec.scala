package ControllerTests

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.xml.Node
import play.api.libs.json.*

class DataWrapperSpec extends AnyWordSpec with Matchers {
    "DataWrapperSpec" should {
        "return a xml if a xml is saved" in {
            val xmlNode = new Node:
                override def label: String = "testNode"

                override def child: collection.Seq[Node] = ???
            val testxml = DataWrapper(Some(xmlNode), None)
            testxml.getNode().label should be ("testNode")

            val testxml2 = DataWrapper(None, None)
            testxml2.getNode().label should be ("box")
        }

        "return a json Value if a json is saved" in {

            val jsonValue = Json.toJson(1)
            val testjson = DataWrapper(None, Some(jsonValue))
            testjson.getJson().toString should be (jsonValue.toString)

            val testjson2 = DataWrapper(None, None)
            testjson2.getJson().toString should be (Json.toJson(0).toString)
        }
    }
}


