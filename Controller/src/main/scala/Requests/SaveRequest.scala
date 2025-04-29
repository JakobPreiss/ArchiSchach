package Controller.Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class SaveRequest(fen: String)

object SaveRequest extends DefaultJsonProtocol {
  implicit val saveRequestFormat: RootJsonFormat[SaveRequest] = jsonFormat1(SaveRequest)
}