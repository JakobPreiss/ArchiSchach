package SharedResources.Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class PlayRequest(move : (Int, Int))

object PlayRequest extends DefaultJsonProtocol {
  implicit val playRequest: RootJsonFormat[PlayRequest] = jsonFormat1(PlayRequest)
}