package server.player

import model.map.{MapId, Point, Rectangle}
import org.http4s.Request
import server.ApiHelper._
import server.Game
import server.map.{GameMap, MapView}
import server.util.stm.FreeSTM._
import server.util.stm.TVar


final case class User(maps: Map[MapId, MapView])

object User {
  def getCurrentUser(req: Request): TaskFailureOr[User] = {
    for {
      testGame <- Game.getGame(Game.testGameId)
      maps <- testGame.applyToMaps(extractMapView)
    } yield User(maps.toMap)
  }

  private def extractMapView(t: TVar[GameMap]) = wrapM {
    atomically {
      for {
        m <- readTVar(t)
      } yield mapViewForMap(m)
    }
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