package Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Move(from: Int, to: Int)
case class MoveRequest(fen: String, move: Move)

object MoveRequest extends DefaultJsonProtocol {
  implicit val moveFormat: RootJsonFormat[Move]           = jsonFormat2(Move)
  implicit val moveReqFormat: RootJsonFormat[MoveRequest] = jsonFormat2(MoveRequest)
}