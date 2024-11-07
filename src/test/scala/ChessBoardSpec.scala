import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import ChessBoard._
import Piece._

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

        "return a correct splitline" in {
            splitLine() should be("    +-----+-----+-----+-----+-----+-----+-----+-----+\n");
        }

        "return a correct pieceLine" in {
            val p = Piece(PieceType.PAWN, Color.BLACK);
            val r = Vector(p, p, p, p, p, p, p, p);
            pieceLine(r, "7") should be("7   |  p  |  p  |  p  |  p  |  p  |  p  |  p  |  p  |\n");
        }

        "return a correct Vector[Piece] board given a FEN" in {
            val testFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            ChessBoard.fenToBoard(testFen) should equal(ChessBoard.getDefaultBoard());
        }

        "return a correct FEN given a board" in {
            ChessBoard.boardToFen(ChessBoard.getDefaultBoard()) should equal("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

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

        "return the correct new Fen String after making a move" in {
            val oldfen1 = "rnbqkb1r/ppp2ppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 2 4"
            val newfen1 = "rnbqkb1r/ppp2ppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQ1RK1 b kq - 3 4"
            ChessBoard.makeMove(oldfen1, (-1, -1)) should be(newfen1)

            val oldfen2 = "rnbqk2r/ppp1bppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNB1QRK1 b kq - 5 5"
            val newfen2 = "rnbq1rk1/ppp1bppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNB1QRK1 w - - 6 6"
            ChessBoard.makeMove(oldfen2, (-3, -1)) should be(newfen2)

            val oldfen3 = "rn2kbnr/pbpqpppp/1p6/3p4/3P1B2/2NQ4/PPP1PPPP/R3KBNR w KQkq - 2 5"
            val newfen3 = "rn2kbnr/pbpqpppp/1p6/3p4/3P1B2/2NQ4/PPP1PPPP/2KR1BNR b kq - 3 5"
            ChessBoard.makeMove(oldfen3, (-2, -1)) should be(newfen3)

            val oldfen4 = "r3kbnr/pbpqpppp/1pn5/3p4/3P1B2/2NQ1N2/PPP1PPPP/2KR1B1R b kq - 5 6"
            val newfen4 = "2kr1bnr/pbpqpppp/1pn5/3p4/3P1B2/2NQ1N2/PPP1PPPP/2KR1B1R w - - 6 7"
            ChessBoard.makeMove(oldfen4, (-4, -1)) should be(newfen4)

            val oldfen5 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            val newfen5 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
            ChessBoard.makeMove(oldfen5, ChessBoard.moveToIndex("e2", "e4")) should be(newfen5)

            val oldfen6 = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
            val newfen6 = "r1bqkbnr/pppppppp/2n5/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 1 2"
            ChessBoard.makeMove(oldfen6, ChessBoard.moveToIndex("b8", "c6")) should be(newfen6)
            val oldfen7 = "r1bqkbnr/pppppppp/2n5/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 1 2"
            val newfen7 = "r1bqkbnr/pppppppp/2n5/8/4P3/8/PPPPKPPP/RNBQ1BNR b kq - 2 2"
            ChessBoard.makeMove(oldfen7, ChessBoard.moveToIndex("e1", "e2")) should be(newfen7)

            val oldfen8 = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPPKPPP/RNBQ1B1R b kq - 1 3"
            val newfen8 = "r1bq1bnr/ppppkppp/2n5/4p3/4P3/5N2/PPPPKPPP/RNBQ1B1R w - - 2 4"
            ChessBoard.makeMove(oldfen8, ChessBoard.moveToIndex("e8", "e7")) should be(newfen8)

            val oldfen9 = "r1bqkbnr/pppppppp/2n5/8/8/5N2/PPPPPPPP/RNBQKB1R w KQkq - 2 2"
            val newfen9 = "r1bqkbnr/pppppppp/2n5/8/8/5N2/PPPPPPPP/RNBQKBR1 b Qkq - 3 2"
            ChessBoard.makeMove(oldfen9, ChessBoard.moveToIndex("h1", "g1")) should be(newfen9)

            val oldfen10 = "r1bqkbnr/pppppppp/2n5/8/8/5N2/PPPPPPPP/RNBQKBR1 b Qkq - 3 2"
            val newfen10 = "1rbqkbnr/pppppppp/2n5/8/8/5N2/PPPPPPPP/RNBQKBR1 w Qk - 4 3"
            ChessBoard.makeMove(oldfen10, ChessBoard.moveToIndex("a8", "b8")) should be(newfen10)

            val oldfen11 = "1rbqkb1r/pppppppp/2n2n2/8/8/2N2N2/PPPPPPPP/R1BQKBR1 w Qk - 6 4"
            val newfen11 = "1rbqkb1r/pppppppp/2n2n2/8/8/2N2N2/PPPPPPPP/1RBQKBR1 b k - 7 4"
            ChessBoard.makeMove(oldfen11, ChessBoard.moveToIndex("a1", "b1")) should be(newfen11)

            val oldfen12 = "1rbqkb1r/pppppppp/2n2n2/8/8/2N2N2/PPPPPPPP/1RBQKBR1 b k - 7 4"
            val newfen12 = "1rbqkbr1/pppppppp/2n2n2/8/8/2N2N2/PPPPPPPP/1RBQKBR1 w - - 8 5"
            ChessBoard.makeMove(oldfen12, ChessBoard.moveToIndex("h8", "g8")) should be(newfen12)
        }
    }

}
