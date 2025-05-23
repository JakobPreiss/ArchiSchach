package SharedResources.Requests

import SharedResources.{PieceType, PieceTypeJsonFormat}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class PieceMovesRequest(pieceTypes: List[PieceType])

object PieceMovesRequest extends DefaultJsonProtocol {
  implicit val piecePositionRequest: RootJsonFormat[PieceMovesRequest] = jsonFormat1(PieceMovesRequest)
}