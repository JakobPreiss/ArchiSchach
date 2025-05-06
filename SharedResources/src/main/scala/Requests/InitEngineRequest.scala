package SharedResources.Requests

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

// ----------------------------------
// /init/engine request
// ----------------------------------
case class InitEngineRequest(
                              gameMode: String,
                              api:      String,
                              fen:      String,
                              depth:    Int
                            )

object InitEngineRequest extends DefaultJsonProtocol {
  implicit val initEngineRequestFormat: RootJsonFormat[InitEngineRequest] =
    jsonFormat4(InitEngineRequest)
}

// ----------------------------------
// /init/duo request
// ----------------------------------
case class InitDuoRequest(
                           gameMode: String,
                           api:      String,
                           fen:      String
                         )

object InitDuoRequest extends DefaultJsonProtocol {
  implicit val initDuoRequestFormat: RootJsonFormat[InitDuoRequest] =
    jsonFormat3(InitDuoRequest)
}
