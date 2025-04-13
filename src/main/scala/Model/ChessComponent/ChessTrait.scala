package Model.ChessComponent

import Model.BasicChessComponent.StandartChess.Piece
import cController.ControllerComponent.Extra.Event

import scala.util.Try

trait ChessTrait {
    
    /**
     * getAllLegalMoves calculates all legal Moves that can be played in the current position (represented by the fen-String)
     * by the Color that is to move
     * @param fen current board state as fen-String
     * @return List of Int-Tupels (fromSquare, toSquare)
     */
    def getAllLegalMoves(fen: String): Try[List[(Int, Int)]]
    
    /**
     * isRemis checks if the given game state is a remis ending
     * @param fen current board state as fen-String
     * @param legalMoves List of possible moves in the current position
     * @return true if it is a remis. Otherwise false
     */
    def isRemis(fen: String, legalMoves: List[(Int, Int)]) : Try[Boolean]

    /**
     * getBestMove talks to the ChessApi and returns the best move from the chess engine depending on the depth (how
     * many moves in advance should be calculated)
     * @param fen current board state as fen-String
     * @param depth how many moves in advance should be calculated
     * @return best move as a String (fe e2e4)
     */
    def getBestMove(fen: String, depth: Int): Try[String]
}
