package SharedResources.Requests

import SharedResources.PieceTypeJsonFormat
import SharedResources.ColorJsonFormat
import SharedResources.Piece
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class PiecesPositionRequest(board: Vector[Piece], piece: List[Piece])

object PiecesPositionRequest extends DefaultJsonProtocol {
  implicit val pieceTypeFormat: RootJsonFormat[Piece] = jsonFormat2(Piece)
  implicit val piecePositionRequest: RootJsonFormat[PiecesPositionRequest] = jsonFormat2(PiecesPositionRequest)
}