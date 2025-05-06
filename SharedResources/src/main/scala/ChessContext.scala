package SharedResources

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

enum State:
    case WhitePlaying, BlackPlaying, WhiteWon, BlackWon, Remis

case class ChessContext(var state: State = State.WhitePlaying):
    def handle(event: Event): Unit =
        val color = event.fen.split(" ")(1)
        if event.remis then
            state = State.Remis
        else color match
            case "b" =>
                state = if event.noMoves then State.WhiteWon else State.BlackPlaying
            case "w" =>
                state = if event.noMoves then State.BlackWon else State.WhitePlaying

object ChessJsonProtocol extends DefaultJsonProtocol {
    // custom formatter for enum
    implicit object StateFormat extends JsonFormat[State] {
        def write(s: State) = JsString(s.toString)
        def read(value: JsValue) = value match
            case JsString(str) =>
                State.valueOf(str) // throws if unknown
            case _ => deserializationError("State must be a string")
    }

    // derive the ChessContext format
    implicit val chessContextFormat: RootJsonFormat[ChessContext] =
        jsonFormat1(ChessContext.apply)
}
