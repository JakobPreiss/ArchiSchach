package Controller.Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class SaveRequest(ctx: Int, fen: String)

object SaveRequest extends DefaultJsonProtocol {
  implicit val saveRequestFormat: RootJsonFormat[SaveRequest] = jsonFormat2(SaveRequest)
}