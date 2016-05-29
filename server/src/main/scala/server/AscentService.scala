package server

import org.http4s.dsl._
import org.http4s.server.HttpService


object AscentService {
  def service(resourceProvider: StaticResourceProvider) = HttpService {
    case GET -> Root / "resources" / name =>
      resourceProvider.get(name)

    case req@(GET -> Root / "games" / gameId / "maps" / mapId) =>
      Maps.getMapData(req)
  }
}
