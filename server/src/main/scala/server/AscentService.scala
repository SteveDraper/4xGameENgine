package server

import model.GameId
import model.map.MapId
import org.http4s.dsl._
import org.http4s.server.HttpService
import server.map.Maps
import server.properties.{GamePropertyRegistry, Properties}


object AscentService {
  def service(resourceProvider: StaticResourceProvider) = HttpService {
    case req if req.uri.path.startsWith("/resources/") =>
      resourceProvider.get(req)

    case req@(GET -> Root / "properties" ) =>
      Properties.getProperties(req)

    case req@(GET -> Root / "games" / gameId / "maps" / mapId) =>
      Maps.getMapData(req, GameId(gameId), MapId(mapId))

    case req@(GET -> Root / "games" / gameId / "maps" / mapId / "metadata") =>
      Maps.getMapMetadata(req, GameId(gameId), MapId(mapId))

    case req@(POST -> Root / "games" / gameId / "update") =>
      Game.update(req, GameId(gameId))

    case req@(POST -> Root / "games" / gameId / "reset") =>
      Game.reset(req, GameId(gameId))
  }
}
