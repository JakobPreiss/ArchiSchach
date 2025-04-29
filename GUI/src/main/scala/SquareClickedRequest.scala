package GUI

import spray.json.{DefaultJsonProtocol, RootJsonFormat}


case class SquareClickedRequest(clickedSquare: Int)

object SquareClickedRequest extends DefaultJsonProtocol {
  implicit val piecePositionRequest: RootJsonFormat[SquareClickedRequest] = jsonFormat1(SquareClickedRequest)
}