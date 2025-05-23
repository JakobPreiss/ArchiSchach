package ControllerTests

import BasicChess.StandartChess.{ChessBoard, PseudoMoves}
import RealChess.LegalMoves
import SharedResources.util.Observer
import TUI.Tui

import scala.language.reflectiveCalls
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ChessContextSpec extends AnyWordSpec with Matchers {

    "ChessContext" should {
        "change the state correctly" in {
            var context = new ChessContext()
            val white1 = Event(false, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", false)
            context.handle(white1);
            context.state should be (State.whitePlayingState);

            val black1 = Event(false, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", false)
            context.handle(black1);
            context.state should be (State.blackPlayingState);

            val white2 = Event(true, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", false)
            context.handle(white2);
            context.state should be (State.blackWonState);

            val black2 = Event(true, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", false)
            context.handle(black2)
            context.state should be (State.whiteWonState);

            val remis = Event(true, "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1", true)
            context.handle(remis);
            context.state should be (State.remisState);
        }
    }
}
