package Controller.Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class PromoteRequest(piecename: String, fen: String, position: Int)

object PromoteRequest extends DefaultJsonProtocol {
  implicit val promoteRequestFormat: RootJsonFormat[PromoteRequest] = jsonFormat3(PromoteRequest)
}