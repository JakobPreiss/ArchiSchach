package DatabaseService

import SharedResources.State

case class GameState(id: Long = 0L, fen: String, state: Int)