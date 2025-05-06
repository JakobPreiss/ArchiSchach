package GUI

import spray.json.{DefaultJsonProtocol, RootJsonFormat}


case class SquareClickedRequest(square: Int)

object SquareClickedRequest extends DefaultJsonProtocol {
  implicit val piecePositionRequest: RootJsonFormat[SquareClickedRequest] = jsonFormat1(SquareClickedRequest)
}