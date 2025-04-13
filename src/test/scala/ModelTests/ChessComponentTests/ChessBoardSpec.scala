package ModelTests.ChessComponentTests

import Model.BasicChessComponent.StandartChess.Color.{BLACK, WHITE}
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

            ChessBoard.getDefaultBoard() should equal(board);
        }
        "return the correct board string" in {
            ChessBoard.getBoardString(ChessBoard.getDefaultBoard()) should be((
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
            ChessBoard.promote("Q", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rQbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("q", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rQbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("R", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rRbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("r", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rRbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("N", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rNbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("n", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rNbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("B", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rBbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
            ChessBoard.promote("b", "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5", 1, WHITE) should be ("rBbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5")
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

        "return the correct new Fen String after making a move" in {
            val oldfen1 = "rnbqkb1r/ppp2ppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 0 4"
            val newfen1 = "rnbqkb1r/ppp2ppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQ1RK1 b kq - 0 4"
            ChessBoard.makeMove(oldfen1, (-1, -1)) should be(newfen1)

            val oldfen2 = "rnbqk2r/ppp1bppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNB1QRK1 b kq - 0 5"
            val newfen2 = "rnbq1rk1/ppp1bppp/3p1n2/4p3/2B1P3/5N2/PPPP1PPP/RNB1QRK1 w - - 0 6"
            ChessBoard.makeMove(oldfen2, (-3, -1)) should be(newfen2)

            val oldfen3 = "rn2kbnr/pbpqpppp/1p6/3p4/3P1B2/2NQ4/PPP1PPPP/R3KBNR w KQkq - 0 5"
            val newfen3 = "rn2kbnr/pbpqpppp/1p6/3p4/3P1B2/2NQ4/PPP1PPPP/2KR1BNR b kq - 0 5"
            ChessBoard.makeMove(oldfen3, (-2, -1)) should be(newfen3)

            val oldfen4 = "r3kbnr/pbpqpppp/1pn5/3p4/3P1B2/2NQ1N2/PPP1PPPP/2KR1B1R b kq - 0 6"
            val newfen4 = "2kr1bnr/pbpqpppp/1pn5/3p4/3P1B2/2NQ1N2/PPP1PPPP/2KR1B1R w - - 0 7"
            ChessBoard.makeMove(oldfen4, (-4, -1)) should be(newfen4)


            val oldfen13 = "rnbq2nr/ppppkppp/8/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQ - 0 4"
            val newfen13 = "rnbq2nr/ppppkppp/8/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQ1RK1 b - - 0 4"
            ChessBoard.makeMove(oldfen13, (-1, -1)) should be(newfen13)

            val oldfen14 = "rnbq1bnr/pp1kp1pp/2p2p2/3p4/3P1B2/2NQ4/PPP1PPPP/R3KBNR w KQ - 0 5"
            val newfen14 = "rnbq1bnr/pp1kp1pp/2p2p2/3p4/3P1B2/2NQ4/PPP1PPPP/2KR1BNR b - - 0 5"
            ChessBoard.makeMove(oldfen14, (-2, -1)) should be(newfen14)

            val oldfen15 = "rnbqk2r/pppp1ppp/5n2/2b1p3/4P3/3P1N2/PPP1QPPP/RNB1KB1R b KQkq - 0 4"
            val newfen15 = "rnbq1rk1/pppp1ppp/5n2/2b1p3/4P3/3P1N2/PPP1QPPP/RNB1KB1R w KQ - 0 5"
            ChessBoard.makeMove(oldfen15, (-3, -1)) should be(newfen15)

            val oldfen16 = "r3kbnr/ppp1pppp/2nq4/3p1b2/4P3/N1PB1Q2/PP1P1PPP/R1B1K1NR b KQkq - 0 5"
            val newfen16 = "2kr1bnr/ppp1pppp/2nq4/3p1b2/4P3/N1PB1Q2/PP1P1PPP/R1B1K1NR w KQ - 0 6"
            ChessBoard.makeMove(oldfen16, (-4, -1)) should be(newfen16)

        }

        "return the correct moves for castling" in {
            val move1: (Int, Int) = (-1, -1)
            val move2: (Int, Int) = (-2, -1)
            val move3: (Int, Int) = (-3, -1)
            val move4: (Int, Int) = (-4, -1)
            val move5: (Int, Int) = (4, 5)
            val move6: (Int, Int) = (60, 61)
            val move7: (Int, Int) = (0, 1)
            val defaultBoard = ChessBoard.getDefaultBoard()
            ChessBoard.translateCastle(defaultBoard, (60, 62)) should be(move1)
            ChessBoard.translateCastle(defaultBoard, (60, 58)) should be(move2)
            ChessBoard.translateCastle(defaultBoard, (4, 6)) should be(move3)
            ChessBoard.translateCastle(defaultBoard, (4, 2)) should be(move4)
            ChessBoard.translateCastle(defaultBoard, (4, 5)) should be(move5)
            ChessBoard.translateCastle(defaultBoard, (60, 61)) should be(move6)
            ChessBoard.translateCastle(defaultBoard, (0, 1)) should be(move7)
        }

        "detect a possible promotion" in {
            val fenWhitePromotion = "rPbqkbnr/1pppppp1/8/8/8/8/P1PPPPpP/RNBQKBNR b KQkq - 0 5"
            ChessBoard.canPromote(fenWhitePromotion) should be(1)

            val fenBlackPromotion = "rQbqkbnr/1pppppp1/8/8/8/8/P1PPPP1P/RNBQKBNp w Qkq - 0 6"
            ChessBoard.canPromote(fenBlackPromotion) should be(63)
        }
    }

}
