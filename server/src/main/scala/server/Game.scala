package server


final case class GameId(value: String) extends AnyVal
final case class Game(id: GameId, maps: Map[MapId, GameMap])

object Game {
  val testGameId = GameId("test")

  def buildTestGame =
    Game(
      testGameId,
      Map(GameMap.testMapId -> GameMap.buildTestMap))
}