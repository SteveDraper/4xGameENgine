package server

import model.GameId
import model.map.MapId
import ApiHelper._
import server.map.GameMap

import scalaz.syntax.either._
import scalaz.syntax.std.option._


final case class Game(id: GameId, maps: Map[MapId, GameMap]) {
  def getMap(mapId: MapId) =
    wrapM(maps.get(mapId) \/> notFound(s"Map '$mapId' not found"))
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
        smallTestMapId -> GameMap.buildTestMap(15, smallTestMapId),
        largeTestMapId -> GameMap.buildTestMap(100, largeTestMapId)))

  def getGame(id: GameId) =
    if ( id == testGameId ) successM(testGame)
    else wrapM(notFound(s"Game '$id' not found").left)
}