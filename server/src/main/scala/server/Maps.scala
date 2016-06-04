package server

import org.http4s.{Request, Response}
import ApiHelper._
import model.GameId
import model.map._
import model.property.{ScalarProperty, VectorProperty, VectorPropertyElement}
import server.properties.GamePropertyRegistry
import state.property.{BasicSparseVectorProperty, ElementId, PropertyId}
import topology.space.CartesianCell

import scalaz.concurrent.Task
import scalaz.syntax.either._
import scalaz.syntax.std.boolean._


object Maps extends QueryParamHelper {
  val paramTopY = "topY"
  val paramBottomY = "bottomY"
  val paramLeftX = "leftX"
  val paramRightX = "rightX"

  val testGame = Game.buildTestGame

  def getMapData(req: Request, gameId: GameId, mapId: MapId): Task[Response] = {
    //  TODO - validate API key and infer identity
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
      _ <- wrapM(bounds.fold(true)(r =>
        (r.topLeft.x < r.bottomRight.x) && (r.topLeft.y < r.bottomRight.y)) either
          () or
          badRequest(s"Bounds top left must be above and left of the bottom right"))
    } yield bounds
  }

  private def normalizeDimension(from: Double, max: Double) = {
    if ( from < 0 ) from + ((-from/max).toInt+1)*max
    else if (from > max) from - (from/max).toInt*max
    else from
  }

  private def normalizeRectDimension(r: Rectangle,
                                     max: Double,
                                     getter: Rectangle=>(Double,Double),
                                     setter:(Rectangle,Double,Double)=>Rectangle): List[Rectangle] = {
    def normalizeRange(range: (Double,Double)) = {
      if (Math.abs(range._1 - range._2) > max) {
        (range._1, range._1 + max)
      }
      else range
    }

    val raw = normalizeRange(getter(r))
    val normalized = (normalizeDimension(raw._1,max), normalizeDimension(raw._2,max))

    if (normalized._1 < normalized._2) List(setter(r,normalized._1,normalized._2))
    else
      List(
        setter(r,0.0,normalized._2),
        setter(r,normalized._1, max)
      )
  }

  private def buildMapResponse(maybeBounds: Option[Rectangle], map: GameMap) = {
    val topology =  map.mapData.getMapTopology

    def boundsChecker(bounds: List[Rectangle])(cell: CartesianCell) = {
      val cellLocation = map.mapData.topology.cellCoordinates(cell).toPoint
      bounds.exists(_.contains(cellLocation))
    }

    def makeBoundsList(rawBounds: Rectangle) = {
      def xGetter(r: Rectangle) = (r.topLeft.x,r.bottomRight.x)
      def xSetter(r: Rectangle, left: Double, right: Double) =
        Rectangle(Point(left,r.topLeft.y), Point(right,r.bottomRight.y))
      def yGetter(r: Rectangle) = (r.topLeft.y,r.bottomRight.y)
      def ySetter(r: Rectangle, top: Double, bottom: Double) =
        Rectangle(Point(r.topLeft.x,top), Point(r.bottomRight.x,bottom))

      val xAdjusted =
        normalizeRectDimension(
          rawBounds,
          map.mapData.projectionWidth,
          xGetter,
          xSetter)
      val result = xAdjusted.map(r =>
        normalizeRectDimension(
          r,
          map.mapData.projectionHeight,
          yGetter,
          ySetter
        )).flatten

      result
    }

    def toScalarProperty(el: (PropertyId, Double)): ScalarProperty = {
      val property = GamePropertyRegistry.scalarProperties.getOrElse(el._1, throw new RuntimeException(s"Unknown scalar property ${el._1.value} encountered"))
      ScalarProperty(property.name, el._2)
    }

    def toVectorElement(el: (ElementId, Double)): VectorPropertyElement = {
      VectorPropertyElement(el._1.value, el._2)
    }

    def toVectorProperty(el: (PropertyId, BasicSparseVectorProperty[Double])): VectorProperty = {
      val property = GamePropertyRegistry.vectorProperties.getOrElse(el._1, throw new RuntimeException(s"Unknown vector property ${el._1.value} encountered"))
      VectorProperty(property.name, el._2.toList.map(toVectorElement))
    }

    def toCellInfo(cell: CartesianCell) = {
      val cellState = map.mapData.cellStateValue(cell)

      CellInfo(
        map.mapData.topology.cellCoordinates(cell).toPoint,
        cellState.scalarProperties.toList.map(toScalarProperty),
        cellState.vectorProperties.toList.map(toVectorProperty))
    }

    val filteredCells =
      maybeBounds.fold(
        map.mapData.cells)(bounds =>
        map.mapData.cells.filter(boundsChecker(makeBoundsList(bounds))))
    MapResponse(
      topology,
      Nil,
      filteredCells
        .map(toCellInfo)
        .toList
    )
  }

}
