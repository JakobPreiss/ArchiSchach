package Model.ChessComponent.RealChess

import requests.Response

import scala.util.{Failure, Success, Try}

object ChessApiClient {
    var host = "https://stockfish.online/api/s/v2.php"

    def getBestMove(fen: String, depth: Int): Try[String] = {
        if (depth >= 16) {
            return Failure(new IllegalArgumentException("Depth must be less than 16"))
        }

        val params = Map(
            "fen" -> fen,
            "depth" -> depth.toString
        )

        val response: Response = requests.get(
            url = host,
            params = params
        )

        if (response.statusCode == 200) {
            val json = ujson.read(response.text())
            if (json("success").bool) {
                Success(json("bestmove").str.split(" ")(1))
            } else {
                println("a")
                Failure(new Exception(s"API request failed: ${json("data").str}"))
            }
        } else {
            println("b")
            Failure(new Exception(s"Error: ${response.statusCode}, ${response.text()}"))
        }
    }
}