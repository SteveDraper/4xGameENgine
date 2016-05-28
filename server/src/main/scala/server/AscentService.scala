package server

import org.http4s.HttpService
import org.http4s.dsl._


object AscentService {
  val service = HttpService {
    case GET -> Root / "resources" / name =>
      StaticResources.get(name)

    case req@(GET -> Root / "games" / gameId / "maps" / mapId) =>
      Maps.getMapData(req)
  }
}
