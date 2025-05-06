package SharedResources.Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class PromotePawnRequest(pieceKind: String)

object PromotePawnRequest extends DefaultJsonProtocol {
  implicit val piecePositionRequest: RootJsonFormat[PromotePawnRequest] = jsonFormat1(PromotePawnRequest)
}