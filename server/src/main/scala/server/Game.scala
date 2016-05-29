package server

import model.{GameId, MapId}


final case class Game(id: GameId, maps: Map[MapId, GameMap])

object Game {
  val testGameId = GameId("test")

  def buildTestGame =
    Game(
      testGameId,
      Map(GameMap.testMapId -> GameMap.buildTestMap))
}