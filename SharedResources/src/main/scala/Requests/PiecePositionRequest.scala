package SharedResources.Requests

import SharedResources.PieceTypeJsonFormat
import SharedResources.ColorJsonFormat
import SharedResources.Piece
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class PiecePositionRequest(board: Vector[Piece], piece: Piece)

object PiecePositionRequest extends DefaultJsonProtocol {
  implicit val pieceTypeFormat: RootJsonFormat[Piece] = jsonFormat2(Piece)
  implicit val piecePositionRequest: RootJsonFormat[PiecePositionRequest] = jsonFormat2(PiecePositionRequest)
}