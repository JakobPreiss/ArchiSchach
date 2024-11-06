import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers._
import ScholarsMate._
import ChessBoard._

class ScholarsMateSpec extends AnyWordSpec {
    /*
    "ScholarsMate" should {
        "move a piece on the board" in {
            val e4FEN = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"

            makeMove(ChessBoard.getDefaultBoard(), (52, 36)) should equal(ChessBoard.fenToBoard(e4FEN));
        }

        "return the same string of moves" in {
            ScholarsMate.movesToString(ChessBoard.getDefaultBoard(), ScholarsMate.moves) should be("    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  n  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  p  |  p  |  p  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  P  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  Q  |  K  |  B  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n" +

                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  n  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  p  |  p  |  p  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  .  |  .  |  P  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  .  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  Q  |  K  |  B  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n" +

                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  n  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  p  |  .  |  p  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  p  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  .  |  .  |  P  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  .  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  Q  |  K  |  B  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n" +

                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  n  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  p  |  .  |  p  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  p  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  B  |  .  |  P  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  .  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  Q  |  K  |  .  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n" +

                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  .  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  p  |  .  |  p  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  n  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  p  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  B  |  .  |  P  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  .  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  Q  |  K  |  .  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n" +

                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  .  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  p  |  .  |  p  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  n  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  p  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  B  |  .  |  P  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  Q  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  .  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  .  |  K  |  .  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n" +

                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  .  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  .  |  .  |  p  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  n  |  p  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  p  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  B  |  .  |  P  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  Q  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  .  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  .  |  K  |  .  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n" +

                    "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "8   |  r  |  .  |  b  |  q  |  k  |  b  |  n  |  r  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "7   |  p  |  p  |  p  |  .  |  .  |  Q  |  p  |  p  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "6   |  .  |  .  |  n  |  p  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "5   |  .  |  .  |  .  |  .  |  p  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "4   |  .  |  .  |  B  |  .  |  P  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "3   |  .  |  .  |  .  |  .  |  .  |  .  |  .  |  .  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "2   |  P  |  P  |  P  |  P  |  .  |  P  |  P  |  P  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "1   |  R  |  N  |  B  |  .  |  K  |  .  |  N  |  R  |\n" + "    +-----+-----+-----+-----+-----+-----+-----+-----+\n" + "       a     b     c     d     e     f     g     h     \n\n")
        }
    }
    
     */
}
