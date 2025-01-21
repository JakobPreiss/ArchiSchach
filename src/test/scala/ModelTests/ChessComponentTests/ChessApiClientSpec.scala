package ModelTests.ChessComponentTests

import Model.ChessComponent.RealChess.ChessApiClient
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.BeforeAndAfterEach

class ChessApiClientSpec extends AnyWordSpec with BeforeAndAfterEach {
    "ChessApiClient" should {
        "return a valid best move for a valid FEN and depth" in {
            val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            val depth = 5
            val bestMove = ChessApiClient.getBestMove(fen, depth)
            bestMove should fullyMatch regex "[a-h][1-8][a-h][1-8][nbrq]?"
        }
        "throw an exception for a depth greater than or equal to 16" in {
            val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            val depth = 16
            assertThrows[IllegalArgumentException] {
                ChessApiClient.getBestMove(fen, depth)
            }
        }
        "throw an exception when the API request fails with success false" in {
            val fen = "invalid fen which hopefully makes the real api return false"
            val depth = 5
            assertThrows[Exception] {
                ChessApiClient.getBestMove(fen, depth)
            }
        }

        "throw an exception when the API returns a non-200 status code" in {
            val originalHost = ChessApiClient.host
            ChessApiClient.host = "https://an.invalid.url.that.does.not.exist.stockfish.online/api/s/v2.php" // Replace with an invalid URL that causes a connection error
            val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            val depth = 5
            val exception = intercept[Exception] {
                ChessApiClient.getBestMove(fen, depth)
            }
            exception.getMessage should startWith("Unknown") // The exception message in this case will be connection specific
            // Reset the host back to its original value to not affect other tests.
            ChessApiClient.host = originalHost
        }
    }
}