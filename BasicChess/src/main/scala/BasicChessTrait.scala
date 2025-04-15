package BasicChess

import BasicChess.StandartChess.{Color, Piece, PieceType}

import scala.util.Try

trait BasicChessTrait {
    /**
     * getBoardString returns the string view of the current board state for the TUI given by the Vector parameter
     * @param fen current board as vector
     * @return string view of the current board
     */
    def getBoardString(fen : String ) : Try[String]

    /**
     * fenToBoard translates the fen-String representation of the board state as a Vector[Piece] Type
     *
     * @param fen current board state as fen-String
     * @return board state as a Vector[Piece]
     * */
    def fenToBoard(fen: String): Try[Vector[Piece]]

    /**
     * getAllPseudoLegalMoves calculates all pseudo legal Moves (moves that the pieces can do without checking if the king would be checked after)
     * that can be played in the current position (represented by the fen-String) by the Color that is to move.
     * @param fen current board state as fen-String
     * @return List of Int-Tupels (fromSquare, toSquare)
     * */
    def getAllPseudoLegalMoves(fen: String): Try[List[(Int, Int)]]

    /**
     * canPromote checks if a pawn promotion is possible on the given board state and returns the index of the square of the promoteable pawn
     * @param fen current board state as fen-String
     * @return index of the square of the promoteable pawn or None if not possible
     * */
    def canPromote(fen: String): Try[Option[Int]]

    /**
     * promote changes the current board state after a promotion happend. The Pawn will be replaced by the given Piece
     * @param pieceName One Letter representing the requested piece
     * @param fen current board state as fen-String
     * @param position index of the square of the promoteable pawn
     * @return new board state as fen-String
     * */
    def promote(pieceName: String, fen: String, position: Int): Try[String]

    /**
     * isColorPiece checks if a piece a given position is a piece of the Color that is to move right now
     * @param fen current board state as fen-String
     * @param position the position that should be checked
     * @return true if there is a piece of the color that is to move. Otherwise false
     * */
    def isColorPiece(fen: String, position: Int): Try[Boolean]

    /**
     * translateCastle translates a raw King Move (over 2 squares) into a castling move that the model will understand
     * @param board current board state as Vector[Piece],
     * @param move move as a Int-Tupel (fromSquare, toSquare)
     * @return move (if move was a castle move it is a little bit different (negative number in front)
     * */
    def translateCastle(board: Vector[Piece], move: (Int, Int)): Try[(Int, Int)]

    /**
     * translateCastle translates a raw King Move (over 2 squares) into a castling move that the model will understand
     * @param fen current board state as fen,
     * @param move  move as a Int-Tupel (fromSquare, toSquare)
     * @return move (if move was a castle move it is a little bit different (negative number in front)
     * */
    def translateCastleFromFen(fen : String, move: (Int, Int)): Try[(Int, Int)]
    /**
     * piecesPositions searches given pieces on the board and returns a list of all squares the given pieces are currently on
     * @param board current board state as Vector[Piece]
     * @param pieces list of pieces to look for
     * @return list of all squares the given piece is currently on
     */
    def piecesPositions(board: Vector[Piece], pieces: List[Piece]): Try[List[Int]]

    /**
     * piecePositions searches a given piece on the board and returns a list of all squares the given piece is currently on
     * @param board  current board state as Vector[Piece]
     * @param pieces a piece to look for
     * @return list of all squares the given piece is currently on
     * */
    def piecePositions(board: Vector[Piece], piece: Piece): Try[List[Int]]
    
    /**
     * onBoard checks if a given move leaves the piece of the premisses of the board
     * @param beginningPosition where the piece starts
     * @param rowDirection how many rows in what direction
     * @param columDirection how many colums in what direction
     * @return true if it remains on the board. Otherwise false
     */
    def onBoard(beginningPosition: Int, rowDirection: Int, columDirection: Int): Try[Boolean]

    /**
     * boardToFen translates the Vector[Piece] Representation of the board to a fen-String
     * @param board current board state as Vector[Piece]
     * @return current board state as fen
     */
    def boardToFen(board: Vector[Piece]): Try[String]

    /**
     * isDifferentColorPiece
     * @param fen
     * @param position
     * @return
     */
    def isDifferentColorPiece(fen: String, position: Int): Try[Boolean]

    /**
     * getDefaultFen returns the starting fen position without the extra info at the end
     * @return starting fen
     */
    def getDefaultFen() : String

    /**
     * translateMoveStringToInt translates a move in the format of e2e4 to a Int-Tupel move
     * @param fen  current board state as fen-String
     * @param move as one String
     * @return move as Int-Tupel
     * */
    def translateMoveStringToInt(fen: String, move: String): Try[(Int, Int)]

    /**
     * getDefaultBoard returns the starting board in the Vector[Piece] format
     * @return starting board in the Vector[Piece] format
     * */
    def getDefaultBoard(): Vector[Piece]

    /**
     * pieceMoves retuns a List of Move-directions that the given pieces can move in
     * @param pieceTypes the piece of which we want the directions
     * @return the directions the piece can go
     */
    def pieceMoves(pieceTypes: List[PieceType]): Try[List[(Int, Int)]]

    /**
     * isValidFen checks if the given fen fits the format
     * @param fen gamestate in fen
     * @return Success(fen) if correct Failure(exeption) if incorrect
     */
    def isValidFen(fen : String) : Try[String]

    /**
     * updateCastleing updates the fen part that keeps track of castleing rights
     * @param fenCastles Castle-part of fen
     * @param move move
     * @return updated Castle-part of fen
     */
    def updateCastleing(fenCastles: String, move:(Int, Int)): Try[String]

    /**
     * updateEnPassant updates the fen part that keeps track of enPassant rights
     * @param fen fenpart that keeps track of enPassant
     * @param move move
     * @return updated fenpart that keeps track of enPassant
     */
    def updateEnpassant(fen: String, move:(Int, Int)): Try[String]

    /**
     * calculateMoveValues calculates the correct color pieces relevant for castleing
     * @param color Color that moved
     * @return pieces in correct color
     */
    def calculateMoveValues(color: Color) : Try[(Piece, Piece, Piece)]

    /**
     * isCorrectBoardVector checks if the given Boardvector is possible and has the correct length
     * @param board Bordvector that has to be checked
     * @return if Success: the given Boardvector, otherwise a Failure with exception
     */
    def isCorrectBoardVector(board : Vector[Piece]) : Try[Vector[Piece]]

    /**
     * isValidMove checks if the move given matches the correct string format
     * @param move Move in String Format
     * @return if Success: Move in String format, otherwise a Failure with exception
     */
    def isValidMove(move: String): Try[String]

    /**
     * makeMove gets a Int-Tupel as a move to make and transforms the given fen into a new fen with the given move made
     *
     * @param fen  current board state as fen-String
     * @param move move as a Int-Tupel (fromSquare, toSquare)
     * @return new board state as fen-String */
    def makeMove(fen: String, move: (Int, Int)): Try[String]
}
