package server

import org.http4s.dsl._
import org.http4s.server.HttpService


object AscentService {
  def service(resourceProvider: StaticResourceProvider) = HttpService {
    case req if req.uri.path.startsWith("/resources/") =>
      resourceProvider.get(req.pathInfo, req)

    case req@(GET -> Root / "games" / gameId / "maps" / mapId) =>
      Maps.getMapData(req)
  }
}
