package server.player

import model.map.{MapId, Point, Rectangle}
import org.http4s.Request
import server.ApiHelper._
import server.Game
import server.map.{GameMap, MapView}

final case class User(maps: Map[MapId, MapView])

object User {
  def getCurrentUser(req: Request): TaskFailureOr[User] = {
    for {
      testGame <- Game.getGame(Game.testGameId)
    } yield User(testGame.maps.mapValues(mapViewForMap))
  }

  private def mapViewForMap(gameMap: GameMap) = {
    if ( gameMap.id == Game.smallTestMapId )
      MapView(
        Point(0.0, 0.0),
        Rectangle(Point(-10.0, -10.0), Point(10.0, 10.0))
      )
    else
      MapView(
        Point(0.0, 0.0),
        Rectangle(
          Point(-gameMap.mapData.projectionWidth/2, -gameMap.mapData.projectionHeight/2),
          Point(gameMap.mapData.projectionWidth/2, gameMap.mapData.projectionHeight/2))
      )
  }
}