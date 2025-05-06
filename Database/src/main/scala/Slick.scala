package DatabaseService

import DatabaseService.DatabaseDAO
import SharedResources.State

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{ProvenShape, Tag}

import scala.concurrent.{ExecutionContext, Future}

import scala.concurrent.{ExecutionContext, Future}

class Slick(db: Database)(implicit ec: ExecutionContext) extends DatabaseDAO {

  implicit val stateMapper: BaseColumnType[State] = MappedColumnType.base[State, Int](
    state => state.ordinal,
    ordinal => State.fromOrdinal(ordinal)
  )

  class GameStateTable(tag: Tag) extends Table[GameState](tag, "game_state") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def fen: Rep[String] = column[String]("fen")
    def state: Rep[Int] = column[Int]("state")

    override def * : ProvenShape[GameState] = (id, fen, state) <> (GameState.apply.tupled, GameState.unapply)
  }

  private val gameStates = TableQuery[GameStateTable]

  override def init(): Unit = {
    val setup = DBIO.seq(
      gameStates.schema.createIfNotExists
    )
    db.run(setup)
  }

  override def saveGameState(fen: String, state: Int): Future[Unit] = {
    val insert = GameState(fen = fen, state = state)
    db.run(gameStates += insert).map(_ => ())
  }

  override def loadGameState(): Future[Option[String]] = {
    val result = db.run(gameStates.sortBy(_.id.desc).result.headOption)
    result.map {
      case Some(gameState) => Some(gameState.fen)
      case None            => Some("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    }
  }
}
