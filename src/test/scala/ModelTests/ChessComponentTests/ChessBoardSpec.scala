package ModelTests.ChessComponentTests

import Model.BasicChessComponent.StandartChess.{ChessBoard, Color, Piece, PieceType}
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import scala.util.{Failure, Success}

class ChessBoardSpec extends AnyWordSpec {

    "ChessBoard" should {
        "return default board" in {
            val p = Piece(PieceType.PAWN, Color.BLACK);
            val r = Piece(PieceType.ROOK, Color.BLACK);
            val n = Piece(PieceType.KNIGHT, Color.BLACK);
            val b = Piece(PieceType.BISHOP, Color.BLACK);
            val q = Piece(PieceType.QUEEN, Color.BLACK);
            val k = Piece(PieceType.KING, Color.BLACK);

            val P = Piece(PieceType.PAWN, Color.WHITE);
            val R = Piece(PieceType.ROOK, Color.WHITE);
            val N = Piece(PieceType.KNIGHT, Color.WHITE);
            val B = Piece(PieceType.BISHOP, Color.WHITE);
            val Q = Piece(PieceType.QUEEN, Color.WHITE);
            val K = Piece(PieceType.KING, Color.WHITE);
            val point = Piece(PieceType.EMPTY, Color.EMPTY);
            val board: Vector[Piece] = Vector(
                r, n, b, q, k, b, n, r, 
                p, p, p, p, p, p, p, p,
                point, point, point, point, point, point, point, point, 
                point, point, point, point, point, point, point, point, 
                point, point, point, point, point, point, point, point, 
                point, point, point, point, point, point, point, point, 
                P, P, P, P, P, P, P, P, 
                R, N, B, Q, K, B, N, R)

            getDefaultBoard() should equal(board);
        }
        "return the correct board string" in {
            getBoardString(getDefaultBoard()) should be((
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "8   |  r  |  n  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "7   |  p  |  p  |  p  |  p  |  p  |  p  |  p  |  p  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "6   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "5   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "4   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "3   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "2   |  P  |  P  |  P  |  P  |  P  |  P  |  P  |  P  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "1   |  R  |  N  |  B  |  Q  |  K  |  B  |  N  |  R  |\n" + 
                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + 
                    "       a     b     c     d     e     f     g     h     "));
        }

        "return a correct Vector[Piece] board given a FEN" in {
            val testFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            ChessBoard.fenToBoard(testFen) should equal(ChessBoard.getDefaultBoard());
        }

        "return a correct FEN given a board" in {
            ChessBoard.boardToFen(ChessBoard.getDefaultBoard()) should equal("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");

        }

        "return correct index given coordinates" in {
            ChessBoard.coordinatesToIndex("a1") should be(56);
            ChessBoard.coordinatesToIndex("e1") should be(60);
            ChessBoard.coordinatesToIndex("a8") should be(0);
            ChessBoard.coordinatesToIndex("h8") should be(7);
            ChessBoard.coordinatesToIndex("d4") should be(35);
        }
        "return correct coordinates given index" in {
            ChessBoard.indexToCoordinates(56) should be("a1");
            ChessBoard.indexToCoordinates(60) should be("e1");
            ChessBoard.indexToCoordinates(0) should be("a8");
            ChessBoard.indexToCoordinates(7) should be("h8");
            ChessBoard.indexToCoordinates(35) should be("d4");
        }
        "return correct index move given a coordinates move" in {
            ChessBoard.moveToIndex("a1", "e1") should be((56, 60))
            ChessBoard.moveToIndex("d4", "a8") should be((35, 0))
            
        }

        "return the correct moves for castling" in {
            val move1 : (Int, Int) = (-1, -1)
            val move2 : (Int, Int) = (-2,-1)
            val move3 : (Int, Int) = (-3,-1)
            val move4 : (Int, Int) = (-4,-1)
            val move5 : (Int, Int) = (4,5)
            val move6 : (Int, Int) = (60,61)
            val move7 : (Int, Int) = (0,1)
            val board = ChessBoard.fenToBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
            ChessBoard.translateCastle(board, (60,62)) should be (move1)
            ChessBoard.translateCastle(board, (60,58)) should be (move2)
            ChessBoard.translateCastle(board, (4,6)) should be (move3)
            ChessBoard.translateCastle(board, (4,2)) should be (move4)
            ChessBoard.translateCastle(board, (4,5)) should be (move5)
            ChessBoard.translateCastle(board, (60, 61)) should be(move6)
            ChessBoard.translateCastle(board, (0,1)) should be (move7)
        }

        "detect a possible promotion" in {
            val fenWhitePromotion = "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5"
            ChessBoard.canPromote(fenWhitePromotion) should be (Some(1))

            val fenBlackPromotion = "rQbqkbnr/1pppppp1/8/8/8/8/P1PPPP1P/RNBQKBNp w Qkq - 0 6"
            ChessBoard.canPromote(fenBlackPromotion) should be (Some(63))
        }

        "promote correctly" in {
            val possiblePromotionFen = "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5"
            ChessBoard.promote("Q", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rQbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("q", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rQbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("R", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rRbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("r", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rRbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("N", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rNbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("n", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rNbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("B", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rBbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("b", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1) should be ("rBbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
        }

        "validate fen correctly" in {
            val fen1 = "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5"
            val fen2 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            val fen3 = "rnbq1bnr/ppppkppp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR w KQ - 0 3"
            val wrongFen1 = ""
            val wrongFen2 = "rnbq1bnr/ppppkpZp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR w KQ - 0 3"
            val wrongFen3 = "rnbq1bnr/ppppkpppp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR w KQ - 0 3"
            val wrongFen4 = "rnbq1bnr/ppp11ppp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR w KQ - 0 3"
            val wrongFen5 ="rnbq1bnr/rnbq1bnr/ppp31ppp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR w KQ - 0 3"

            ChessBoard.isValidFen(fen1) should be(Success("rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5"))
            ChessBoard.isValidFen(fen2) should be(Success("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"))
            ChessBoard.isValidFen(fen3) should be(Success("rnbq1bnr/ppppkppp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR w KQ - 0 3"))
            val message1 = ChessBoard.isValidFen(wrongFen1) match {
                case Success(i) => i
                case Failure(m) => m.getMessage
            }
            message1 should be ("fen doesn`t match follow this example: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 ")

            val message2 = ChessBoard.isValidFen(wrongFen2) match {
                case Success(i) => i
                case Failure(m) => m.getMessage
            }
            message2 should be("fen doesn`t match follow this example: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 ")
            val message3 = ChessBoard.isValidFen(wrongFen3) match {
                case Success(i) => i
                case Failure(m) => m.getMessage
            }
            message3 should be("Not 8 columns in each row")

            val message4 = ChessBoard.isValidFen(wrongFen4) match {
                case Success(i) => i
                case Failure(m) => m.getMessage
            }
            message4 should be("two subsequent digits")
        }
    }

}
