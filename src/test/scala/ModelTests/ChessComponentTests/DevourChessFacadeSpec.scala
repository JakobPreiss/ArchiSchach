package ModelTests.ChessComponentTests

import Model.BasicChessComponent.StandartChess.{BasicChessFacade, Color, Piece, PieceType}
import Model.ChessComponent.DevourChess.{DevourChessFacade, Remis}
import Model.ChessComponent.RealChess.RealChessFacade
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Success

class DevourChessFacadeSpec extends AnyWordSpec {
    val testInstance = DevourChessFacade()
    "DevourChessFacade" should {
        "not do anything yet" in {
        true should be (true)
        }
    }

}
