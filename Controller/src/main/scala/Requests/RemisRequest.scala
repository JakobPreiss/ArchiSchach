package Controller.Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class RemisRequest(fen: String, legalMoves: List[(Int, Int)])

object RemisRequest extends DefaultJsonProtocol {
  implicit val promoteRequestFormat: RootJsonFormat[RemisRequest] = jsonFormat2(RemisRequest)
}