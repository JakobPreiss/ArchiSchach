package ModelTests.ChessComponentTests

import Model.ChessComponent.DevourChess.{DevourChessFacade, Remis}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*

import scala.util.Success

class DevourChessSpec extends AnyWordSpec {
    val testObject = DevourChessFacade()
    "DevourChess" should {
        "getAllLegalMoves Test" in {
            val fen = "rnbqkbnr/ppppppp1/8/7p/4P3/8/PPPP1PPP/RNBQKBNR w KQkq h6 0 1"


            testObject.getAllLegalMoves(fen) should be(Success(List((59, 31))))

            testObject.getAllLegalMoves("rnb1kbnr/pppp1ppp/8/4p3/4P1Qq/8/PPPP1PPP/RNB1KBNR w KQkq - 0 1") should be (Success(List((38, 14), (38, 39), (38, 11))))
        }
        "isColorPiece Test" in {
            val fen = "rnbqkbnr/ppppppp1/8/7p/4P3/8/PPPP1PPP/RNBQKBNR w KQkq h6 0 1"
            testObject.isColorPiece(fen, 59) should be(Success(true))
            testObject.isColorPiece(fen, 0) should be(Success(false))
            testObject.isColorPiece(fen, 31) should be(Success(false))
        }

        "isRemis Test" in {
            val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            testObject.isRemis(fen, testObject.getAllLegalMoves(fen).getOrElse(List())).getOrElse(true) should be (false)

            val fen2 = "8/8/8/8/6B1/3b4/8/8 w - - 0 1"
            testObject.isRemis(fen2, testObject.getAllLegalMoves(fen2).getOrElse(List())).getOrElse(true) should be (false)

            val fen3 = "8/8/8/6B1/3b4/8/8/8 w - - 0 1"
            testObject.isRemis(fen3, testObject.getAllLegalMoves(fen3).getOrElse(List())).getOrElse(true) should be (false)

            val fen4 = "8/8/8/6B1/8/3b4/8/8 w - - 0 1"
            testObject.isRemis(fen4, testObject.getAllLegalMoves(fen4).getOrElse(List().appended((1,1)))).getOrElse(false) should be (true)

            val fen5 = "8/8/8/5B2/8/4b3/8/8 b - - 0 1"
            testObject.isRemis(fen5, testObject.getAllLegalMoves(fen5).getOrElse(List().appended((1,1)))).getOrElse(false) should be (true)

            Remis.isRemis("B7/8/1b6/8/8/8/8/8 w - - 0 1") should be(true)
        }

        "getBestMove Test" in {
            testObject.getBestMove("", 0) should be(Success(""))
        }

        "translateMoveStringToInt Test" in {
            testObject.translateMoveStringToInt("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4") should be (Success((52, 36)))
        }

        "getDefaultFen Test" in {
            testObject.getDefaultFen() should be ("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        }
    }
}
