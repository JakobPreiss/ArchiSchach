package Model.ChessComponent.BasicChessComponent.StandartChess

import Model.ChessComponent.*
import Model.ChessComponent.BasicChessComponent.StandartChess.Color.EMPTY
import Model.ChessComponent.BasicChessComponent.StandartChess.{Color, Piece, PieceType, PseudoMoves}
import Model.ChessComponent.RealChess.LegalMoves

import scala.annotation.tailrec
import scala.util.{Try, Success, Failure}

object ChessBoard {

    def getBoardString(board: Vector[Piece]): String = {
        @tailrec def buildBoard(index: Int, accumulator: String): String = {
            index match {
                case 8 => {
                    val letters = "abcdefgh";
                    accumulator + splitLine() + "       " + letters.map(element => s"$element     ").mkString("");
                }
                case _ => buildBoard(index + 1, accumulator + splitLine() + pieceLine(board.slice(index * 8, index * 8 + 8), s"${8 - index}"));
            }
        }

        def splitLine(): String = {
            "    " + "+-----" * 8 + "+\n"
        }

        def pieceLine(line: Vector[Piece], columnNumber: String): String = {
            s"$columnNumber   |" + line.map(el => s"  ${el.toString()}  |").mkString("") + "\n";
        }

        buildBoard(0, "")

    }

    def getDefaultBoard(): Vector[Piece] = {
        val defaultFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        fenToBoard(defaultFEN);
    }

    def boardToFen(board: Vector[Piece]): String = {

        @tailrec
        def pieceToStringTransformer(fen: List[String], accumulator: List[String], stringPosition: Int): String = {
            fen match {
                case Nil => {
                    if (stringPosition == 0) {
                        accumulator.mkString
                    } else {
                        (stringPosition.toString::accumulator).mkString
                    }

                }
                case h :: t => {
                    h match {
                        case "." => pieceToStringTransformer(t, accumulator, stringPosition + 1);
                        case element => {
                            if (stringPosition == 0) {
                                pieceToStringTransformer(t, element::accumulator, stringPosition)
                            } else {
                                pieceToStringTransformer(t, element::stringPosition.toString::accumulator, 0)
                            }
                        }
                    }
                }
            }
        }

        @tailrec
        def rowdivider(accumulator: List[String], size: Int, pieces: List[Piece]): List[String] = {
            pieces match {
                case Nil => accumulator
                case h :: t => {
                    if ((size + 1) % 8 == 0 && size != 63) {
                        rowdivider("/"::h.toString()::accumulator, size+1, t)
                    } else {
                        rowdivider(h.toString()::accumulator, size+1, t)
                    }
                }
            }
        }
        pieceToStringTransformer(rowdivider(List(), 0, board.toList), List(), 0);
    }

    def fenToBoard(fen: String): Vector[Piece] = {
        @tailrec def emptySpaceAdder(accumulator: List[Piece], spaces: Int): List[Piece] = {
            spaces match {
                case el if el > 0 => emptySpaceAdder(Piece(PieceType.EMPTY, Color.EMPTY) :: accumulator, spaces - 1)
                case _ => accumulator
            }
        }

        @tailrec def pieceAdder(fen: List[Char], accumulator: List[Piece]): List[Piece] = {
            fen match {
                case Nil => accumulator
                case h :: t => {
                    h match {
                        case '/' => pieceAdder(t, accumulator)
                        case 'p' => pieceAdder(t, Piece(PieceType.PAWN, Color.BLACK) :: accumulator)
                        case 'r' => pieceAdder(t, Piece(PieceType.ROOK, Color.BLACK) :: accumulator)
                        case 'n' => pieceAdder(t, Piece(PieceType.KNIGHT, Color.BLACK) :: accumulator)
                        case 'b' => pieceAdder(t, Piece(PieceType.BISHOP, Color.BLACK) :: accumulator)
                        case 'q' => pieceAdder(t, Piece(PieceType.QUEEN, Color.BLACK) :: accumulator)
                        case 'k' => pieceAdder(t, Piece(PieceType.KING, Color.BLACK) :: accumulator)
                        case 'P' => pieceAdder(t, Piece(PieceType.PAWN, Color.WHITE) :: accumulator)
                        case 'R' => pieceAdder(t, Piece(PieceType.ROOK, Color.WHITE) :: accumulator)
                        case 'N' => pieceAdder(t, Piece(PieceType.KNIGHT, Color.WHITE) :: accumulator)
                        case 'B' => pieceAdder(t, Piece(PieceType.BISHOP, Color.WHITE) :: accumulator)
                        case 'Q' => pieceAdder(t, Piece(PieceType.QUEEN, Color.WHITE) :: accumulator)
                        case 'K' => pieceAdder(t, Piece(PieceType.KING, Color.WHITE) :: accumulator)
                        case element: Char if element.isDigit => pieceAdder(t, emptySpaceAdder(accumulator, element.toInt - 48))
                    }
                }
            }
        }

        pieceAdder(fen.split(" ")(0).toList, List()).reverse.toVector
    }

    def coordinatesToIndex(coordinates: String): Int = {
        (coordinates.charAt(0).toInt - 97) + 8 * (8 - (coordinates.charAt(1).toInt - 48));
    }

    def indexToCoordinates(index: Int): String = {
        val file = (index % 8)
        val rank = 7 - (index / 8)

        val fileChar = (file + 97).toChar
        val rankChar = (rank + 49).toChar

        s"$fileChar$rankChar"
    }

    def moveToIndex(from: String, to: String): (Int, Int) = {
        (coordinatesToIndex(from), coordinatesToIndex(to));
    }

    def updateCastleing(fenCastles: String, move:(Int, Int)): String = {
        move match {
            case (-1, -1) => {
                val newRights = fenCastles.replace("K", "").replace("Q", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case (-2, -1) => {
                val newRights = fenCastles.replace("Q", "").replace("K", "")
                if (newRights.isEmpty) "-" else newRights

            }
            case (-3, -1) => {
                val newRights = fenCastles.replace("k", "").replace("q", "")
                if (newRights.isEmpty) "-" else newRights 
            }
            case (-4, -1) => {
                val newRights = fenCastles.replace("q", "").replace("k", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case (60, _) => {
                val newRights = fenCastles.replace("K", "").replace("Q", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case (4, _) => {
                val newRights = fenCastles.replace("k", "").replace("q", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case (0, _) => {
                val newRights = fenCastles.replace("q", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case (7, _) => {
                val newRights = fenCastles.replace("k", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case (63, _) => {
                val newRights = fenCastles.replace("K", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case (56, _) => {
                val newRights = fenCastles.replace("Q", "")
                if (newRights.isEmpty) "-" else newRights
            }
            case _ => fenCastles
        }
    }
    
    def updateEnpassant(fen: String, move:(Int, Int)): String = {
        val (from, to) = move;
        if(to < 0) {
            return "-"
        }
        val fenSplit = fen.split(" ");
        val board = ChessBoard.fenToBoard(fen);
        if (board(from).pieceType == PieceType.PAWN && Math.abs(from - to) == 16) {
            ChessBoard.indexToCoordinates(from + 8 * ((to - from) / Math.abs(to - from)))
        } else {
            "-"
        }
    }

    def translateCastle(board: Vector[Piece], move: (Int, Int)): (Int, Int) = {
        val (from, to) = move;
        if (board(from) == Piece(PieceType.KING, Color.BLACK)) {
            if (move == (4, 2)) {
                (-4, -1)
            } else if (move == (4, 6)) {
                (-3, -1)
            } else {
                move
            }
        } else if (board(from) == Piece(PieceType.KING, Color.WHITE)) {
            if (move == (60, 62)) {
                (-1, -1)
            } else if (move == (60, 58)) {
                (-2, -1)
            } else {
                move
            }
        } else {
            move
        }
    }
    
    def translateMoveStringToInt(fen: String, move: String): (Int, Int) = {
        ChessBoard.translateCastle(ChessBoard.fenToBoard(fen), ChessBoard.moveToIndex(move.substring(0, 2), move.substring(2, 4)))
    }

    def makeMove(fen: String, move: (Int, Int)): String = {
        val fenSplit = fen.split(" ")
        val board = ChessBoard.fenToBoard(fen);
        val newBoard = LegalMoves.makeMove(board, move);
        if (fenSplit(1) == "w") {
            ChessBoard.boardToFen(newBoard) + " b " + updateCastleing(fenSplit(2), move) + " " + updateEnpassant(fen, move) + " " + fenSplit(4) + " " + fenSplit(5)
        } else {
            ChessBoard.boardToFen(newBoard) + " w " + updateCastleing(fenSplit(2), move) + " " + updateEnpassant(fen, move) + " " + fenSplit(4) + " " + (fenSplit(5).toInt+1).toString
        }
    }
    
    def canPromote(fen: String): Option[Int] = {
        val boardFen = fen.split(" ")(0);
        val first_row = boardFen.split("/")(0);
        val last_row = boardFen.split("/")(7);
        if (first_row.contains("P")) {
            return Some(first_row.indexOf('P'))
        }
        if (last_row.contains("p")) {
            return Some(last_row.indexOf('p') + 56)
        }
        None
    }

    def promote(pieceName: String, fen : String, position : Int) : String = {
        val board = ChessBoard.fenToBoard(fen)
        val fensplit = fen.split(" ")
        val colors = PseudoMoves.extractColor(fensplit(1))

        def pieceFactory(pieceName1: String, color: Color) = pieceName1 match {
            case "Q" => Piece(PieceType.QUEEN, color)
            case "q" => Piece(PieceType.QUEEN, color)
            case "B" => Piece(PieceType.BISHOP, color)
            case "b" => Piece(PieceType.BISHOP, color)
            case "N" => Piece(PieceType.KNIGHT, color)
            case "n" => Piece(PieceType.KNIGHT, color)
            case "R" => Piece(PieceType.ROOK, color)
            case "r" => Piece(PieceType.ROOK, color)
        }
        ChessBoard.boardToFen(board.updated(position, pieceFactory(pieceName, colors._3))) + " " + fensplit(1) + " " + fensplit(2) + " " + fensplit (3) + " " + fensplit(4) + " " + fensplit(5);
    }
    
    def colorDetection(fen : String, position : Int) : Color = {
        fen.split(" ")(1) match {
            case "w" => Color.WHITE
            case "b" => Color.BLACK
            case _ => Color.EMPTY
        }
    }
    
    def isColorPiece(fen : String, position : Int) : Boolean = {
        val board = ChessBoard.fenToBoard(fen)
        board(position).color == colorDetection(fen, position)
    }

    def isDifferentColorPiece(fen: String, position: Int): Boolean = {
        val board = ChessBoard.fenToBoard(fen)
        board(position).color != EMPTY && board(position).color != colorDetection(fen, position)
    }

    def isValidFen(fen : String) : Try[String] = {
        if (fen.matches("^(((?:[rnbqkpRNBQKP1-8]+/){7})[rnbqkpRNBQKP1-8]+)\\s([b|w])\\s([K|Q|k|q]{1,4})\\s(-|[a-h][1-8])\\s(\\d+\\s\\d+)$")) {
            val beginningfen = fen.split(" ")(0)
            val fenList = beginningfen.split("/")
            if (fenList.length != 8) {
                return Failure(new IllegalArgumentException("not 8 rows"))
            }
            val numbers = List("1", "2", "3", "4", "5", "6", "7", "8")
            val pieces = List("p", "n", "b", "r", "q", "k", "P", "N", "B", "R", "Q", "K")

            @tailrec
            def checkCorrectOrder(fenParts: List[String], previousWasDigit: Boolean, previousWasPiece: Boolean, numbers: List[String], pieces: List[String], fieldsum: Int): Try[Int] = {
                fenParts match {
                    case Nil => Success(fieldsum)
                    case h :: t if (numbers.contains(h) && previousWasDigit) => Failure(new IllegalArgumentException("two subsequent digits"))
                    case h :: t if (numbers.contains(h) && !previousWasDigit) => checkCorrectOrder(t, true, false, numbers, pieces, (fieldsum + h.toInt))
                    case h :: t if (pieces.contains(h)) => checkCorrectOrder(t, false, true, numbers, pieces, (fieldsum + 1))
                    case _ => Failure(new IllegalArgumentException("fen contains unallowed letters"))
                }
            }

            val results: Array[Try[Int]] = fenList.map { fen =>
                val fenParts: List[String] = fen.toList.map(_.toString)
                checkCorrectOrder(fenParts, previousWasDigit = false, previousWasPiece = false, numbers, pieces, fieldsum = 0)
            }
            val firstFailure = results.collectFirst { case Failure(exception) => exception }
            firstFailure match {
                case Some(exception) =>
                    return Failure(exception)
                case None =>
            }
            if (results.exists(_ != Success(8))) {
                return Failure(new IllegalArgumentException("Not 8 columns in each row"))
            }
            return Success(fen)
        } else {
            return Failure(new IllegalArgumentException("fen doesn`t match follow this example: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 "))
        }
    }

    def isValidMove(move: String): Try[String] = {
        if (move.matches("^[a-h][1-8][a-h][1-8]$")) {
            Success(move)
        } else {
            Failure(new IllegalArgumentException("Invalid move format. Expected format: a1b2"))
        }
    }

    // TODO: Implement the logic to check if the move is valid based on the current game state
    def isValidMove(move: (Int, Int)): Try[(Int, Int)] = {
        Success(move)
    }

    def isValidPosition(fen: String, position: Int): Try[Int] = {
        if (position >= 0 && position < fen.length) {
            Success(position)
        } else {
            Failure(new IllegalArgumentException("Invalid position. Expected a value between 0 and 71."))
        }
    }

    def isValidPieceName(pieceName: String): Try[String] = {
        if (pieceName.matches("^[KQRBNPkqrbnp]$")) {
            Success(pieceName)
        } else {
            Failure(new IllegalArgumentException("Invalid piece name. Expected one of K, Q, R, B, N, P, k, q, r, b, n, p."))
        }
    }
}
