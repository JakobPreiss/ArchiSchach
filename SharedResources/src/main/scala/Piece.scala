package SharedResources

import spray.json._

enum Color:
    case BLACK, WHITE, EMPTY

object Color:
    def fromString(str: String): Color = str match
        case "BLACK" => BLACK
        case "WHITE" => WHITE
        case "EMPTY" => EMPTY
        case _       => throw new IllegalArgumentException(s"'$str' is not a valid Color")

implicit object ColorJsonFormat extends RootJsonFormat[Color]:
    def write(c: Color): JsValue = JsString(c.toString)

    def read(value: JsValue): Color = value match
        case JsString(str) => Color.fromString(str)
        case _             => deserializationError("Color must be represented as a string")

enum PieceType:
    case PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING, EMPTY

object PieceType:
    def fromString(str: String): PieceType = str match
        case "P" | "p" => PAWN
        case "R" | "r" => ROOK
        case "N" | "n" => KNIGHT
        case "B" | "b" => BISHOP
        case "Q" | "q" => QUEEN
        case "K" | "k" => KING
        case "."       => EMPTY
        case other     => throw new IllegalArgumentException(s"'$other' is not a valid PieceType")

implicit object PieceTypeJsonFormat extends RootJsonFormat[PieceType]:
    def write(c: PieceType): JsValue = JsString(c.toString)

    def read(value: JsValue): PieceType = value match
        case JsString(str) => PieceType.fromString(str)
        case _             => deserializationError("PieceType must be represented as a string")

case class Piece(pieceType: PieceType, color: Color):
    override def toString(): String =
        val pieceMap: Map[(PieceType, Color), String] = Map(
            (PieceType.PAWN, Color.WHITE)   -> "P",
            (PieceType.PAWN, Color.BLACK)   -> "p",
            (PieceType.ROOK, Color.WHITE)   -> "R",
            (PieceType.ROOK, Color.BLACK)   -> "r",
            (PieceType.KNIGHT, Color.WHITE) -> "N",
            (PieceType.KNIGHT, Color.BLACK) -> "n",
            (PieceType.BISHOP, Color.WHITE) -> "B",
            (PieceType.BISHOP, Color.BLACK) -> "b",
            (PieceType.QUEEN, Color.WHITE)  -> "Q",
            (PieceType.QUEEN, Color.BLACK)  -> "q",
            (PieceType.KING, Color.WHITE)   -> "K",
            (PieceType.KING, Color.BLACK)   -> "k",
            (PieceType.EMPTY, Color.EMPTY)  -> "."
        )
        pieceMap.getOrElse((pieceType, color), "?")

object PieceJsonProtocol extends DefaultJsonProtocol:
    implicit val colorFormat: RootJsonFormat[Color]       = ColorJsonFormat
    implicit val pieceTypeFormat: RootJsonFormat[PieceType] = PieceTypeJsonFormat
    implicit val pieceFormat: RootJsonFormat[Piece]       = jsonFormat2(Piece)
