package server

import org.http4s.{Request, Response}
import ApiHelper._
import model.GameId
import model.map._
import topology.space.CartesianCell

import scalaz.concurrent.Task
import scalaz.syntax.either._


object Maps extends QueryParamHelper {
  val paramTopY = "topY"
  val paramBottomY = "bottomY"
  val paramLeftX = "leftX"
  val paramRightX = "rightX"

  val testGame = Game.buildTestGame

  def getMapData(req: Request, gameId: GameId, mapId: MapId): Task[Response] = {
    //  TODO - validate API key and infer identity
    //  TODO - find game
    //  TODO - find specific map
    join {
      for {
        maybeBounds <- validateBounds(req)
        game <- Game.getGame(gameId)
        map <- game.getMap(mapId)
      } yield buildMapResponse(maybeBounds, map)
    }
  }

  private def validateBounds(req: Request) = {
    for {
      maybeTopY <- optionalDouble(req, paramTopY)
      maybeBottomY <- optionalDouble(req, paramBottomY)
      maybeLeftX <- optionalDouble(req, paramLeftX)
      maybeRightX <- optionalDouble(req, paramRightX)
      bounds <-
        maybeTopY.flatMap(topY =>
          maybeBottomY.flatMap(bottomY =>
            maybeLeftX.flatMap(leftX =>
              maybeRightX.map(rightX => Rectangle(Point(leftX, topY), Point(rightX, bottomY)))))).fold(
          if (maybeTopY.isDefined || maybeBottomY.isDefined || maybeLeftX.isDefined || maybeRightX.isDefined)
            wrapM(badRequest(s"If bounds are specified they must be complete and specify all 4 bounding rectangle bounds").left[Option[Rectangle]])
          else successM(None:Option[Rectangle]))(r => successM(Some(r)))
    } yield bounds
  }

  private def buildMapResponse(maybeBounds: Option[Rectangle], map: GameMap) = {
    def boundsChecker(bounds: Rectangle)(cell: CartesianCell) = {
      val cellLocation = map.mapData.topology.cellCoordinates(cell).toPoint
      bounds.contains(cellLocation)
    }

    def toCellInfo(cell: CartesianCell) = {
      CellInfo(
        map.mapData.topology.cellCoordinates(cell).toPoint,
        Nil,
        Nil)
    }

    val filteredCells =
      maybeBounds.fold(
        map.mapData.cells)(bounds =>
        map.mapData.cells.filter(boundsChecker(bounds)))
    MapResponse(
      map.mapData.getMapTopology,
      Nil,
      filteredCells
        .map(toCellInfo)
        .toList
    )
  }

}
