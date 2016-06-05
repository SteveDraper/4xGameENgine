package server

import model.GameId
import model.map.MapId
import org.http4s.dsl._
import org.http4s.server.HttpService
import server.map.Maps


object AscentService {
  def service(resourceProvider: StaticResourceProvider) = HttpService {
    case req if req.uri.path.startsWith("/resources/") =>
      resourceProvider.get(req)

    case req@(GET -> Root / "games" / gameId / "maps" / mapId) =>
      Maps.getMapData(req, GameId(gameId), MapId(mapId))
  }
}
