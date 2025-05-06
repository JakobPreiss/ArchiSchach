package DatabaseService

import scala.concurrent.Future

trait DatabaseDAO {
  def init(): Unit
  def saveGameState(fen: String, state: Int): Future[Unit]
  def loadGameState(): Future[Option[String]]
}
