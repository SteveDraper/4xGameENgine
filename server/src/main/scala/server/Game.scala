package server

import model.GameId
import model.map.MapId
import ApiHelper._
import server.map.GameMap
import server.util.stm.{FreeSTM, TVar}
import FreeSTM._
import org.http4s.Request

import scalaz.syntax.either._
import scalaz.syntax.std.option._
import server.util.stm.FreeSTM.STM

import scalaz.concurrent.Task
import scalaz.syntax.traverse._
import scalaz.std.list._

final case class Game(id: GameId, maps: Map[MapId, TVar[GameMap]]) {
  import Game._

  def getMap(mapId: MapId) = for {
    t <- wrapM(maps.get(mapId) \/> notFound(s"Map '$mapId' not found"))
    m <- readMap(t)
  } yield m

  def applyToMaps[A](f: TVar[GameMap] => TaskFailureOr[A]): TaskFailureOr[Map[MapId,A]] = {
    maps
      .mapValues(f)
      .toList
      .map(el => el._2.map(a => (el._1,a)))
      .sequenceU
      .map(_.toMap)
  }

  def update = {
    applyToMaps { t => wrapM(
      atomically {
        for {
          m <- readTVar(t)
          _ <- writeTVar(t, m.update)
        } yield ()
      })
    }
  }
}

object Game {
  val testGameId = GameId("test")
  val smallTestMapId = MapId("test")
  val largeTestMapId = MapId("testLarge")
  lazy val testGame = buildTestGame

  def buildTestGame =
    Game(
      testGameId,
      Map(
        smallTestMapId -> TVar(GameMap.buildTestMap(16, smallTestMapId)),
        largeTestMapId -> TVar(GameMap.buildTestMap(100, largeTestMapId))))

  def getGame(id: GameId) =
    if ( id == testGameId ) successM(testGame)
    else wrapM(notFound(s"Game '$id' not found").left)

  def update(req: Request, id: GameId) = join {
    for {
      game <- getGame(id)
      result <- game.update
    } yield ()
  }

  def readMap(t: TVar[GameMap]) = wrapM {
    atomically {
      for {
        m <- readTVar(t)
      } yield m
    }
  }
}